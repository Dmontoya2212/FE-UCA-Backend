package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.dte.HaciendaRecepcionRequest;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.IntentoEmision;

public interface EmisionEvidenceService {
    IntentoEmision registrarInicio(Factura factura, String idempotencyKey, String ambiente, HaciendaRecepcionRequest request);
    void registrarRespuesta(IntentoEmision intento, String estado, HaciendaRecepcionResponse response);
    void registrarErrorTecnico(IntentoEmision intento, RuntimeException exception);
}
