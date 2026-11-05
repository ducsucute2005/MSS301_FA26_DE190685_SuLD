package com.mss301.watch.dto;

import com.mss301.watch.entity.SagaLog;
import com.mss301.watch.entity.SagaState;
import com.mss301.watch.service.saga.SagaStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SagaResponse(
    String sagaId,
    String sagaType,
    Integer watchId,
    SagaStatus status,
    String currentStep,
    String failureReason,
    String payload,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<StepLog> steps
) {

    public record StepLog(
        Integer stepNo,
        String stepName,
        String phase,
        String result,
        String message,
        LocalDateTime at
    ) {
    }

    public static SagaResponse of(SagaState state, List<SagaLog> logs) {
        List<StepLog> steps = logs == null ? null : logs.stream()
            .map(log -> new StepLog(
                log.getStepNo(),
                log.getStepName(),
                log.getPhase(),
                log.getResult(),
                log.getMessage(),
                log.getCreatedAt()
            ))
            .toList();
        return new SagaResponse(
            state.getSagaId(),
            state.getSagaType(),
            state.getWatchId(),
            state.getStatus(),
            state.getCurrentStep(),
            state.getFailureReason(),
            state.getPayload(),
            state.getCreatedAt(),
            state.getUpdatedAt(),
            steps
        );
    }
}
