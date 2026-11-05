package com.mss301.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/account")
    public ResponseEntity<Map<String, Object>> accountFallback() {
        return unavailable("Account Service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/brand")
    public ResponseEntity<Map<String, Object>> brandFallback() {
        return unavailable("Brand Service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/watch")
    public ResponseEntity<Map<String, Object>> watchFallback() {
        return unavailable("Watch Service is currently unavailable. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> unavailable(String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
            "success", false,
            "message", message,
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
