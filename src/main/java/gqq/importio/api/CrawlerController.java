package gqq.importio.api;

import java.net.URI;
import java.util.HashMap;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gqq.importio.crawler.Crawler;
import gqq.importio.crawler.CrawlerConfiguration;
import gqq.importio.crawler.HTMLPageResponseFetcher;
import gqq.importio.crawler.impl.AhrefPageURLParser;
import gqq.importio.crawler.impl.JettyClientResponseFetcher;
import gqq.importio.crawler.impl.JettyCrawler;
import gqq.importio.crawler.util.StatusCode;
import gqq.importio.dao.model.RedisUrl;
import gqq.importio.dao.service.RedisUrlService;
import gqq.importio.util.DateUtils;

@RestController
public class CrawlerController {
	private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);
	@Autowired
	RedisUrlService service;

	@RequestMapping("/test")
	public String doTest() {
		logger.info("just do test");
		CrawlerConfiguration config = CrawlerConfiguration.builder().setStartUrl("http://www.mkyong.com").setMaxLevels(2).build();
		doCrawler(config);
		return "finished";
	}

	@RequestMapping(value = "/url/search", method = RequestMethod.POST)
	public ResponseEntity<UrlEntity> getVisited(@RequestBody String url) {
		logger.info("url is {}", url);
		RedisUrl rUrl = service.getByUrl(url);
		UrlEntity entity = new UrlEntity();
		if (rUrl == null) {
			entity.setHttpCode(org.eclipse.jetty.http.HttpStatus.NO_CONTENT_204);
			entity.setUrl(org.eclipse.jetty.http.HttpStatus.Code.NO_CONTENT.getMessage());
			return new ResponseEntity<UrlEntity>(entity, HttpStatus.OK);
		}
		entity.setHttpCode(rUrl.getHttpCode());
		entity.setUrl(rUrl.getUrl());
		entity.setTimestamp(DateUtils.fromUnixTimeStr(rUrl.getTimestamp()));
		return new ResponseEntity<UrlEntity>(entity, HttpStatus.OK);

	}

	public void doCrawler(CrawlerConfiguration config) {
		HttpClient httpClient = new HttpClient(new SslContextFactory());
		HTMLPageResponseFetcher fetcher = new JettyClientResponseFetcher(new HashMap<>(), httpClient);
		Crawler crawler = new JettyCrawler(fetcher, new AhrefPageURLParser());
		crawler.setService(service);
		crawler.doProcess(config);
	}
}
