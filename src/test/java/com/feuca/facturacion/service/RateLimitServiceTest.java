package com.feuca.facturacion.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @Test
    void blocksRequestsAfterConfiguredAttemptsWithinWindow() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimitService service = new RateLimitService(clock);

        assertTrue(service.allow("login:127.0.0.1", 2, Duration.ofMinutes(5)));
        assertTrue(service.allow("login:127.0.0.1", 2, Duration.ofMinutes(5)));
        assertFalse(service.allow("login:127.0.0.1", 2, Duration.ofMinutes(5)));
    }

    @Test
    void resetsAttemptsWhenWindowExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimitService service = new RateLimitService(clock);

        assertTrue(service.allow("emision:factura", 1, Duration.ofSeconds(60)));
        assertFalse(service.allow("emision:factura", 1, Duration.ofSeconds(60)));

        clock.setInstant(Instant.parse("2026-01-01T00:01:01Z"));

        assertTrue(service.allow("emision:factura", 1, Duration.ofSeconds(60)));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
