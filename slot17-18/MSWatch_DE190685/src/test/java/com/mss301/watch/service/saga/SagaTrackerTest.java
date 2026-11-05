package com.mss301.watch.service.saga;

import com.mss301.watch.entity.SagaLog;
import com.mss301.watch.entity.SagaState;
import com.mss301.watch.repository.SagaLogRepository;
import com.mss301.watch.repository.SagaStateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SagaTrackerTest {

    private final SagaStateRepository stateRepository = mock(SagaStateRepository.class);
    private final SagaLogRepository logRepository = mock(SagaLogRepository.class);
    private final SagaTracker tracker = new SagaTracker(stateRepository, logRepository);

    @Test
    void startCreatesPendingSaga() {
        when(stateRepository.save(any(SagaState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SagaState state = tracker.start("CREATE_WATCH", 12, "{\"name\":\"watch\"}");

        assertNotNull(state.getSagaId());
        assertEquals(36, state.getSagaId().length());
        assertEquals("CREATE_WATCH", state.getSagaType());
        assertEquals(12, state.getWatchId());
        assertEquals(SagaStatus.PENDING, state.getStatus());
        assertNotNull(state.getCreatedAt());
        assertNotNull(state.getUpdatedAt());
    }

    @Test
    void failTruncatesReasonAndUpdatesSagaState() {
        SagaState state = SagaState.builder().sagaId("saga-1").build();
        when(stateRepository.findBySagaId("saga-1")).thenReturn(Optional.of(state));

        tracker.fail("saga-1", "x".repeat(1200));

        assertEquals(SagaStatus.FAILED, state.getStatus());
        assertEquals(1000, state.getFailureReason().length());
        assertNotNull(state.getUpdatedAt());
    }

    @Test
    void logTruncatesMessageBeforeSaving() {
        when(logRepository.save(any(SagaLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tracker.log("saga-1", 2, "ASSIGN_BRAND", "EXECUTE", "FAILED", "x".repeat(1200));

        verify(logRepository).save(org.mockito.ArgumentMatchers.argThat(log ->
            log.getSagaId().equals("saga-1")
                && log.getStepNo() == 2
                && log.getMessage().length() == 1000
                && log.getCreatedAt() != null
        ));
    }

    @Test
    void everyWriteMethodUsesRequiresNew() throws NoSuchMethodException {
        for (String methodName : new String[] {"start", "updateStatus", "currentStep", "attachWatch", "fail", "log"}) {
            Transactional transaction = SagaTracker.class.getMethod(methodName, parameterTypes(methodName))
                .getAnnotation(Transactional.class);
            assertNotNull(transaction, methodName);
            assertEquals(Propagation.REQUIRES_NEW, transaction.propagation(), methodName);
        }
    }

    @Test
    void updatesFailWhenSagaDoesNotExist() {
        when(stateRepository.findBySagaId("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> tracker.updateStatus("missing", SagaStatus.FAILED));
    }

    private Class<?>[] parameterTypes(String methodName) {
        return switch (methodName) {
            case "start" -> new Class<?>[] {String.class, Integer.class, String.class};
            case "updateStatus" -> new Class<?>[] {String.class, SagaStatus.class};
            case "currentStep" -> new Class<?>[] {String.class, String.class};
            case "attachWatch" -> new Class<?>[] {String.class, Integer.class};
            case "fail" -> new Class<?>[] {String.class, String.class};
            case "log" -> new Class<?>[] {
                String.class, int.class, String.class, String.class, String.class, String.class
            };
            default -> throw new IllegalArgumentException("Unexpected method: " + methodName);
        };
    }
}
