package com.feuca.facturacion.client;

import com.feuca.facturacion.dto.dte.HaciendaRecepcionRequest;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;

public interface HaciendaClient {
    HaciendaRecepcionResponse enviar(HaciendaRecepcionRequest request);
}
