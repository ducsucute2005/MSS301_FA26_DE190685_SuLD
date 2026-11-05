package com.mss301.watch.exception;

import com.mss301.watch.dto.ApiResponse;
import com.mss301.watch.service.saga.SagaExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SagaExecutionException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleSaga(SagaExecutionException exception) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sagaId", exception.getSagaId());
        data.put("sagaStatus", exception.getSagaStatus());
        data.put("failedStep", exception.getFailedStep());
        data.put("reason", exception.getReason());
        return ResponseEntity.status(exception.getHttpStatus())
            .header("X-Saga-Id", exception.getSagaId())
            .body(new ApiResponse<>(false,
                "Saga " + exception.getSagaStatus() + ": " + exception.getReason(), data));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
            .body(ApiResponse.error(exception.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Request body is invalid"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception exception) {
        log.error("Unhandled request error", exception);
        return ResponseEntity.internalServerError().body(ApiResponse.error("Internal server error"));
    }
}
