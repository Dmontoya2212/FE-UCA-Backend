package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;
import com.feuca.facturacion.entity.Factura;

import java.util.UUID;

public class FacturaMapper {
    private FacturaMapper(){}

    public static Factura toEntityCreate(FacturaRequest req) {
        return Factura.builder()
                .id(UUID.randomUUID())
                .empresaId(req.getEmpresaId())
                .clienteId(req.getClienteId())
                .numero(req.getNumero())
                .fechaEmision(req.getFechaEmision())
                .estado("BORRADOR")
                .monedaCodigo(req.getMonedaCodigo() != null ? req.getMonedaCodigo() : "USD")
                .build();
    }

    public static FacturaResponse toResponse(Factura f) {
        return FacturaResponse.builder()
                .id(f.getId())
                .empresaId(f.getEmpresaId())
                .clienteId(f.getClienteId())
                .numero(f.getNumero())
                .fechaEmision(f.getFechaEmision())
                .estado(f.getEstado())
                .monedaCodigo(f.getMonedaCodigo())
                .subtotalSinIva(f.getSubtotalSinIva())
                .totalIva(f.getTotalIva())
                .totalConIva(f.getTotalConIva())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    public static void applyUpdate(com.feuca.facturacion.entity.Factura f,
                                   com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest req) {

        if (req.getClienteId() != null) f.setClienteId(req.getClienteId());
        if (req.getNumero() != null) f.setNumero(req.getNumero());
        if (req.getFechaEmision() != null) f.setFechaEmision(req.getFechaEmision());
        if (req.getMonedaCodigo() != null) f.setMonedaCodigo(req.getMonedaCodigo());
    }
}
