package gqq.importio.crawler.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gqq.importio.crawler.Crawler;
import gqq.importio.crawler.CrawlerConfiguration;
import gqq.importio.crawler.CrawlerURL;
import gqq.importio.crawler.HTMLPageResponse;
import gqq.importio.crawler.HTMLPageResponseFetcher;
import gqq.importio.crawler.PageURLParser;
import gqq.importio.dao.model.RedisUrl;
import gqq.importio.dao.service.RedisUrlService;

/**
 * Crawler with jetty client.
 * 
 * doing http request ancyle
 * 
 * @author gqq
 *
 */
public class JettyCrawler implements Crawler {
	private static final Logger logger = LoggerFactory.getLogger(JettyCrawler.class);
	private final HTMLPageResponseFetcher responseFetcher;
	private final PageURLParser parser;

	private RedisUrlService service;

	private final static int MAX_REQUEST_URLS = 50;
	private final static int SLEEP_MINISECONDS = 100;

	/**
	 * constructor of Jetty crawler
	 * 
	 * @param theResponseFetcher
	 *            response fetcher
	 * @param theParser
	 *            parser which is used to parse html contents using jsoup
	 * @param service
	 *            Redis service used to store data.
	 */
	public JettyCrawler(HTMLPageResponseFetcher theResponseFetcher, PageURLParser theParser, RedisUrlService service) {
		responseFetcher = theResponseFetcher;
		parser = theParser;
		this.service = service;
	}

	public static String getDomainName(String url) throws URISyntaxException {
		URI uri = new URI(url);
		return uri.getHost();
	}

	/**
	 * Shutdown the crawler.
	 */
	@Override
	public void shutdown() {
		if (responseFetcher != null)
			responseFetcher.shutdown();
	}

	/**
	 * Startup the crawler.
	 */
	@Override
	public void startup() {
		responseFetcher.startup();
	}

	/**
	 * scrawler starts from here.
	 * 
	 * @param configuration
	 * @throws URISyntaxException
	 */
	public Set<CrawlerURL> doProcess(CrawlerConfiguration configuration) throws URISyntaxException, Exception {
		final Set<CrawlerURL> allUrls = new LinkedHashSet<CrawlerURL>();
		String host = getDomainName(configuration.getStartUrl());
		int level = 0;
		Set<CrawlerURL> currUrls = new LinkedHashSet<>();

		CrawlerURL startURI = new CrawlerURL(configuration.getStartUrl());
		currUrls.add(startURI);
		allUrls.add(startURI);

		currUrls.forEach(url -> logger.debug(url.toString()));

		try {
			startup();
			while (level < configuration.getMaxLevels()) {
				// 1. run next level crawler, get the response.
				logger.debug("size of currUrls is {}", currUrls.size());

				Iterator<CrawlerURL> iterator = currUrls.iterator();
				Set<HTMLPageResponse> currResponses = new LinkedHashSet<>();
				while (iterator.hasNext()) {
					Set<CrawlerURL> requestUrls = new LinkedHashSet<>();
					int cnt = 0;
					while (iterator.hasNext() && cnt < MAX_REQUEST_URLS) {
						CrawlerURL validUrl = iterator.next();
						requestUrls.add(validUrl);
						iterator.remove();
						cnt++;
					}
					// set the urls to be processed.
					logger.info("\n\n");
					logger.info("**********level : {}, requestUrls num : {}, currUrls num : {}************", level, requestUrls.size(),
							currUrls.size());
					requestUrls.forEach(ru -> logger.debug(ru.getUrl()));
					logger.info("***************** ****************** ************************* ************");
					responseFetcher.setUrls(requestUrls);
					responseFetcher.processing();

					currResponses.addAll(responseFetcher.getResponses());
					if (iterator.hasNext()) {
						// if cnt reaches to max request urls, we need to wait some seconds, and then request again.
						Thread.sleep(SLEEP_MINISECONDS);
					}
					logger.info("currResponses urls's size is {}", currResponses.size());
				}

				// 2. save the next level results into Redis. (optimization is using async).
				List<RedisUrl> modelUrls = getModelUrls(currResponses);
				modelUrls.forEach(m -> service.saveOrUpdate(m));

				// 3. get NextLevelLinkes from response.
				currUrls = getNextLevelLinks(currUrls, allUrls, host, currResponses);
				level++;
			}

		} finally {
			shutdown();
		}

		allUrls.removeAll(currUrls);
		return allUrls;
	}

	/**
	 * get http request results from all the responses.
	 * 
	 * @param currResponses
	 * @return redis urls list
	 */
	@Override
	public List<RedisUrl> getModelUrls(Set<HTMLPageResponse> currResponses) {
		List<RedisUrl> results = new ArrayList<>();
		currResponses.forEach(res -> {
			RedisUrl url = new RedisUrl();
			url.setHttpCode(res.getResponseCode());
			url.setTimestamp(res.getFetchTime());
			url.setUrl(res.getPageUrl().getUrl());
			results.add(url);
		});
		return results;
	}

	/**
	 * get the information for a certain level.
	 * 
	 * @param allUrls
	 *            all the urls.
	 * @param currUrls
	 *            currUrls which are to be visited.
	 * @param host
	 *            host name.
	 * @param requestHeaders
	 *            request headers.
	 * @return crawlerUrl Set
	 * @throws Exception
	 */
	public Set<CrawlerURL> getNextLevelLinks(Set<CrawlerURL> currUrls, Set<CrawlerURL> allUrls, String host, Set<HTMLPageResponse> responses)
			throws Exception {

		final Set<CrawlerURL> nextLevel = new LinkedHashSet<CrawlerURL>();

		for (HTMLPageResponse response : responses) {
			if (HttpStatus.OK_200 == response.getResponseCode() && response.getResponseType().indexOf("html") > 0) {
				// we know that this links work
				final Set<CrawlerURL> allLinks = parser.get(response);

				for (CrawlerURL link : allLinks) {
					// only add if it is the same host
					if (host.equals(link.getHost()) && !allUrls.contains(link)) {
						nextLevel.add(link);
					}
				}
			} else {
				allUrls.remove(response.getUrl());
			}
		}

		return nextLevel;
	}

	@Override
	public void saveIntoRedis(List<RedisUrl> urls) {
		urls.forEach(url -> service.saveOrUpdate(url));
	}

}