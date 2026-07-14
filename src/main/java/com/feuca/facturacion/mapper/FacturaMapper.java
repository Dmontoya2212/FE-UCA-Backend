package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest;
import com.feuca.facturacion.dto.response.Factura.FacturaEmissionResponse;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;
import com.feuca.facturacion.dto.response.FacturaLinea.FacturaLineaResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.enums.EstadoFactura;
import com.feuca.facturacion.util.FacturaLineaCalculator;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class FacturaMapper {
    private FacturaMapper() {}

    public static Factura toEntityCreate(FacturaRequest req) {
        UUID id = UUID.randomUUID();
        return Factura.builder()
                .id(id)
                .empresaId(req.getEmpresaId())
                .clienteId(req.getClienteId())
                .numero(req.getNumero() != null ? req.getNumero() : "BORRADOR-" + id)
                .fechaEmision(req.getFechaEmision())
                .estado(EstadoFactura.BORRADOR.name())
                .monedaCodigo(req.getMonedaCodigo() != null ? req.getMonedaCodigo() : "USD")
                .tipoDte(req.getTipoDte() != null ? req.getTipoDte() : "01")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static FacturaLinea toLineaEntity(FacturaLineaRequest req, UUID facturaId) {
        FacturaLinea linea = FacturaLinea.builder()
                .id(UUID.randomUUID())
                .facturaId(facturaId)
                .itemId(req.getItemId())
                .descripcion(req.getDescripcion().trim())
                .cantidad(req.getCantidad())
                .precioSinIva(req.getPrecioSinIva())
                .ivaPorcentaje(req.getIvaPorcentaje())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        FacturaLineaCalculator.recalcular(linea);
        return linea;
    }

    public static FacturaResponse toResponse(Factura f, List<FacturaLinea> lineas, String clienteNombre) {
        List<FacturaLineaResponse> lineasResponse = lineas == null ? List.of() :
                lineas.stream().map(FacturaMapper::toLineaResponse).toList();

        return FacturaResponse.builder()
                .id(f.getId())
                .empresaId(f.getEmpresaId())
                .clienteId(f.getClienteId())
                .clienteNombre(clienteNombre)
                .clienteNombreRazonSocial(f.getClienteNombreRazonSocial() != null ? f.getClienteNombreRazonSocial() : clienteNombre)
                .clienteNifCif(f.getClienteNifCif())
                .clienteDireccion(f.getClienteDireccion())
                .numero(f.getNumero())
                .fechaEmision(f.getFechaEmision())
                .estado(f.getEstado())
                .monedaCodigo(f.getMonedaCodigo())
                .subtotalSinIva(f.getSubtotalSinIva())
                .totalIva(f.getTotalIva())
                .totalConIva(f.getTotalConIva())
                .tipoDte(f.getTipoDte())
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

    public static FacturaEmissionResponse toEmissionResponse(Factura f) {
        return FacturaEmissionResponse.builder()
                .id(f.getId())
                .empresaId(f.getEmpresaId())
                .estado(f.getEstado())
                .tipoDte(f.getTipoDte())
                .codigoGeneracion(f.getCodigoGeneracion())
                .numeroControl(f.getNumeroControl())
                .selloRecibido(f.getSelloRecibido())
                .fechaRecepcion(f.getFechaRecepcion())
                .haciendaCodigoRespuesta(f.getHaciendaCodigoRespuesta())
                .haciendaMensajeRespuesta(f.getHaciendaMensajeRespuesta())
                .haciendaErrores(f.getHaciendaErrores())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    public static void applyUpdate(Factura f, FacturaUpdateRequest req) {
        if (req.getClienteId() != null) f.setClienteId(req.getClienteId());
        if (req.getFechaEmision() != null) f.setFechaEmision(req.getFechaEmision());
        if (req.getMonedaCodigo() != null) f.setMonedaCodigo(req.getMonedaCodigo());
        if (req.getTipoDte() != null) f.setTipoDte(req.getTipoDte());
        f.setUpdatedAt(OffsetDateTime.now());
    }
}
