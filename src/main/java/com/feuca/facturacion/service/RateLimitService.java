package com.feuca.facturacion.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitService {

    private static final long CLEANUP_INTERVAL = 256;
    private static final int MAX_BUCKETS = 100_000;

    private final Clock clock;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong requestCount = new AtomicLong();

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
        cleanupExpiredBuckets(now);
        if (!buckets.containsKey(key) && buckets.size() >= MAX_BUCKETS) {
            return false;
        }
        AtomicBoolean allowed = new AtomicBoolean(false);

        buckets.compute(key, (ignored, current) -> {
            if (current == null || !now.isBefore(current.expiresAt)) {
                allowed.set(true);
                return new Bucket(now.plus(window), 1);
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

    private void cleanupExpiredBuckets(Instant now) {
        if (requestCount.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            buckets.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().expiresAt));
        }
    }

    private static final class Bucket {
        private final Instant expiresAt;
        private int count;

        private Bucket(Instant expiresAt, int count) {
            this.expiresAt = expiresAt;
            this.count = count;
        }
    }
}
