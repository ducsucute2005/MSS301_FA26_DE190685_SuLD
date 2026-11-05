package com.mss301.watch.service.saga;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SagaFaultInjector {

    @Value("${saga.fault-injection.enabled:false}")
    private boolean enabled;

    public boolean shouldFailAfter(Integer failAfterStep, int completedStepNo) {
        if (failAfterStep == null) {
            return false;
        }
        if (!enabled) {
            log.warn("Header X-Saga-Fail-After-Step={} ignored because saga.fault-injection.enabled=false",
                failAfterStep);
            return false;
        }
        return failAfterStep == completedStepNo;
    }
}
