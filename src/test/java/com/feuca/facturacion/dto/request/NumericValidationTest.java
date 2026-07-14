package com.feuca.facturacion.dto.request;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.dto.request.Item.ItemRequest;
import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaRequest;
import com.feuca.facturacion.entity.ItemCategoria;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NumericValidationTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void facturaLineaRejectsNonPositiveQuantityAndInvalidIvaPercentage() {
        FacturaLineaRequest request = FacturaLineaRequest.builder()
                .descripcion("Servicio")
                .cantidad(BigDecimal.ZERO)
                .precioSinIva(new BigDecimal("10.00000000"))
                .ivaPorcentaje(new BigDecimal("100.01"))
                .build();

        var violations = VALIDATOR.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cantidad")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("ivaPorcentaje")));
    }

    @Test
    void facturaLineaRejectsNegativeValues() {
        FacturaLineaRequest request = FacturaLineaRequest.builder()
                .descripcion("Servicio")
                .cantidad(new BigDecimal("-1.00"))
                .precioSinIva(new BigDecimal("-0.01"))
                .ivaPorcentaje(new BigDecimal("-0.01"))
                .build();

        var violations = VALIDATOR.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cantidad")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("precioSinIva")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("ivaPorcentaje")));
    }

    @Test
    void facturaLineaAllowsZeroPrice() {
        FacturaLineaRequest request = FacturaLineaRequest.builder()
                .descripcion("Cortesia")
                .cantidad(BigDecimal.ONE)
                .precioSinIva(new BigDecimal("0.00000000"))
                .ivaPorcentaje(new BigDecimal("13.00"))
                .build();

        var violations = VALIDATOR.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void facturaLineaUpdateRejectsTooManyDecimals() {
        FacturaLineaUpdateRequest request = FacturaLineaUpdateRequest.builder()
                .cantidad(new BigDecimal("1.001"))
                .precioSinIva(new BigDecimal("1.123456789"))
                .build();

        var violations = VALIDATOR.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cantidad")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("precioSinIva")));
    }

    @Test
    void itemRejectsNegativePriceAndIvaRejectsOutOfRangePercentage() {
        ItemRequest item = ItemRequest.builder()
                .empresaId(UUID.randomUUID())
                .nombre("Servicio")
                .categoria(ItemCategoria.SERVICIO)
                .ivaId(UUID.randomUUID())
                .precioSinIva(new BigDecimal("-0.01"))
                .build();
        IvaTasaRequest iva = IvaTasaRequest.builder()
                .empresaId(UUID.randomUUID())
                .nombre("IVA")
                .porcentaje(new BigDecimal("100.01"))
                .build();

        assertTrue(VALIDATOR.validate(item).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("precioSinIva")));
        assertTrue(VALIDATOR.validate(iva).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("porcentaje")));
    }

    @Test
    void facturaRequiresCurrencyAndAtLeastOneLine() {
        FacturaRequest request = FacturaRequest.builder()
                .empresaId(UUID.randomUUID())
                .fechaEmision(LocalDate.now())
                .monedaCodigo("")
                .lineas(List.of())
                .build();

        var violations = VALIDATOR.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("monedaCodigo")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lineas")));
    }
}
