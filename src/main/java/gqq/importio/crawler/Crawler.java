/******************************************************
 * Web crawler
 * 
 * 
 * 
 ******************************************************* 
 */
package gqq.importio.crawler;

import java.util.List;
import java.util.Set;

import gqq.importio.dao.model.RedisUrl;
import gqq.importio.dao.service.RedisUrlService;

/**
 * Interface of a web crawler.
 * 
 */
public interface Crawler {

	/**
	 * Shutdown the crawler.
	 */
	void shutdown();

	/**
	 * start crawler by the configuration and install the result into database.
	 * 
	 * @param configuration
	 */
	void doProcess(CrawlerConfiguration configuration);

	/**
	 * start the crawler.
	 */
	void startup();

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

	void setService(RedisUrlService service);
}
