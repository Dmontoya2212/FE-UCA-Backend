package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.dto.response.FacturaLinea.FacturaLineaResponse;
import com.feuca.facturacion.entity.FacturaLinea;

import java.util.UUID;

public class FacturaLineaMapper {
    private FacturaLineaMapper(){}

    public static FacturaLinea toEntityCreate(FacturaLineaRequest req) {
        return FacturaLinea.builder()
                .id(UUID.randomUUID())
                .facturaId(req.getFacturaId())
                .itemId(req.getItemId())
                .descripcion(req.getDescripcion())
                .cantidad(req.getCantidad())
                .precioSinIva(req.getPrecioSinIva())
                .ivaPorcentaje(req.getIvaPorcentaje())

                .build();
    }

    public static void applyUpdate(FacturaLinea e, FacturaLineaUpdateRequest req) {
        if (req.getItemId() != null) e.setItemId(req.getItemId());
        if (req.getDescripcion() != null) e.setDescripcion(req.getDescripcion());
        if (req.getCantidad() != null) e.setCantidad(req.getCantidad());
        if (req.getPrecioSinIva() != null) e.setPrecioSinIva(req.getPrecioSinIva());
        if (req.getIvaPorcentaje() != null) e.setIvaPorcentaje(req.getIvaPorcentaje());
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
