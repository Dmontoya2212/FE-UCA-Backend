package com.feuca.facturacion.util;

import com.feuca.facturacion.entity.FacturaLinea;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FacturaLineaCalculatorTest {

    @Test
    void recalcularNormalizesScalesAndUsesHalfUpRounding() {
        FacturaLinea linea = FacturaLinea.builder()
                .cantidad(new BigDecimal("2.345"))
                .precioSinIva(new BigDecimal("10.123456785"))
                .ivaPorcentaje(new BigDecimal("12.345"))
                .build();

        FacturaLineaCalculator.recalcular(linea);

        assertEquals(new BigDecimal("2.35"), linea.getCantidad());
        assertEquals(new BigDecimal("10.12345679"), linea.getPrecioSinIva());
        assertEquals(new BigDecimal("12.35"), linea.getIvaPorcentaje());
        assertEquals(new BigDecimal("23.79012346"), linea.getSubtotalSinIva());
        assertEquals(new BigDecimal("2.93808025"), linea.getTotalIva());
        assertEquals(new BigDecimal("26.72820371"), linea.getTotalConIva());
    }
}
