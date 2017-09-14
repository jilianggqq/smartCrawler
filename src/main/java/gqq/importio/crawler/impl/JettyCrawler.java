package gqq.importio.crawler.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
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

/**
 * Crawl urls within the same domain.
 * 
 */
public class JettyCrawler implements Crawler {
	private static final Logger logger = LoggerFactory.getLogger(JettyCrawler.class);
	private final HTMLPageResponseFetcher responseFetcher;
	private final PageURLParser parser;

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
		final Map<String, String> requestHeaders = configuration.getRequestHeadersMap();
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
				currUrls = getNextLevelLinks(allUrls, currUrls, host, requestHeaders);
				currUrls.forEach(url -> logger.info(url.toString()));
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
	public Set<CrawlerURL> getNextLevelLinks(Set<CrawlerURL> allUrls, Set<CrawlerURL> currUrls, String host, Map<String, String> requestHeaders)
			throws Exception {
		logger.info("size of currUrls is {}", currUrls.size());
		// set the urls to be processed.
		responseFetcher.setUrls(currUrls);
		responseFetcher.processing();
		Set<HTMLPageResponse> responses = responseFetcher.getResponses();
		final Set<CrawlerURL> nextLevel = new LinkedHashSet<CrawlerURL>();

		for (HTMLPageResponse response : responses) {
			if (HttpStatus.OK_200 == response.getResponseCode() && response.getResponseType().indexOf("html") > 0) {
				// we know that this links work
				final Set<CrawlerURL> allLinks = parser.get(response);

				for (CrawlerURL link : allLinks) {
					// only add if it is the same host
					if (host.equals(link.getHost()) && !allUrls.contains(link)) {
						nextLevel.add(link);
						allUrls.add(link);
					}
				}
			} else {
				allUrls.remove(response.getUrl());
			}
		}

		return nextLevel;
	}
	
}