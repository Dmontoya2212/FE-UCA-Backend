package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.dte.DteFacturaElectronica;

import java.util.UUID;

import com.feuca.facturacion.entity.Factura;

public interface DteService {
    DteFacturaElectronica generarDte(UUID empresaId, UUID facturaId);
    void asignarCodigos(Factura f);
}
