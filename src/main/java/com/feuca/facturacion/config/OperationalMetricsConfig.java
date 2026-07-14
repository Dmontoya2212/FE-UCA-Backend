package com.feuca.facturacion.config;

import com.feuca.facturacion.enums.EstadoFactura;
import com.feuca.facturacion.repository.DteSecuenciaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;

@Configuration
public class OperationalMetricsConfig {

    @Bean
    MeterBinder facturacionBusinessMetrics(
            FacturaRepository facturaRepository,
            DteSecuenciaRepository dteSecuenciaRepository,
            @Value("${monitoring.invoices.sending-stale-minutes:15}") long staleMinutes
    ) {
        return registry -> {
            Gauge.builder("facturacion.facturas.enviando", facturaRepository,
                            repository -> repository.countByEstado(EstadoFactura.ENVIANDO.name()))
                    .description("Facturas actualmente en estado ENVIANDO.")
                    .register(registry);

            Gauge.builder("facturacion.facturas.enviando.stale", facturaRepository,
                            repository -> repository.countByEstadoAndUpdatedAtBefore(
                                    EstadoFactura.ENVIANDO.name(),
                                    OffsetDateTime.now().minusMinutes(staleMinutes)))
                    .description("Facturas ENVIANDO por mas tiempo que el umbral configurado.")
                    .register(registry);

            Gauge.builder("facturacion.correlativos.secuencias", dteSecuenciaRepository, DteSecuenciaRepository::count)
                    .description("Cantidad de secuencias DTE configuradas.")
                    .register(registry);

            Gauge.builder("facturacion.correlativos.consumidos", dteSecuenciaRepository,
                            repository -> {
                                Long total = repository.sumUltimoCorrelativo();
                                return total != null ? total : 0L;
                            })
                    .description("Suma de los ultimos correlativos DTE consumidos.")
                    .register(registry);
        };
    }
}
