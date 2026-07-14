package com.feuca.facturacion.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationalMetricsServiceTest {

    @Test
    void recordsAuthenticationAndEmissionFailures() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OperationalMetricsService service = new OperationalMetricsService(registry);

        service.recordAuthenticationFailure("InvalidCredentialsException");
        service.recordEmissionFailure("ERROR_TECNICO");

        assertEquals(1.0, registry.counter("facturacion.auth.failures", "reason", "InvalidCredentialsException").count());
        assertEquals(1.0, registry.counter("facturacion.emision.failures", "result", "ERROR_TECNICO").count());
    }
}
