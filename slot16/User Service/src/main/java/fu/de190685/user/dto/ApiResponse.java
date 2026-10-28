package fu.de190685.user.dto;

import java.time.Instant;

public class ApiResponse {

	private int status;
	private String message;
	private Object data;
	private String timestamp;

	public ApiResponse() {
	}

	public ApiResponse(int status, String message, Object data) {
		this.status = status;
		this.message = message;
		this.data = data;
		this.timestamp = Instant.now().toString();
	}

	public static ApiResponse success(String message, Object data) {
		return new ApiResponse(1, message, data);
	}

	public static ApiResponse error(int status, String message) {
		return new ApiResponse(status, message, null);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
