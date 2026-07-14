package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.client.HaciendaClient;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionRequest;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;
import com.feuca.facturacion.service.DteJsonValidationService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HaciendaServiceImplTest {

    private final HaciendaClient haciendaClient = mock(HaciendaClient.class);
    private final DteJsonValidationService dteJsonValidationService = mock(DteJsonValidationService.class);
    private final HaciendaServiceImpl haciendaService = new HaciendaServiceImpl(haciendaClient, dteJsonValidationService);

    @Test
    void enviarDteDelegatesToClient() {
        HaciendaRecepcionRequest request = HaciendaRecepcionRequest.builder()
                .empresaId(UUID.randomUUID())
                .facturaId(UUID.randomUUID())
                .build();
        HaciendaRecepcionResponse response = HaciendaRecepcionResponse.builder()
                .aceptada(false)
                .mensajeRespuesta("Rechazado")
                .build();
        when(haciendaClient.enviar(request)).thenReturn(response);

        assertSame(response, haciendaService.enviarDte(request));
    }

    @Test
    void respuestaAceptadaRequiresAcceptedFlagSelloAndReceptionDate() {
        assertTrue(haciendaService.respuestaAceptada(HaciendaRecepcionResponse.builder()
                .aceptada(true)
                .selloRecibido("SELLO")
                .fechaRecepcion(OffsetDateTime.now())
                .build()));
        assertFalse(haciendaService.respuestaAceptada(HaciendaRecepcionResponse.builder()
                .aceptada(true)
                .selloRecibido("")
                .fechaRecepcion(OffsetDateTime.now())
                .build()));
        assertFalse(haciendaService.respuestaAceptada(HaciendaRecepcionResponse.builder()
                .aceptada(true)
                .selloRecibido("SELLO")
                .build()));
    }
}
