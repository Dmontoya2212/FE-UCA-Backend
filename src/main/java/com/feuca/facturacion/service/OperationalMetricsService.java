package com.feuca.facturacion.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class OperationalMetricsService {

    private final MeterRegistry meterRegistry;

    public OperationalMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordAuthenticationFailure(String reason) {
        Counter.builder("facturacion.auth.failures")
                .tag("reason", tag(reason))
                .description("Fallos de autenticacion.")
                .register(meterRegistry)
                .increment();
    }

    public void recordEmissionFailure(String result) {
        Counter.builder("facturacion.emision.failures")
                .tag("result", tag(result))
                .description("Fallos o rechazos durante la emision DTE.")
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startHaciendaTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopHaciendaTimer(Timer.Sample sample, String result) {
        if (sample == null) {
            return;
        }
        sample.stop(Timer.builder("facturacion.hacienda.latency")
                .tag("result", tag(result))
                .description("Latencia de la llamada de envio a Hacienda.")
                .register(meterRegistry));
    }

    private String tag(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}
