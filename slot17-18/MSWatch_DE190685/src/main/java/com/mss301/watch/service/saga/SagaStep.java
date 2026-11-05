package com.mss301.watch.service.saga;

public record SagaStep(String name, Runnable action, String compensationName, Runnable compensation) {

    public static SagaStep of(String name, Runnable action, String compensationName, Runnable compensation) {
        return new SagaStep(name, action, compensationName, compensation);
    }

    public static SagaStep pivot(String name, Runnable action) {
        return new SagaStep(name, action, null, null);
    }

    public boolean isPivot() {
        return compensation == null;
    }
}
