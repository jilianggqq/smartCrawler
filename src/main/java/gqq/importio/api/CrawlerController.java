package gqq.importio.api;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
import gqq.importio.crawler.CrawlerURL;
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

	/**
	 * start a crawler from posted url.
	 * 
	 * @param entity
	 *            Request entity. 1. url path. 2.the visit depth.
	 * @param ucBuilder
	 *            uribuilder.
	 * @return created result.
	 */
	@RequestMapping(value = "/url", method = RequestMethod.POST)
	public ResponseEntity<?> crawler(@RequestBody RequestUrlEntity entity, UriComponentsBuilder ucBuilder) {
		logger.info("entity : {}", entity.toString());

		// check
		ErrorEntity ee = new ErrorEntity();
		if (!check(entity.getUrl(), ee)) {
			return new ResponseEntity<ErrorEntity>(ee, HttpStatus.BAD_REQUEST);
		}

		CrawlerConfiguration config = CrawlerConfiguration.builder().setStartUrl(entity.getUrl()).setMaxLevels(entity.getDepth()).build();
		try {
			startCrawler(config);
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(ucBuilder.path("/url").build().toUri());
			return new ResponseEntity<String>(headers, HttpStatus.CREATED);
		} catch (URISyntaxException e) {
			return new ResponseEntity(new ErrorEntity(e.getMessage(), e.getReason()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return new ResponseEntity(new ErrorEntity(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * check if request parameter is correct.
	 * 
	 * @param entity
	 * @param eEntity
	 * @return
	 */
	private boolean check(String url, ErrorEntity eEntity) {
		if (url == null || url.isEmpty()) {
			eEntity.setMsg("request url can not be null!");
			return false;
		}
		return true;
	}

	/**
	 * Take a URL and returns the HTTP status code and timestamp it was fetched, or that it wasn't visited
	 * 
	 * @param entity
	 *            UrlEntity object.
	 * @return 1. 200 UrlEntity 2. 204 UrlEntity
	 */
	@RequestMapping(value = "/urls/one", method = RequestMethod.POST)
	public ResponseEntity<?> searchOne(@RequestBody ResponseUrlEntity entity) {
		logger.info("entity's url : {} ", entity.getUrl());
		ErrorEntity eEntity = new ErrorEntity();
		if (!check(entity.getUrl(), eEntity)) {
			return new ResponseEntity<ErrorEntity>(eEntity, HttpStatus.BAD_REQUEST);
		}

		fillEntity(entity);
		return new ResponseEntity<ResponseUrlEntity>(entity, HttpStatus.OK);

	}

	/**
	 * Take URLs and returns the HTTP status code and timestamp they were fetched, or they were not visited
	 * 
	 * @param entity
	 *            UrlEntity object.
	 * @return 1. 200 UrlEntity 2. 204 UrlEntity 3. 400 Bad Request
	 */
	@RequestMapping(value = "/urls/mult", method = RequestMethod.POST)
	public ResponseEntity<?> searchMult(@RequestBody List<ResponseUrlEntity> entities) {

		if (entities == null || entities.size() == 0) {
			return new ResponseEntity<ErrorEntity>(new ErrorEntity("request body can not be null"), HttpStatus.BAD_REQUEST);
		}

		ErrorEntity eEntity = new ErrorEntity();
		for (ResponseUrlEntity entity : entities) {
			if (!check(entity.getUrl(), eEntity)) {
				return new ResponseEntity<ErrorEntity>(eEntity, HttpStatus.BAD_REQUEST);
			}
		}

		entities.forEach(entity -> {
			logger.info("entity's url : {} ", entity.getUrl());
			fillEntity(entity);
		});
		return new ResponseEntity<List<ResponseUrlEntity>>(entities, HttpStatus.OK);
	}

	/**
	 * start crawler.
	 * 
	 * @param config
	 * @return all the processed urls.
	 * @throws Exception
	 * @throws URISyntaxException
	 */
	public Set<CrawlerURL> startCrawler(CrawlerConfiguration config) throws URISyntaxException, Exception {
		HttpClient httpClient = new HttpClient(new SslContextFactory());
		HTMLPageResponseFetcher fetcher = new JettyClientResponseFetcher(new HashMap<>(), httpClient);
		Crawler crawler = new JettyCrawler(fetcher, new AhrefPageURLParser(), service);
		return crawler.doProcess(config);
	}

	/**
	 * fill the response entity from retrieving redis.
	 * 
	 * @param entity
	 */
	public void fillEntity(ResponseUrlEntity entity) {
		RedisUrl rUrl = service.getByUrl(entity.getUrl());
		if (rUrl == null) {
			entity.setHttpCode(org.eclipse.jetty.http.HttpStatus.NO_CONTENT_204);
			entity.setUrl(org.eclipse.jetty.http.HttpStatus.Code.NO_CONTENT.getMessage());
			return;
		}
		entity.setHttpCode(rUrl.getHttpCode());
		entity.setTimestamp(DateUtils.fromUnixTimeStr(rUrl.getTimestamp()));
	}
}
