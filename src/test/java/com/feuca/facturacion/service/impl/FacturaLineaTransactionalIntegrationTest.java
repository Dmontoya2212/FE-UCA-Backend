package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.FacturaTotalsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.expiration-ms=900000",
        "jwt.issuer=feuca-test"
})
@ActiveProfiles("test")
class FacturaLineaTransactionalIntegrationTest {

    @Autowired
    private FacturaLineaServiceImpl facturaLineaService;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private FacturaLineaRepository facturaLineaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private AccessControlService accessControlService;

    @MockitoBean
    private FacturaTotalsService facturaTotalsService;

    private UUID facturaId;
    private UUID lineaId;

    @AfterEach
    void cleanup() {
        if (lineaId != null) {
            jdbcTemplate.update("DELETE FROM factura_lineas WHERE id = ?", lineaId);
        }
        if (facturaId != null) {
            jdbcTemplate.update("DELETE FROM facturas WHERE id = ?", facturaId);
        }
    }

    @Test
    void updateRollsBackLineWhenInvoiceTotalsRecalculationFails() {
        UUID empresaId = jdbcTemplate.queryForObject("SELECT id FROM empresas LIMIT 1", UUID.class);
        facturaId = UUID.randomUUID();
        lineaId = UUID.randomUUID();

        jdbcTemplate.update("""
                        INSERT INTO facturas (
                            id, empresa_id, numero, fecha_emision, fecha_vencimiento, estado, moneda_codigo,
                            subtotal_sin_iva, total_iva, total_con_iva, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, CAST(? AS invoice_status), ?, ?, ?, ?, ?, ?)
                        """,
                facturaId,
                empresaId,
                "TX-" + facturaId,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                "BORRADOR",
                "USD",
                new BigDecimal("1.00000000"),
                BigDecimal.ZERO.setScale(8),
                new BigDecimal("1.00000000"),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        facturaLineaRepository.saveAndFlush(FacturaLinea.builder()
                .id(lineaId)
                .facturaId(facturaId)
                .descripcion("Servicio")
                .cantidad(new BigDecimal("1.00"))
                .precioSinIva(new BigDecimal("1.00000000"))
                .ivaPorcentaje(new BigDecimal("0.00"))
                .subtotalSinIva(new BigDecimal("1.00000000"))
                .totalIva(BigDecimal.ZERO.setScale(8))
                .totalConIva(new BigDecimal("1.00000000"))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());

        when(facturaTotalsService.recalcularTotalesFactura(facturaId))
                .thenThrow(new IllegalStateException("Fallo intermedio"));

        assertThrows(IllegalStateException.class, () -> facturaLineaService.update(
                empresaId,
                facturaId,
                lineaId,
                FacturaLineaUpdateRequest.builder()
                        .cantidad(new BigDecimal("5.00"))
                        .precioSinIva(new BigDecimal("10.00000000"))
                        .ivaPorcentaje(new BigDecimal("13.00"))
                        .build()
        ));

        FacturaLinea persisted = facturaLineaRepository.findById(lineaId).orElseThrow();
        assertEquals(new BigDecimal("1.00"), persisted.getCantidad());
        assertEquals(new BigDecimal("1.00000000"), persisted.getSubtotalSinIva());
        assertEquals(new BigDecimal("1.00000000"), persisted.getTotalConIva());
    }
}
