package fu.de190685.post.dto;

import java.time.Instant;

public class ApiResponse<T> {

	private int status;
	private String message;
	private T data;
	private String timestamp;

	public ApiResponse() {
	}

	public ApiResponse(int status, String message, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
		this.timestamp = Instant.now().toString();
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(1, message, data);
	}

	public static <T> ApiResponse<T> error(int status, String message) {
		return new ApiResponse<>(status, message, null);
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

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
