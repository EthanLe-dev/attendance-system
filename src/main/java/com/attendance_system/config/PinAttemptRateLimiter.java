package com.attendance_system.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Limits failed dashboard-PIN attempts per client IP to slow brute-force attacks. */
@Component
public class PinAttemptRateLimiter {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(HttpServletRequest request) {
        AttemptWindow window = attempts.get(clientIp(request));
        return window != null && !window.isExpired() && window.failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    public void recordFailure(HttpServletRequest request) {
        String clientIp = clientIp(request);
        attempts.compute(clientIp, (ignored, current) -> {
            if (current == null || current.isExpired()) {
                return new AttemptWindow(1, Instant.now());
            }
            return new AttemptWindow(current.failedAttempts + 1, current.startedAt);
        });
    }

    public void reset(HttpServletRequest request) {
        attempts.remove(clientIp(request));
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private record AttemptWindow(int failedAttempts, Instant startedAt) {
        boolean isExpired() {
            return startedAt.plus(WINDOW).isBefore(Instant.now());
        }
    }
}
