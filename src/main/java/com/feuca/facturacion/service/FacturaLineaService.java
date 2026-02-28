package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.dto.response.FacturaLinea.FacturaLineaResponse;

import java.util.List;
import java.util.UUID;

public interface FacturaLineaService {
    FacturaLineaResponse create(UUID empresaId, FacturaLineaRequest request);

    FacturaLineaResponse getById(UUID empresaId, UUID facturaId, UUID lineaId);

    List<FacturaLineaResponse> getAllByFactura(UUID empresaId, UUID facturaId);

    FacturaLineaResponse update(UUID empresaId, UUID facturaId, UUID lineaId, FacturaLineaUpdateRequest request);

    void delete(UUID empresaId, UUID facturaId, UUID lineaId);
}
