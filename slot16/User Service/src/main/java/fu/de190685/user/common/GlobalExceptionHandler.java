package fu.de190685.user.common;

import fu.de190685.user.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse> handleApiException(ApiException exception) {
		return ResponseEntity.status(exception.getHttpStatus())
				.body(ApiResponse.error(exception.getResponseStatus(), exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse("Validation failed");
		return validationFailed(message);
	}

	@ExceptionHandler({
			ConstraintViolationException.class,
			MethodArgumentTypeMismatchException.class,
			HttpMessageNotReadableException.class
	})
	public ResponseEntity<ApiResponse> handleInvalidInput(Exception exception) {
		return validationFailed("Validation failed: " + exception.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse> handleUnexpectedException(Exception exception) {
		LOGGER.error("Unexpected error while processing a user-service request", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error(0, "Server error"));
	}

	private ResponseEntity<ApiResponse> validationFailed(String message) {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ApiResponse.error(2, message));
	}
}
