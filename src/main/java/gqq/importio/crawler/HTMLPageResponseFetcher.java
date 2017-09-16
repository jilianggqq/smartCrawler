/******************************************************
 * Web crawler
 * 
 * 
 * Copyright (C) 2012 by Peter Hedenskog (http://peterhedenskog.com)
 * 
 ****************************************************** 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 ******************************************************* 
 */
package gqq.importio.crawler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for the response fetchers.
 * 
 */
public abstract class HTMLPageResponseFetcher {
	protected final Logger logger;
	// the http responses result.
	protected Set<HTMLPageResponse> correctResponses;
	protected Set<HTMLPageResponse> errorResponses;

	protected final HttpClient httpClient;

	private Set<CrawlerURL> urls;

	protected final Map<String, String> requestHeaders;

	protected CountDownLatch latch;

	public HTMLPageResponseFetcher(Map<String, String> requestHeaders, HttpClient httpClient) {
		logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
		this.correctResponses = new HashSet<>();
		this.errorResponses = new HashSet<>();
		this.httpClient = httpClient;
		this.requestHeaders = requestHeaders;
	}

	public Set<HTMLPageResponse> getErrorResponses() {
		return errorResponses;
	}

	public Set<HTMLPageResponse> getResponses() {
		return this.correctResponses;
	}

	/**
	 * fetcher processing.
	 * @throws Exception
	 */
	public void processing() throws Exception {
		if (!httpClient.isStarted()) {
			throw new Exception("You should start httpClient first");
		}
		if (getUrls().isEmpty()) {
			throw new Exception("there is no url needed to be processed");
		}
		correctResponses.clear();
		errorResponses.clear();
		latch = new CountDownLatch(getUrls().size());
	}

	public void shutdown() {
		if (httpClient.isStarted()) {
			try {
				httpClient.stop();
				logger.info("http client has stopped");
			} catch (Exception e) {
				logger.error("!!! http client shut down error !!!");
				logger.error(e.getMessage());
			}
		}
	}

	public void startup() {
		try {
			httpClient.start();
			logger.info("http client is started, processing...");
		} catch (Exception e) {
			logger.error("!!! http client start up error !!!");
			logger.error(e.getMessage());
		}

	}

	/**
	 * @return the urls
	 */
	public Set<CrawlerURL> getUrls() {
		return urls;
	}

	/**
	 * @param urls the urls to set
	 */
	public void setUrls(Set<CrawlerURL> urls) {
		this.urls = urls;
	}
}
