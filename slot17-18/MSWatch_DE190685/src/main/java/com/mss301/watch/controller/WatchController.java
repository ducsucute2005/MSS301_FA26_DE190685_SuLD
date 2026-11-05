package com.mss301.watch.controller;

import com.mss301.watch.dto.ApiResponse;
import com.mss301.watch.dto.WatchRequest;
import com.mss301.watch.dto.WatchResponse;
import com.mss301.watch.service.SagaResult;
import com.mss301.watch.service.WatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/watches")
@RequiredArgsConstructor
@Tag(name = "Watch", description = "Watch management with Saga Pattern")
public class WatchController {

    public static final String FAIL_AFTER_HEADER = "X-Saga-Fail-After-Step";
    public static final String SAGA_ID_HEADER = "X-Saga-Id";

    private final WatchService watchService;

    @GetMapping
    @Operation(summary = "List all watches (public, newest first)")
    public ResponseEntity<ApiResponse<List<WatchResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(watchService.findAll(), "Success"));
    }

    @PostMapping
    @Operation(summary = "Add a new watch (Admin only)")
    public ResponseEntity<ApiResponse<WatchResponse>> create(
        @RequestHeader(value = "X-User-Role", required = false) String role,
        @RequestHeader(value = FAIL_AFTER_HEADER, required = false) Integer failAfterStep,
        @Valid @RequestBody WatchRequest request
    ) {
        checkAdminRole(role);
        SagaResult<WatchResponse> result = watchService.create(request, failAfterStep);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(SAGA_ID_HEADER, result.sagaId())
            .body(ApiResponse.success(result.body(),
                "Watch created successfully (saga " + result.sagaId() + " COMPLETED)"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a watch (Admin only)")
    public ResponseEntity<ApiResponse<WatchResponse>> update(
        @RequestHeader(value = "X-User-Role", required = false) String role,
        @RequestHeader(value = FAIL_AFTER_HEADER, required = false) Integer failAfterStep,
        @PathVariable Integer id,
        @Valid @RequestBody WatchRequest request
    ) {
        checkAdminRole(role);
        SagaResult<WatchResponse> result = watchService.update(id, request, failAfterStep);
        return ResponseEntity.ok()
            .header(SAGA_ID_HEADER, result.sagaId())
            .body(ApiResponse.success(result.body(),
                "Watch updated successfully (saga " + result.sagaId() + " COMPLETED)"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a watch (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(
        @RequestHeader(value = "X-User-Role", required = false) String role,
        @RequestHeader(value = FAIL_AFTER_HEADER, required = false) Integer failAfterStep,
        @PathVariable Integer id
    ) {
        checkAdminRole(role);
        SagaResult<Void> result = watchService.delete(id, failAfterStep);
        return ResponseEntity.ok()
            .header(SAGA_ID_HEADER, result.sagaId())
            .body(ApiResponse.success(null,
                "Watch deleted successfully (saga " + result.sagaId() + " COMPLETED)"));
    }

    static void checkAdminRole(String role) {
        if (!"1".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Only Administrator (role=1) can perform this action");
        }
    }
}
