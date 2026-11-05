package com.mss301.watch.service.saga;

import com.mss301.watch.entity.SagaLog;
import com.mss301.watch.entity.SagaState;
import com.mss301.watch.repository.SagaLogRepository;
import com.mss301.watch.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaTracker {

    private final SagaStateRepository stateRepository;
    private final SagaLogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SagaState start(String sagaType, Integer watchId, String payload) {
        LocalDateTime now = LocalDateTime.now();
        return stateRepository.save(SagaState.builder()
            .sagaId(UUID.randomUUID().toString())
            .sagaType(sagaType)
            .watchId(watchId)
            .status(SagaStatus.PENDING)
            .payload(payload)
            .createdAt(now)
            .updatedAt(now)
            .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(String sagaId, SagaStatus status) {
        SagaState state = get(sagaId);
        state.setStatus(status);
        state.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void currentStep(String sagaId, String stepName) {
        SagaState state = get(sagaId);
        state.setCurrentStep(stepName);
        state.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void attachWatch(String sagaId, Integer watchId) {
        get(sagaId).setWatchId(watchId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(String sagaId, String reason) {
        SagaState state = get(sagaId);
        state.setStatus(SagaStatus.FAILED);
        state.setFailureReason(truncate(reason));
        state.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String sagaId, int stepNo, String stepName, String phase, String result, String message) {
        logRepository.save(SagaLog.builder()
            .sagaId(sagaId)
            .stepNo(stepNo)
            .stepName(stepName)
            .phase(phase)
            .result(result)
            .message(truncate(message))
            .createdAt(LocalDateTime.now())
            .build());
    }

    private SagaState get(String sagaId) {
        return stateRepository.findBySagaId(sagaId)
            .orElseThrow(() -> new IllegalStateException("Saga not found: " + sagaId));
    }

    private String truncate(String value) {
        return value == null || value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
