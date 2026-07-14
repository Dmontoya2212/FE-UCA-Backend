package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.response.Factura.FacturaEmissionResponse;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;

import java.util.List;
import java.util.UUID;

public interface FacturaService {
    FacturaResponse create(FacturaRequest request);
    FacturaResponse getById(UUID empresaId, UUID facturaId);
    List<FacturaResponse> getAllByEmpresa(UUID empresaId);
    FacturaResponse update(UUID empresaId, UUID facturaId, FacturaUpdateRequest request);
    void delete(UUID empresaId, UUID facturaId);

    FacturaEmissionResponse prepararParaEnvio(UUID empresaId, UUID facturaId);

    // Requiere integracion real; solo una respuesta aceptada de Hacienda debe marcar la factura como emitida.
    FacturaEmissionResponse enviarAHacienda(UUID empresaId, UUID facturaId);
    FacturaEmissionResponse enviarAHacienda(UUID empresaId, UUID facturaId, String idempotencyKey);
}
