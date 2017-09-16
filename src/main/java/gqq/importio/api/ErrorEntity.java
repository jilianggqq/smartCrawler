package gqq.importio.api;

public class ErrorEntity {
	private String msg;

	public ErrorEntity(String msg, String stack) {
		this.msg = msg;
		this.stack = stack;
	}

	public ErrorEntity(String message) {
		this(message, "");
	}

	public String getMsg() {
		return msg;
	}

	public String getStackTrace() {
		return stack;
	}

	private String stack;
}
