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

import java.util.Map;
import java.util.Set;

/**
 * Interface for the response fetchers.
 * 
 */
public interface HTMLPageResponseFetcher {

	/**
	 * 
	 * @param urls
	 *            the urls need to be crawled.
	 * @param requestHeaders
	 *            http request headers.
	 * @return
	 */
	void get(Set<CrawlerURL> urls, Map<String, String> requestHeaders);

	/**
	 * shut down http client
	 */
	void shutdown();

	/**
	 * start up http client
	 */
	void startup();
}
