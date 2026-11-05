package com.mss301.watch.service.saga;

public enum SagaStatus {
    PENDING,
    EXECUTING,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
