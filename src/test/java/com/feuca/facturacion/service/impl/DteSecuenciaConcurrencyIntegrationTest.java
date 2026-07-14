package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.service.DteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.expiration-ms=900000",
        "jwt.issuer=feuca-test"
})
@ActiveProfiles("test")
class DteSecuenciaConcurrencyIntegrationTest {

    @Autowired
    private DteService dteService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID empresaId;

    @AfterEach
    void cleanup() {
        if (empresaId != null) {
            jdbcTemplate.update("DELETE FROM dte_secuencias WHERE empresa_id = ?", empresaId);
            jdbcTemplate.update("DELETE FROM empresas WHERE id = ?", empresaId);
        }
    }

    @Test
    void concurrentCodigoGenerationUsesSingleLockedSequence() throws Exception {
        empresaId = UUID.randomUUID();
        jdbcTemplate.update("""
                        INSERT INTO empresas (
                            id, razon_social, nombre_legal, nombre_comercial, nit, email, telefono,
                            direccion, actividad_economica, ciudad, pais, cod_establecimiento,
                            cod_punto_venta, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                        """,
                empresaId,
                "Empresa Concurrencia",
                "Empresa Concurrencia Legal",
                "Concurrencia",
                "NIT-" + empresaId,
                "concurrencia-" + empresaId + "@example.com",
                "TEL-" + empresaId.toString().substring(0, 12),
                "Direccion de prueba",
                "Servicios",
                "San Salvador",
                "El Salvador",
                "M001",
                "P001"
        );

        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<String>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(generateNumeroControl(start)));
            }

            start.countDown();

            List<String> numerosControl = new ArrayList<>();
            for (Future<String> future : futures) {
                numerosControl.add(future.get(20, TimeUnit.SECONDS));
            }

            assertEquals(threads, Set.copyOf(numerosControl).size());
            List<Long> correlativos = numerosControl.stream()
                    .map(this::correlativo)
                    .sorted(Comparator.naturalOrder())
                    .toList();
            assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L), correlativos);

            Long ultimoCorrelativo = jdbcTemplate.queryForObject(
                    "SELECT ultimo_correlativo FROM dte_secuencias WHERE empresa_id = ? AND tipo_dte = '01'",
                    Long.class,
                    empresaId
            );
            Integer secuencias = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM dte_secuencias WHERE empresa_id = ? AND tipo_dte = '01'",
                    Integer.class,
                    empresaId
            );
            assertEquals(8L, ultimoCorrelativo);
            assertEquals(1, secuencias);
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(20, TimeUnit.SECONDS));
        }
    }

    private Callable<String> generateNumeroControl(CountDownLatch start) {
        return () -> {
            start.await(10, TimeUnit.SECONDS);
            Factura factura = Factura.builder()
                    .empresaId(empresaId)
                    .tipoDte("01")
                    .build();

            dteService.asignarCodigos(factura);

            return factura.getNumeroControl();
        };
    }

    private Long correlativo(String numeroControl) {
        return Long.parseLong(numeroControl.substring(numeroControl.length() - 15));
    }
}
