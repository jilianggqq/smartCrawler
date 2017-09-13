package gqq.importio.crawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.eclipse.jetty.http.HttpStatus;

import gqq.importio.crawler.util.StatusCode;

/**
 * Crawl urls within the same domain.
 * 
 */
public class JettyCrawler {

	private final HTMLPageResponseFetcher responseFetcher;
	private final ExecutorService service;
	private final PageURLParser parser;

	/**
	 * Create a new crawler.
	 * 
	 * @param theResponseFetcher
	 *            the response fetcher to use.
	 * @param theService
	 *            the thread pool.
	 * @param theParser
	 *            the parser.
	 */
	public JettyCrawler(HTMLPageResponseFetcher theResponseFetcher, ExecutorService theService, PageURLParser theParser) {
		service = theService;
		responseFetcher = theResponseFetcher;
		parser = theParser;
	}
	
	public JettyCrawler(PageURLParser theParser) {
		parser = theParser;
		responseFetcher = null;
		service = null;
	}
	
	public static void main(String[] args) throws URISyntaxException {
		JettyCrawler jc = new JettyCrawler(new AhrefPageURLParser());
//		jc.getNextLevelLinks(allUrls, currUrls, verifiedUrls, requestHeaders);
		CrawlerConfiguration config = CrawlerConfiguration.builder().setStartUrl("https://www.scientificamerican.com/podcast/60-second-science/").setMaxLevels(2).build();
		
		
		jc.getUrl(config);
	}
	
	public static String getDomainName(String url) throws URISyntaxException {
	    URI uri = new URI(url);
	    return uri.getHost();

	}

	/**
	 * Shutdown the crawler.
	 */
	public void shutdown() {
		if (service != null)
			service.shutdown();
		if (responseFetcher != null)
			responseFetcher.shutdown();
	}
	
	public void getUrl(CrawlerConfiguration configuration) throws URISyntaxException {
		final Map<String, String> requestHeaders = configuration.getRequestHeadersMap();
//		final HTMLPageResponse resp = verifyInput(configuration.getStartUrl(), requestHeaders);

		String host = getDomainName(configuration.getStartUrl());
		int level = 0;
		Set<CrawlerURL> currUrls = new HashSet<>();
		final Set<CrawlerURL> allUrls = new LinkedHashSet<CrawlerURL>();

		CrawlerURL startURI = new CrawlerURL(configuration.getStartUrl());
		currUrls.add(startURI);
		allUrls.add(startURI);
		
		while (level < configuration.getMaxLevels()) {
			currUrls.forEach(url -> System.out.println(url));
			System.out.println("---------------------------");
			currUrls = getNextLevelLinks(allUrls, currUrls, host, requestHeaders);
			currUrls.forEach(url -> System.out.println(url));
			level++;
		}

//		verifiedUrls.add(resp);

//		final String host = resp.getPageUrl().getHost();
//		getNextLevelLinks(allUrls, currUrls, verifiedUrls, new HashMap<>());
	}

	public Set<CrawlerURL> getNextLevelLinks(Set<CrawlerURL> allUrls, Set<CrawlerURL> currUrls, String host,
			Map<String, String> requestHeaders) {
		JettyClientResponseFetcher fetcher = new JettyClientResponseFetcher();
		fetcher.get(currUrls, requestHeaders);
		Set<HTMLPageResponse> responses = fetcher.getResponses();
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

	/**
	 * Fetch links to the next level of the crawl.
	 * 
	 * @param responses
	 *            holding bodys where we should fetch the links.
	 * @param allUrls
	 *            every url we have fetched so far
	 * @param nonWorkingUrls
	 *            the urls that didn't work to fetch
	 * @param verifiedUrls
	 *            responses that are already verified
	 * @param host
	 *            the host we are working on
	 * @param onlyOnPath
	 *            only fetch files that match the following path. If empty, all will match.
	 * @param notOnPath
	 *            don't collect/follow urls that contains this text in the url
	 * @return the next level of links that we should fetch
	 */
	protected Set<CrawlerURL> fetchNextLevelLinks(Map<Future<HTMLPageResponse>, CrawlerURL> responses, Set<CrawlerURL> allUrls,
			Set<HTMLPageResponse> nonWorkingUrls, Set<HTMLPageResponse> verifiedUrls, String host, String onlyOnPath, String notOnPath) {

		final Set<CrawlerURL> nextLevel = new LinkedHashSet<CrawlerURL>();

		final Iterator<Entry<Future<HTMLPageResponse>, CrawlerURL>> it = responses.entrySet().iterator();

		while (it.hasNext()) {

			final Entry<Future<HTMLPageResponse>, CrawlerURL> entry = it.next();

			try {

				final HTMLPageResponse response = entry.getKey().get();
				if (HttpStatus.OK_200 == response.getResponseCode() && response.getResponseType().indexOf("html") > 0) {
					// we know that this links work
					verifiedUrls.add(response);
					final Set<CrawlerURL> allLinks = parser.get(response);

					for (CrawlerURL link : allLinks) {
						// only add if it is the same host
						if (host.equals(link.getHost()) && link.getUrl().contains(onlyOnPath)
								&& (notOnPath.equals("") ? true : (!link.getUrl().contains(notOnPath)))) {
							if (!allUrls.contains(link)) {
								nextLevel.add(link);
								allUrls.add(link);
							}
						}
					}
				} else if (HttpStatus.OK_200 != response.getResponseCode()
						|| StatusCode.SC_SERVER_REDIRECT_TO_NEW_DOMAIN.getCode() == response.getResponseCode()) {
					allUrls.remove(entry.getValue());
					nonWorkingUrls.add(response);
				} else {
					// it is of another content type than HTML or if it redirected to another domain
					allUrls.remove(entry.getValue());
				}

			} catch (InterruptedException e) {
				nonWorkingUrls.add(new HTMLPageResponse(entry.getValue(), StatusCode.SC_SERVER_RESPONSE_UNKNOWN.getCode(),
						Collections.<String, String>emptyMap(), "", "", 0, "", -1));
			} catch (ExecutionException e) {
				nonWorkingUrls.add(new HTMLPageResponse(entry.getValue(), StatusCode.SC_SERVER_RESPONSE_UNKNOWN.getCode(),
						Collections.<String, String>emptyMap(), "", "", 0, "", -1));
			}
		}
		return nextLevel;
	}

}