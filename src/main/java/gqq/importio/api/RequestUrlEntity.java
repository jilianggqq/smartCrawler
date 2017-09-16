package gqq.importio.api;

/**
 * POJO of request parameter.
 * 
 * @author gqq
 *
 */
public class RequestUrlEntity {
	private String url;

	private int depth = 2;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	@Override
	public String toString() {
		return String.format("url is [%s], depth is [%d]", url, depth);
	}
}
