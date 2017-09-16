package gqq.importio.crawler.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpFields;

import gqq.importio.crawler.CrawlerURL;
import gqq.importio.crawler.HTMLPageResponse;
import gqq.importio.crawler.HTMLPageResponseFetcher;
import gqq.importio.util.DateUtils;
import gqq.importio.util.StatusCode;

public class JettyClientResponseFetcher extends HTMLPageResponseFetcher {

	public JettyClientResponseFetcher(Map<String, String> requestHeaders, HttpClient httpClient) {
		super(requestHeaders, httpClient);
	}

	/**
	 * get the response of every url.
	 * 
	 * @param url
	 */
	public void getResponse(CrawlerURL url) {
		if (url.isWrongSyntax()) {
			HTMLPageResponse htmlPageResponse = new HTMLPageResponse(url, StatusCode.SC_MALFORMED_URI.getCode(),
					Collections.<String, String>emptyMap(), "", "", 0, "", 0);
			errorResponses.add(htmlPageResponse);
			latch.countDown();
			return;
		}
		final Map<String, String> headersAndValues = new HashMap<>();

		try {
			Request newRequest = httpClient.newRequest(url.getUrl());
			for (String key : requestHeaders.keySet()) {
				// get.setHeader(key, requestHeaders.get(key));
				newRequest.header(key, requestHeaders.get(key));
			}
			newRequest.onResponseHeaders(response -> {
				HttpFields headers = response.getHeaders();
				for (String name : headers.getFieldNamesCollection()) {
					headersAndValues.put(name, headers.getValuesList(name).get(0));
				}

			}).send(new BufferingResponseListener() {

				@Override
				public void onComplete(Result result) {
					try {
						if (result.isFailed()) return;
						
						if (!getMediaType().equals("text/html"))
							return;
						String encoding = getEncoding();
						String body = getContentAsString(encoding);
						if (body != null) {
							// System.out.println(body);
							int size = body.length();
							HTMLPageResponse hpresponse = new HTMLPageResponse(url, result.getResponse().getStatus(), headersAndValues, body,
									encoding, size, getMediaType(), DateUtils.toUnixTime(DateUtils.getDateTimeNow()));
							correctResponses.add(hpresponse);
						} else {
							logger.info("body is null, url is " + url.toString());
						}
					} catch (IllegalArgumentException e) {
						logger.error("******************IllegalArgumentException***********************");
						logger.error(e.getMessage());
						logger.info("body is null, url is " + url.toString() + " encoding is " + getEncoding());
					} finally {
						latch.countDown();
					}
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			HTMLPageResponse htmlPageResponse = new HTMLPageResponse(url, StatusCode.SC_SERVER_RESPONSE_UNKNOWN.getCode(),
					Collections.<String, String>emptyMap(), "", "", 0, "", -1);
			errorResponses.add(htmlPageResponse);
			latch.countDown();
		}
	}

	@Override
	public void processing() throws Exception {
		super.processing();
		for (CrawlerURL crawlerURL : getUrls()) {
			getResponse(crawlerURL);
		}
		latch.await();
	}

}
