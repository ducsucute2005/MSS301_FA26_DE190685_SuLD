package com.mss301.watch.controller;

import com.mss301.watch.dto.ApiResponse;
import com.mss301.watch.dto.SagaResponse;
import com.mss301.watch.entity.SagaState;
import com.mss301.watch.repository.SagaLogRepository;
import com.mss301.watch.repository.SagaStateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/sagas")
@RequiredArgsConstructor
@Tag(name = "Saga", description = "Saga state and step logs")
public class SagaController {

    private final SagaStateRepository stateRepository;
    private final SagaLogRepository logRepository;

    @GetMapping
    @Operation(summary = "List recent sagas (newest first)")
    public ResponseEntity<ApiResponse<List<SagaResponse>>> recent(
        @RequestHeader(value = "X-User-Role", required = false) String role,
        @RequestParam(defaultValue = "20") int limit
    ) {
        WatchController.checkAdminRole(role);
        int pageSize = Math.max(1, Math.min(limit, 100));
        List<SagaResponse> sagas = stateRepository.findAllByOrderByIdDesc(PageRequest.of(0, pageSize))
            .stream()
            .map(state -> SagaResponse.of(state, null))
            .toList();
        return ResponseEntity.ok(ApiResponse.success(sagas, "Success"));
    }

    @GetMapping("/{sagaId}")
    @Operation(summary = "Get saga state and ordered step logs")
    public ResponseEntity<ApiResponse<SagaResponse>> detail(
        @RequestHeader(value = "X-User-Role", required = false) String role,
        @PathVariable String sagaId
    ) {
        WatchController.checkAdminRole(role);
        SagaState state = stateRepository.findBySagaId(sagaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saga not found: " + sagaId));
        return ResponseEntity.ok(ApiResponse.success(
            SagaResponse.of(state, logRepository.findBySagaIdOrderByIdAsc(sagaId)), "Success"));
    }
}
