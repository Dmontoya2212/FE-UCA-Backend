package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.dto.response.FacturaLinea.FacturaLineaResponse;
import com.feuca.facturacion.entity.FacturaLinea;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

public class FacturaLineaMapper {
    private FacturaLineaMapper() {}

    public static void applyUpdate(FacturaLinea e, FacturaLineaUpdateRequest req) {
        if (req.getItemId() != null) e.setItemId(req.getItemId());
        if (req.getDescripcion() != null) e.setDescripcion(req.getDescripcion().trim());
        if (req.getCantidad() != null) e.setCantidad(req.getCantidad());
        if (req.getPrecioSinIva() != null) e.setPrecioSinIva(req.getPrecioSinIva());
        if (req.getIvaPorcentaje() != null) e.setIvaPorcentaje(req.getIvaPorcentaje());
        recalculateTotals(e);
        e.setUpdatedAt(OffsetDateTime.now());
    }

    private static void recalculateTotals(FacturaLinea e) {
        BigDecimal subtotalSinIva = e.getCantidad()
                .multiply(e.getPrecioSinIva())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalIva = subtotalSinIva
                .multiply(e.getIvaPorcentaje())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        e.setSubtotalSinIva(subtotalSinIva);
        e.setTotalIva(totalIva);
        e.setTotalConIva(subtotalSinIva.add(totalIva).setScale(2, RoundingMode.HALF_UP));
    }

    public static FacturaLineaResponse toResponse(FacturaLinea e) {
        return FacturaLineaResponse.builder()
                .id(e.getId())
                .facturaId(e.getFacturaId())
                .itemId(e.getItemId())
                .descripcion(e.getDescripcion())
                .cantidad(e.getCantidad())
                .precioSinIva(e.getPrecioSinIva())
                .ivaPorcentaje(e.getIvaPorcentaje())
                .subtotalSinIva(e.getSubtotalSinIva())
                .totalIva(e.getTotalIva())
                .totalConIva(e.getTotalConIva())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
