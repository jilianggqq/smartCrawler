package gqq.importio.api;

import java.util.HashMap;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import gqq.importio.crawler.Crawler;
import gqq.importio.crawler.CrawlerConfiguration;
import gqq.importio.crawler.HTMLPageResponseFetcher;
import gqq.importio.crawler.impl.AhrefPageURLParser;
import gqq.importio.crawler.impl.JettyClientResponseFetcher;
import gqq.importio.crawler.impl.JettyCrawler;
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
		startCrawler(config);
		return "finished";
	}

	/**
	 * start a crawler from posted url.
	 * 
	 * @param entity
	 *            Request entity.
	 * @param ucBuilder
	 *            uribuilder.
	 * @return created result.
	 */
	@RequestMapping(value = "/url", method = RequestMethod.POST)
	public ResponseEntity<?> crawler(@RequestBody RequestUrlEntity entity, UriComponentsBuilder ucBuilder) {
		logger.info("entity : {}", entity.toString());
		CrawlerConfiguration config = CrawlerConfiguration.builder().setStartUrl(entity.getUrl()).setMaxLevels(entity.getDepth()).build();
		startCrawler(config);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/url").build().toUri());
		return new ResponseEntity<String>(headers, HttpStatus.CREATED);
	}

	/**
	 * check whether a url has been visited.
	 * 
	 * @param entity
	 *            UrlEntity object.
	 * @return 1. 200 UrlEntity 2. 204 UrlEntity
	 */
	@RequestMapping(value = "/urls/search", method = RequestMethod.POST)
	public ResponseEntity<ResponseUrlEntity> getIfVisited(@RequestBody ResponseUrlEntity entity) {
		logger.info("entity's url : {} ", entity.getUrl());
		RedisUrl rUrl = service.getByUrl(entity.getUrl());
		if (rUrl == null) {
			entity.setHttpCode(org.eclipse.jetty.http.HttpStatus.NO_CONTENT_204);
			entity.setUrl(org.eclipse.jetty.http.HttpStatus.Code.NO_CONTENT.getMessage());
			return new ResponseEntity<ResponseUrlEntity>(entity, HttpStatus.NO_CONTENT);
		}
		entity.setHttpCode(rUrl.getHttpCode());
		entity.setTimestamp(DateUtils.fromUnixTimeStr(rUrl.getTimestamp()));
		return new ResponseEntity<ResponseUrlEntity>(entity, HttpStatus.OK);
	}

	/**
	 * start crawler.
	 * @param config
	 */
	public void startCrawler(CrawlerConfiguration config) {
		HttpClient httpClient = new HttpClient(new SslContextFactory());
		HTMLPageResponseFetcher fetcher = new JettyClientResponseFetcher(new HashMap<>(), httpClient);
		Crawler crawler = new JettyCrawler(fetcher, new AhrefPageURLParser());
		crawler.setService(service);
		crawler.doProcess(config);
	}
}
