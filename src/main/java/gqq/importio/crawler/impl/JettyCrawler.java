package gqq.importio.crawler.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import gqq.importio.crawler.Crawler;
import gqq.importio.crawler.CrawlerConfiguration;
import gqq.importio.crawler.CrawlerURL;
import gqq.importio.crawler.HTMLPageResponse;
import gqq.importio.crawler.HTMLPageResponseFetcher;
import gqq.importio.crawler.PageURLParser;
import gqq.importio.dao.RedisUrlRepository;
import gqq.importio.dao.model.RedisUrl;
import gqq.importio.dao.service.RedisUrlService;
import gqq.importio.dao.service.RedisUrlServiceImpl;

/**
 * Crawl urls within the same domain.
 * 
 */
public class JettyCrawler implements Crawler {
	private static final Logger logger = LoggerFactory.getLogger(JettyCrawler.class);
	private final HTMLPageResponseFetcher responseFetcher;
	private final PageURLParser parser;

	private RedisUrlService service;

	@Override
	public void setService(RedisUrlService service) {
		this.service = service;
	}

	// private RedisUrlRepository repository;
	/**
	 * Create a new crawler.
	 * 
	 * @param theResponseFetcher
	 *            the response fetcher to use.
	 * @param theParser
	 *            the parser.
	 */
	public JettyCrawler(HTMLPageResponseFetcher theResponseFetcher, PageURLParser theParser) {
		responseFetcher = theResponseFetcher;
		parser = theParser;
		// service = new RedisUrlServiceImpl(new RedisUrlRepository)
		// service = new RedisUrlServiceImpl(repository);
	}
	//
	// public static void main(String[] args) throws URISyntaxException {
	// JettyCrawler jc = new JettyCrawler(new AhrefPageURLParser());
	//// jc.getNextLevelLinks(allUrls, currUrls, verifiedUrls, requestHeaders);
	// CrawlerConfiguration config =
	// CrawlerConfiguration.builder().setStartUrl("https://www.scientificamerican.com/podcast/60-second-science/").setMaxLevels(2).build();
	//
	//
	// jc.getUrl(config);
	// }

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
	public void doProcess(CrawlerConfiguration configuration) {
		startup();
		try {
			String host = getDomainName(configuration.getStartUrl());
			int level = 0;
			Set<CrawlerURL> currUrls = new HashSet<>();
			final Set<CrawlerURL> allUrls = new LinkedHashSet<CrawlerURL>();

			CrawlerURL startURI = new CrawlerURL(configuration.getStartUrl());
			currUrls.add(startURI);
			allUrls.add(startURI);

			currUrls.forEach(url -> logger.info(url.toString()));
			while (level < configuration.getMaxLevels()) {
				logger.info("------------------------------------------------------");

				// 1. run next level crawler, get the response.
				logger.info("size of currUrls is {}", currUrls.size());
				// set the urls to be processed.
				responseFetcher.setUrls(currUrls);
				responseFetcher.processing();
				Set<HTMLPageResponse> currResponses = responseFetcher.getResponses();
				logger.info("currResponses urls's size is {}", currResponses.size());

				// 2. save the next level results into Redis. (optimization is using async).
				List<RedisUrl> modelUrls = getModelUrls(currResponses);
				logger.info("type of service is {}", service.getClass().getSimpleName());
				logger.info("model urls's size is {}", modelUrls.size());
				modelUrls.forEach(m -> service.saveOrUpdate(m));

				// 3. get NextLevelLinkes from response.

				currUrls = getNextLevelLinks(currUrls, allUrls, host, currResponses);
				// currUrls.forEach(url -> logger.info(url.toString()));
				level++;
			}

		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			shutdown();
		}

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