package com.mss301.watch.config;

import feign.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {

    @Test
    void configuresBasicFeignLogging() {
        assertEquals(Logger.Level.BASIC, new AppConfig().feignLoggerLevel());
    }
}
