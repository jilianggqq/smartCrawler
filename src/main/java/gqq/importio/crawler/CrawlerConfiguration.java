package gqq.importio.crawler;

import java.util.Collections;
import java.util.Map;

import gqq.importio.crawler.util.HeaderUtil;

/**
 * Configuration for a crawl.
 * 
 */
public final class CrawlerConfiguration {

	/**
	 * The default crawl level if no is supplied.
	 */
	public static final int DEFAULT_CRAWL_LEVEL = 2;

	private int maxLevels = DEFAULT_CRAWL_LEVEL;

	private String requestHeaders = "";

	private String startUrl;
	private Map<String, String> requestHeadersMap = Collections.emptyMap();

	private CrawlerConfiguration() {

	}

	public String getRequestHeaders() {
		return requestHeaders;
	}

	public Map<String, String> getRequestHeadersMap() {
		return requestHeadersMap;
	}

	public int getMaxLevels() {
		return maxLevels;
	}

	public String getStartUrl() {
		return startUrl;
	}

	private CrawlerConfiguration copy() {
		final CrawlerConfiguration conf = new CrawlerConfiguration();
		conf.setMaxLevels(getMaxLevels());
		conf.setStartUrl(getStartUrl());
		conf.setRequestHeaders(getRequestHeaders());
		return conf;

	}

	private void setRequestHeaders(String requestHeaders) {
		this.requestHeaders = requestHeaders;
		requestHeadersMap = HeaderUtil.getInstance().createHeadersFromString(requestHeaders);
	}

	private void setMaxLevels(int maxLevels) {
		this.maxLevels = maxLevels;
	}

	private void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + maxLevels;
		result = prime * result + ((startUrl == null) ? 0 : startUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CrawlerConfiguration other = (CrawlerConfiguration) obj;
		if (maxLevels != other.maxLevels)
			return false;

		if (startUrl == null) {
			if (other.startUrl != null)
				return false;
		} else if (!startUrl.equals(other.startUrl))
			return false;

		return true;
	}

	public static class Builder {
		private final CrawlerConfiguration configuration = new CrawlerConfiguration();

		public Builder() {
		}

		public CrawlerConfiguration build() {
			return configuration.copy();
		}

		public Builder setMaxLevels(int maxLevels) {
			configuration.setMaxLevels(maxLevels);
			return this;
		}

		public Builder setStartUrl(String startUrl) {
			configuration.setStartUrl(startUrl);
			return this;
		}

		public Builder setRequestHeaders(String requestHeaders) {
			configuration.setRequestHeaders(requestHeaders);
			return this;
		}
	}

	public static Builder builder() {
		return new Builder();
	}

}
