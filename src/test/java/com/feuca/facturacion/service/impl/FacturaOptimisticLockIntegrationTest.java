package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.repository.FacturaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.expiration-ms=900000",
        "jwt.issuer=feuca-test"
})
@ActiveProfiles("test")
class FacturaOptimisticLockIntegrationTest {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private UUID empresaId;
    private UUID facturaId;

    @AfterEach
    void cleanup() {
        if (facturaId != null) {
            jdbcTemplate.update("DELETE FROM facturas WHERE id = ?", facturaId);
        }
        if (empresaId != null) {
            jdbcTemplate.update("DELETE FROM empresas WHERE id = ?", empresaId);
        }
    }

    @Test
    void staleFacturaUpdateFailsWithOptimisticLockConflict() {
        empresaId = UUID.randomUUID();
        facturaId = UUID.randomUUID();
        insertEmpresa(empresaId);
        insertFactura(empresaId, facturaId);

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Factura firstCopy = tx.execute(status -> facturaRepository.findById(facturaId).orElseThrow());
        Factura secondCopy = tx.execute(status -> facturaRepository.findById(facturaId).orElseThrow());

        firstCopy.setNumero("F-OPT-PRIMERA");
        tx.executeWithoutResult(status -> facturaRepository.saveAndFlush(firstCopy));

        secondCopy.setNumero("F-OPT-SEGUNDA");
        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> tx.executeWithoutResult(status -> facturaRepository.saveAndFlush(secondCopy)));

        Factura persisted = facturaRepository.findById(facturaId).orElseThrow();
        assertEquals("F-OPT-PRIMERA", persisted.getNumero());
        assertEquals(1L, persisted.getVersion());
    }

    private void insertEmpresa(UUID id) {
        jdbcTemplate.update("""
                        INSERT INTO empresas (
                            id, razon_social, nombre_legal, nombre_comercial, nit, email, telefono,
                            direccion, actividad_economica, ciudad, pais, cod_establecimiento,
                            cod_punto_venta, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                        """,
                id,
                "Empresa Lock",
                "Empresa Lock Legal",
                "Lock",
                "NIT-" + id,
                "lock-" + id + "@example.com",
                "TEL-" + id.toString().substring(0, 12),
                "Direccion de prueba",
                "Servicios",
                "San Salvador",
                "El Salvador",
                "M001",
                "P001"
        );
    }

    private void insertFactura(UUID empresaId, UUID facturaId) {
        jdbcTemplate.update("""
                        INSERT INTO facturas (
                            id, empresa_id, numero, fecha_emision, fecha_vencimiento, estado, moneda_codigo,
                            subtotal_sin_iva, total_iva, total_con_iva, created_at, updated_at, version
                        )
                        VALUES (?, ?, ?, ?, ?, CAST(? AS invoice_status), ?, ?, ?, ?, ?, ?, ?)
                        """,
                facturaId,
                empresaId,
                "F-OPT-INICIAL",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                "BORRADOR",
                "USD",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                0L
        );
    }
}
