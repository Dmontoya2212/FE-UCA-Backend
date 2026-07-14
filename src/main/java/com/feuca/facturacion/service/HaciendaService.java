package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionRequest;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;

public interface HaciendaService {
    void validarDte(DteFacturaElectronica dte);
    String firmarDte(DteFacturaElectronica dte);
    HaciendaRecepcionResponse enviarDte(HaciendaRecepcionRequest request);
    boolean respuestaAceptada(HaciendaRecepcionResponse response);
}
