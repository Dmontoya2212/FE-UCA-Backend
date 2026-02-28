package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;

import java.util.List;
import java.util.UUID;

public interface FacturaService {
    FacturaResponse create(FacturaRequest request);
    FacturaResponse getById(UUID empresaId, UUID facturaId);
    List<FacturaResponse> getAllByEmpresa(UUID empresaId);
    FacturaResponse update(UUID empresaId, UUID facturaId, FacturaUpdateRequest request);
    void delete(UUID empresaId, UUID facturaId);

    // Esto es lo que cambia el estado de la factura (Osea que cambia de Borrador a Enviada)(Gay el que lo lea)
    FacturaResponse enviarAHacienda(UUID empresaId, UUID facturaId);
}
