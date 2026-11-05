package com.mss301.watch.service.saga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SagaStepTest {

    @Test
    void regularStepContainsActionAndCompensation() {
        Runnable action = () -> { };
        Runnable compensation = () -> { };

        SagaStep step = SagaStep.of("ASSIGN_BRAND", action, "UNASSIGN_BRAND", compensation);

        assertSame(action, step.action());
        assertSame(compensation, step.compensation());
        assertFalse(step.isPivot());
    }

    @Test
    void pivotHasNoCompensation() {
        Runnable action = () -> { };

        SagaStep step = SagaStep.pivot("DELETE_WATCH", action);

        assertSame(action, step.action());
        assertNull(step.compensationName());
        assertNull(step.compensation());
        assertTrue(step.isPivot());
    }
}
