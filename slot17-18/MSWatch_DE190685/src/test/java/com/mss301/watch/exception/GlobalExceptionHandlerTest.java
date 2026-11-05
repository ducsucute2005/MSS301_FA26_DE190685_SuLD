package com.mss301.watch.exception;

import com.mss301.watch.dto.ApiResponse;
import com.mss301.watch.service.saga.SagaExecutionException;
import com.mss301.watch.service.saga.SagaStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void sagaFailureResponseIncludesTrackingDetailsAndHeader() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        SagaExecutionException exception = new SagaExecutionException(
            "saga-123", SagaStatus.COMPENSATED, "ASSIGN_BRAND", HttpStatus.BAD_REQUEST,
            "MSBrand returned 404: brand not found", new IllegalStateException("not found")
        );

        ResponseEntity<ApiResponse<Map<String, Object>>> response = handler.handleSaga(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("saga-123", response.getHeaders().getFirst("X-Saga-Id"));
        assertEquals("saga-123", response.getBody().data().get("sagaId"));
        assertEquals(SagaStatus.COMPENSATED, response.getBody().data().get("sagaStatus"));
        assertEquals("ASSIGN_BRAND", response.getBody().data().get("failedStep"));
    }
}
