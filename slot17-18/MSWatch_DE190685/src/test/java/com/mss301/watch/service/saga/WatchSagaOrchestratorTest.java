package com.mss301.watch.service.saga;

import com.mss301.watch.client.BrandServiceClient;
import com.mss301.watch.service.WatchData;
import com.mss301.watch.service.WatchLocalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WatchSagaOrchestratorTest {

    private final WatchLocalService localService = mock(WatchLocalService.class);
    private final BrandServiceClient brandClient = mock(BrandServiceClient.class);
    private final SagaTracker tracker = mock(SagaTracker.class);
    private final SagaFaultInjector faultInjector = new SagaFaultInjector();
    private final JsonMapper jsonMapper = mock(JsonMapper.class);
    private WatchSagaOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(faultInjector, "enabled", true);
        orchestrator = new WatchSagaOrchestrator(localService, brandClient, tracker, faultInjector, jsonMapper);
        ReflectionTestUtils.setField(orchestrator, "maxAttempts", 3);
        ReflectionTestUtils.setField(orchestrator, "backoffMs", 0L);
        when(tracker.start(any(), any(), any())).thenAnswer(invocation ->
            com.mss301.watch.entity.SagaState.builder().sagaId("saga-123").build());
        when(jsonMapper.writeValueAsString(any())).thenReturn("{}");
    }

    @Test
    void createSagaCompensatesCommittedStepsInReverseOrderAfterInjectedFailure() {
        when(localService.insert(any())).thenReturn(41);

        SagaExecutionException exception = assertThrows(SagaExecutionException.class,
            () -> orchestrator.createWatch(sampleData(null), 2));

        assertEquals(SagaStatus.COMPENSATED, exception.getSagaStatus());
        assertEquals("FAULT_INJECTION after ASSIGN_BRAND", exception.getFailedStep());
        var order = inOrder(brandClient, localService);
        order.verify(brandClient).unassignWatch(41);
        order.verify(localService).deleteIfExists(41);
        verify(tracker).updateStatus("saga-123", SagaStatus.COMPENSATED);
    }

    @Test
    void failedCompensationRetriesAndLeavesSagaCompensating() {
        when(localService.insert(any())).thenReturn(41);
        org.mockito.Mockito.doThrow(new IllegalStateException("brand unavailable"))
            .when(brandClient).unassignWatch(41);

        SagaExecutionException exception = assertThrows(SagaExecutionException.class,
            () -> orchestrator.createWatch(sampleData(null), 2));

        assertEquals(SagaStatus.COMPENSATING, exception.getSagaStatus());
        verify(brandClient, org.mockito.Mockito.times(3)).unassignWatch(41);
        verify(localService, never()).deleteIfExists(41);
        verify(tracker, org.mockito.Mockito.times(2)).updateStatus("saga-123", SagaStatus.COMPENSATING);
    }

    @Test
    void injectedFailureAfterDeletePivotIsSkipped() {
        WatchData before = sampleData(41);

        SagaOutcome result = orchestrator.deleteWatch(before, 2);

        assertEquals(SagaStatus.COMPLETED, result.status());
        verify(localService).delete(41);
        verify(brandClient, never()).assignWatch(1, 41);
        verify(tracker).log("saga-123", 2, "FAULT_INJECTION", "FAULT", "SKIPPED",
            "Ignored: step 2 (DELETE_WATCH) is the pivot, saga cannot be rolled back");
    }

    private WatchData sampleData(Integer watchId) {
        return new WatchData(watchId, "A Sample Watch", 2, 1, "Steel", "Leather", "50 meters",
            new java.math.BigDecimal("1000.00"), java.time.LocalDate.of(2026, 11, 5), 10);
    }
}
