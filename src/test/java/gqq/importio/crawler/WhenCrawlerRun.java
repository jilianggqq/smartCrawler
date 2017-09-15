package gqq.importio.crawler;

import java.util.HashMap;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;

import gqq.importio.crawler.impl.AhrefPageURLParser;
import gqq.importio.crawler.impl.JettyClientResponseFetcher;
import gqq.importio.crawler.impl.JettyCrawler;

public class WhenCrawlerRun {

	@Test
	public void testWithMkyong() throws Exception {
		HttpClient httpClient = new HttpClient(new SslContextFactory());
		HTMLPageResponseFetcher fetcher = new JettyClientResponseFetcher(new HashMap<>(), httpClient);
		Crawler crawler = new JettyCrawler(fetcher, new AhrefPageURLParser());

		CrawlerConfiguration config = CrawlerConfiguration.builder().setStartUrl("http://www.mkyong.com").setMaxLevels(2).build();
		crawler.doProcess(config);
	}
	
	@Test
	public void testWithMkong2() throws Exception {
//		HttpClient httpClient = new HttpClient(new SslContextFactory());
//		HTMLPageResponseFetcher fetcher = new JettyClientResponseFetcher(new HashMap<>(), httpClient);
//		Crawler crawler = new JettyCrawler(fetcher, new AhrefPageURLParser());
//
//		CrawlerConfiguration config = CrawlerConfiguration.builder().setStartUrl("http://www.mkyong.com/author/jacklok/").setMaxLevels(1).build();
//		crawler.doProcess(config);
	}
}
