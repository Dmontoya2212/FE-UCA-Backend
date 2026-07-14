package com.feuca.facturacion.util;

import com.feuca.facturacion.entity.FacturaLinea;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FacturaLineaCalculator {

    public static final int CANTIDAD_SCALE = 2;
    public static final int PRECIO_SCALE = 8;
    public static final int IVA_PORCENTAJE_SCALE = 2;
    public static final int TOTAL_SCALE = 8;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private FacturaLineaCalculator() {
    }

    public static void recalcular(FacturaLinea linea) {
        BigDecimal cantidad = require(linea.getCantidad(), "cantidad").setScale(CANTIDAD_SCALE, ROUNDING_MODE);
        BigDecimal precioSinIva = require(linea.getPrecioSinIva(), "precioSinIva").setScale(PRECIO_SCALE, ROUNDING_MODE);
        BigDecimal ivaPorcentaje = require(linea.getIvaPorcentaje(), "ivaPorcentaje").setScale(IVA_PORCENTAJE_SCALE, ROUNDING_MODE);

        BigDecimal subtotalSinIva = cantidad.multiply(precioSinIva).setScale(TOTAL_SCALE, ROUNDING_MODE);
        BigDecimal totalIva = subtotalSinIva.multiply(ivaPorcentaje)
                .divide(CIEN, TOTAL_SCALE, ROUNDING_MODE);
        BigDecimal totalConIva = subtotalSinIva.add(totalIva).setScale(TOTAL_SCALE, ROUNDING_MODE);

        linea.setCantidad(cantidad);
        linea.setPrecioSinIva(precioSinIva);
        linea.setIvaPorcentaje(ivaPorcentaje);
        linea.setSubtotalSinIva(subtotalSinIva);
        linea.setTotalIva(totalIva);
        linea.setTotalConIva(totalConIva);
    }

    private static BigDecimal require(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("No se puede calcular la linea sin " + fieldName + ".");
        }
        return value;
    }
}
