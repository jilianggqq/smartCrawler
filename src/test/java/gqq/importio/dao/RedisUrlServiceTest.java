package gqq.importio.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gqq.importio.dao.model.RedisUrl;
import gqq.importio.dao.service.RedisUrlService;
import gqq.importio.util.DateUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RedisUrlServiceTest {
	final Logger logger = LoggerFactory.getLogger(RedisUrlServiceTest.class);
	private static final String URI = "http://www.mkyong.com";
	private static final int HTTPCODE = HttpStatus.OK_200;
	private static final int HTTPCODE2 = HttpStatus.BAD_REQUEST_400;

	private static final long TIME = DateUtils.toUnixTime(DateUtils.getDateNow());

	@Before
	public void setUp() throws Exception {

	}

	@Autowired
	RedisUrlService service;

	@Test
	public void testService() throws Exception {
		// given
		RedisUrl url = new RedisUrl();
//		url.getHttpCode()
		url.setHttpCode(HTTPCODE);
		url.setTimestamp(TIME);
		url.setUrl(URI);

		if (service.exists(URI)) {
			logger.info("the url with id [{}] exists!", URI);
			url.setHttpCode(HTTPCODE2);
			service.saveOrUpdate(url);
		} else {
			logger.info("insert url with [{}]", URI);
			// when
			service.saveOrUpdate(url);

			// then
			RedisUrl result = service.getByUrl(url.getUrl());
			assertEquals(HTTPCODE, result.getHttpCode());
			assertEquals(URI, result.getUrl());
			assertEquals(TIME, url.getTimestamp());
		}
	}
}
