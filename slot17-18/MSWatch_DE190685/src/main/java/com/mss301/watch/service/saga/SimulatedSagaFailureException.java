package com.mss301.watch.service.saga;

public class SimulatedSagaFailureException extends RuntimeException {

    public SimulatedSagaFailureException(String message) {
        super(message);
    }
}
