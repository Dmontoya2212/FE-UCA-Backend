package com.feuca.facturacion.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RateLimitService {

    private final Clock clock;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitService() {
        this(Clock.systemUTC());
    }

    RateLimitService(Clock clock) {
        this.clock = clock;
    }

    public boolean allow(String key, int maxAttempts, Duration window) {
        if (key == null || key.isBlank() || maxAttempts <= 0 || window == null || window.isZero() || window.isNegative()) {
            return false;
        }

        Instant now = clock.instant();
        AtomicBoolean allowed = new AtomicBoolean(false);

        buckets.compute(key, (ignored, current) -> {
            if (current == null || !now.isBefore(current.windowStart.plus(window))) {
                allowed.set(true);
                return new Bucket(now, 1);
            }

            if (current.count >= maxAttempts) {
                allowed.set(false);
                return current;
            }

            current.count++;
            allowed.set(true);
            return current;
        });

        return allowed.get();
    }

    private static final class Bucket {
        private final Instant windowStart;
        private int count;

        private Bucket(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
