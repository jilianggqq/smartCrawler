package gqq.importio.crawler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpFields;

import com.soulgalore.crawler.util.StatusCode;

public class JettyClientResponseFetcher implements HTMLPageResponseFetcher {

	// the http responses result.
	private Set<HTMLPageResponse> responses;

	private HttpClient httpClient;
	
	final CountDownLatch latch;

	public JettyClientResponseFetcher() {
		this.responses = new HashSet<>();
		this.httpClient = new HttpClient();
	}

	public Set<HTMLPageResponse> getResponses() {
		return this.responses;
	}

	public static void main(String[] args) {
		JettyClientResponseFetcher test = new JettyClientResponseFetcher();
		Set<CrawlerURL> urls = new HashSet<>();
		urls.add(new CrawlerURL("http://www.mkyong.com/"));
		urls.add(new CrawlerURL("http://www.google.com/"));
		System.out.println("starting.....");

		test.get(urls, new HashMap<>());
		for (HTMLPageResponse rs : test.responses) {
			System.out.println(rs.getPageUrl());
			System.out.println(rs.getBody());

		}
	}

	public void getResponse(CrawlerURL url, Map<String, String> requestHeaders) {
		if (url.isWrongSyntax()) {
			HTMLPageResponse htmlPageResponse = new HTMLPageResponse(url, StatusCode.SC_MALFORMED_URI.getCode(),
					Collections.<String, String>emptyMap(), "", "", 0, "", 0);
			responses.add(htmlPageResponse);
			latch.countDown();
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
					String encoding = getEncoding();
					String body = getContentAsString(encoding);
					int size = body.length();
					HTMLPageResponse hpresponse = new HTMLPageResponse(url, result.getResponse().getStatus(), headersAndValues, body, encoding, size,
							getMediaType(), 0);
					responses.add(hpresponse);
					latch.countDown();
				}

				@Override
				public void onFailure(Response response, Throwable failure) {
					// TODO Auto-generated method stub
					super.onFailure(response, failure);
					latch.countDown();
				}

			});

		} catch (Exception e) {
			System.err.println(e);
			HTMLPageResponse htmlPageResponse = new HTMLPageResponse(url, StatusCode.SC_SERVER_RESPONSE_UNKNOWN.getCode(),
					Collections.<String, String>emptyMap(), "", "", 0, "", -1);
			responses.add(htmlPageResponse);
			latch.countDown();
		}
	}

	@Override
	public void get(Set<CrawlerURL> urls, Map<String, String> requestHeaders) {
		try {
			// start http client
			startup();
			
			for (CrawlerURL crawlerURL : urls) {
				getResponse(crawlerURL, requestHeaders, latch, httpClient);
			}
			latch.await();
			// end http client
			shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		if (httpClient.isStarted()) {
			try {
				httpClient.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void startup() {
		try {
			httpClient.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
