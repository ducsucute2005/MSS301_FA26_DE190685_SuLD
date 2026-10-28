package fu.de190685.user.common;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

	private final HttpStatus httpStatus;
	private final int responseStatus;

	public ApiException(HttpStatus httpStatus, int responseStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
		this.responseStatus = responseStatus;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public int getResponseStatus() {
		return responseStatus;
	}
}
