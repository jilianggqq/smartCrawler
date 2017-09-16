package gqq.importio.crawler;

import java.util.HashMap;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gqq.importio.crawler.impl.AhrefPageURLParser;
import gqq.importio.crawler.impl.JettyClientResponseFetcher;
import gqq.importio.crawler.impl.JettyCrawler;
import gqq.importio.dao.service.RedisUrlService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class WhenCrawlerRun {

	@Autowired
	private RedisUrlService service;

	@Test
	public void testWithMkyong() throws Exception {
		
		HttpClient httpClient = new HttpClient(new SslContextFactory());
		HTMLPageResponseFetcher fetcher = new JettyClientResponseFetcher(new HashMap<>(), httpClient);
		Crawler crawler = new JettyCrawler(fetcher, new AhrefPageURLParser(), service);

		String start = "http://www.mkyong.com";
		// String start = "https://sfbay.craigslist.org/";
		// level default is 2
		CrawlerConfiguration config = CrawlerConfiguration.builder().setStartUrl(start).build();
		crawler.doProcess(config);
	}

}
