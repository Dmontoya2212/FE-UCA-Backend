package com.feuca.facturacion.client;

import com.feuca.facturacion.dto.dte.HaciendaRecepcionRequest;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import org.springframework.stereotype.Component;

@Component
public class UnsupportedHaciendaClient implements HaciendaClient {

    @Override
    public HaciendaRecepcionResponse enviar(HaciendaRecepcionRequest request) {
        throw new FacturaValidationException("La integracion real con Hacienda no esta configurada; no se puede enviar ni simular recepcion.");
    }
}
