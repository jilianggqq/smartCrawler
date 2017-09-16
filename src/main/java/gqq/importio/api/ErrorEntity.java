package gqq.importio.api;

/**
 * POJO of error entity
 * @author gqq
 *
 */
public class ErrorEntity {
	private String msg;

	public ErrorEntity(String msg, String stack) {
		this.msg = msg;
		this.stack = stack;
	}

	public ErrorEntity(String message) {
		this(message, "");
	}

	public ErrorEntity() {
		this("");
	}

	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getStackTrace() {
		return stack;
	}

	private String stack;
}
