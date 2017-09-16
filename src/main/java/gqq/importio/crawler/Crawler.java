package gqq.importio.crawler;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import gqq.importio.dao.model.RedisUrl;

/**
 * Interface of a web crawler.
 * 
 */
public interface Crawler {

	/**
	 * shut down jetty http client
	 */
	void shutdown();

	
	/**
	 * start jetty http client.
	 */
	void startup();

	/**
	 * start crawler by the configuration and install the result into database.
	 * 
	 * @param configuration
	 * @return 
	 * @throws Exception 
	 * @throws URISyntaxException 
	 */
	Set<CrawlerURL> doProcess(CrawlerConfiguration configuration) throws URISyntaxException, Exception;

	/**
	 * save all the model into redis.
	 * @param urls
	 */
	void saveIntoRedis(List<RedisUrl> urls);

	/**
	 * get http request results from all the responses.
	 * 
	 * @param currResponses
	 * @return redis urls list
	 */
	List<RedisUrl> getModelUrls(Set<HTMLPageResponse> currResponses);

}
