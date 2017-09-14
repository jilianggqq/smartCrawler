package gqq.importio.dao;

import java.math.BigDecimal;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

import gqq.importio.dao.model.RedisUrl;
import gqq.importio.util.DateUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RedisUrlRepositoryTest {
	final Logger logger = LoggerFactory.getLogger(RedisUrlRepositoryTest.class);
	private static final String URI = "http://www.mkyong.com";
	private static final int HTTPCODE = HttpStatus.OK_200;
	
	private static final long TIME= DateUtils.toUnixTime(DateUtils.getDateNow());

	@Autowired
	private RedisUrlRepository repository;

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testPersistence() {
		// given
		RedisUrl url = new RedisUrl();
		url.setHttpCode(HTTPCODE);
		url.setTimestamp(TIME);
		url.setUrl(URI);

		if (repository.exists(URI)) {
			logger.info("the url with id [{}] exists!", URI);
		} else {
			logger.info("insert url with [{}]", URI);
			// when
			repository.save(url);
			
			// then
			RedisUrl result = repository.findOne(url.getUrl());
			assertEquals(HTTPCODE, result.getHttpCode());
			assertEquals(URI, result.getUrl());
			assertEquals(TIME, url.getTimestamp());
		}
	}
}