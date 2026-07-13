package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;
import com.feuca.facturacion.dto.response.FacturaLinea.FacturaLineaResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class FacturaMapper {
    private FacturaMapper() {}

    public static Factura toEntityCreate(FacturaRequest req) {
        return Factura.builder()
                .id(UUID.randomUUID())
                .empresaId(req.getEmpresaId())
                .clienteId(req.getClienteId())
                .numero(req.getNumero())
                .fechaEmision(req.getFechaEmision())
                .fechaVencimiento(req.getFechaEmision().plusDays(30))
                .estado(InvoiceStatus.BORRADOR)
                .monedaCodigo(req.getMonedaCodigo() != null ? req.getMonedaCodigo() : "USD")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static FacturaLinea toLineaEntity(FacturaLineaRequest req, UUID facturaId) {
        BigDecimal cantidad = req.getCantidad();
        BigDecimal precioSinIva = req.getPrecioSinIva();
        BigDecimal ivaPct = req.getIvaPorcentaje();

        BigDecimal subtotalSinIva = cantidad.multiply(precioSinIva).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalIva = subtotalSinIva.multiply(ivaPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalConIva = subtotalSinIva.add(totalIva).setScale(2, RoundingMode.HALF_UP);

        return FacturaLinea.builder()
                .id(UUID.randomUUID())
                .facturaId(facturaId)
                .itemId(req.getItemId())
                .descripcion(req.getDescripcion().trim())
                .cantidad(cantidad)
                .precioSinIva(precioSinIva)
                .ivaPorcentaje(ivaPct)
                .subtotalSinIva(subtotalSinIva)
                .totalIva(totalIva)
                .totalConIva(totalConIva)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static FacturaResponse toResponse(Factura f, List<FacturaLinea> lineas, String clienteNombre) {
        List<FacturaLineaResponse> lineasResponse = lineas == null ? List.of() :
                lineas.stream().map(FacturaMapper::toLineaResponse).toList();

        return FacturaResponse.builder()
                .id(f.getId())
                .empresaId(f.getEmpresaId())
                .clienteId(f.getClienteId())
                .clienteNombre(clienteNombre)
                .numero(f.getNumero())
                .fechaEmision(f.getFechaEmision())
                .fechaVencimiento(f.getFechaVencimiento())
                .estado(f.getEstado())
                .monedaCodigo(f.getMonedaCodigo())
                .subtotalSinIva(f.getSubtotalSinIva())
                .totalIva(f.getTotalIva())
                .totalConIva(f.getTotalConIva())
                .lineas(lineasResponse)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    public static FacturaLineaResponse toLineaResponse(FacturaLinea l) {
        return FacturaLineaResponse.builder()
                .id(l.getId())
                .facturaId(l.getFacturaId())
                .itemId(l.getItemId())
                .descripcion(l.getDescripcion())
                .cantidad(l.getCantidad())
                .precioSinIva(l.getPrecioSinIva())
                .ivaPorcentaje(l.getIvaPorcentaje())
                .subtotalSinIva(l.getSubtotalSinIva())
                .totalIva(l.getTotalIva())
                .totalConIva(l.getTotalConIva())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }

    public static void applyUpdate(Factura f, FacturaUpdateRequest req) {
        if (req.getClienteId() != null) f.setClienteId(req.getClienteId());
        if (req.getNumero() != null) f.setNumero(req.getNumero());
        if (req.getFechaEmision() != null) {
            f.setFechaEmision(req.getFechaEmision());
            f.setFechaVencimiento(req.getFechaEmision().plusDays(30));
        }
        if (req.getMonedaCodigo() != null) f.setMonedaCodigo(req.getMonedaCodigo());
        f.setUpdatedAt(OffsetDateTime.now());
    }
}
