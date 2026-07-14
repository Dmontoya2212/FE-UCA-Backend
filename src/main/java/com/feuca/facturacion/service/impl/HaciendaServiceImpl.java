package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.client.HaciendaClient;
import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionRequest;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;
import com.feuca.facturacion.service.DteJsonValidationService;
import com.feuca.facturacion.service.HaciendaService;
import org.springframework.stereotype.Service;

@Service
public class HaciendaServiceImpl implements HaciendaService {

    private final HaciendaClient haciendaClient;
    private final DteJsonValidationService dteJsonValidationService;

    public HaciendaServiceImpl(HaciendaClient haciendaClient, DteJsonValidationService dteJsonValidationService) {
        this.haciendaClient = haciendaClient;
        this.dteJsonValidationService = dteJsonValidationService;
    }

    @Override
    public void validarDte(DteFacturaElectronica dte) {
        dteJsonValidationService.validarYSerializar(dte);
    }

    @Override
    public String firmarDte(DteFacturaElectronica dte) {
        return dteJsonValidationService.validarYSerializar(dte);
    }

    @Override
    public HaciendaRecepcionResponse enviarDte(HaciendaRecepcionRequest request) {
        return haciendaClient.enviar(request);
    }

    @Override
    public boolean respuestaAceptada(HaciendaRecepcionResponse response) {
        return response != null
                && response.isAceptada()
                && response.getSelloRecibido() != null
                && !response.getSelloRecibido().isBlank()
                && response.getFechaRecepcion() != null;
    }
}
