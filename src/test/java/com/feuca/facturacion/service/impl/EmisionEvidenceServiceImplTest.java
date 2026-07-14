package com.feuca.facturacion.service.impl;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionRequest;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.IntentoEmision;
import com.feuca.facturacion.repository.IntentoEmisionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmisionEvidenceServiceImplTest {

    private final IntentoEmisionRepository intentoEmisionRepository = mock(IntentoEmisionRepository.class);
    private final EmisionEvidenceServiceImpl service = new EmisionEvidenceServiceImpl(intentoEmisionRepository);

    @Test
    void registrarInicioPersistsTransmissionEvidenceWithoutSecrets() {
        UUID facturaId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .id(facturaId)
                .empresaId(empresaId)
                .codigoGeneracion("COD-GEN")
                .numeroControl("DTE-01-M001P001-000000000000001")
                .build();
        HaciendaRecepcionRequest request = HaciendaRecepcionRequest.builder()
                .empresaId(empresaId)
                .facturaId(facturaId)
                .codigoGeneracion("COD-GEN")
                .numeroControl("DTE-01-M001P001-000000000000001")
                .dteJson("{\"identificacion\":{\"numeroControl\":\"DTE-01-M001P001-000000000000001\"}}")
                .documentoFirmado("DOCUMENTO-FIRMADO")
                .build();
        when(intentoEmisionRepository.countByFacturaId(facturaId)).thenReturn(2L);
        when(intentoEmisionRepository.save(any(IntentoEmision.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IntentoEmision intento = service.registrarInicio(factura, "idem-1", "00", request);

        assertEquals(facturaId, intento.getFacturaId());
        assertEquals(empresaId, intento.getEmpresaId());
        assertEquals("COD-GEN", intento.getCodigoGeneracion());
        assertEquals("DTE-01-M001P001-000000000000001", intento.getNumeroControl());
        assertEquals("00", intento.getAmbiente());
        assertEquals("idem-1", intento.getIdempotencyKey());
        assertEquals(3, intento.getNumeroIntento());
        assertNotNull(intento.getFechaIntento());
        assertTrue(intento.getRequestJson().contains("COD-GEN"));
        assertTrue(intento.getRequestJson().contains("\\\"identificacion\\\""));
    }

    @Test
    void registrarRespuestaPersistsHaciendaResponseEvidence() {
        IntentoEmision intento = IntentoEmision.builder().id(UUID.randomUUID()).build();
        OffsetDateTime fechaRecepcion = OffsetDateTime.now();
        HaciendaRecepcionResponse response = HaciendaRecepcionResponse.builder()
                .aceptada(true)
                .codigoHttp(200)
                .codigoRespuesta("001")
                .mensajeRespuesta("Procesado")
                .selloRecibido("SELLO")
                .fechaRecepcion(fechaRecepcion)
                .responseJson("{\"estado\":\"PROCESADO\"}")
                .build();

        service.registrarRespuesta(intento, "EMITIDA", response);

        ArgumentCaptor<IntentoEmision> captor = ArgumentCaptor.forClass(IntentoEmision.class);
        verify(intentoEmisionRepository).save(captor.capture());
        IntentoEmision saved = captor.getValue();
        assertEquals("EMITIDA", saved.getEstadoIntento());
        assertEquals(200, saved.getCodigoHttp());
        assertEquals("001", saved.getCodigoHacienda());
        assertEquals("Procesado", saved.getDescripcionRespuesta());
        assertEquals("SELLO", saved.getSelloRecibido());
        assertEquals("{\"estado\":\"PROCESADO\"}", saved.getResponseJson());
        assertNotNull(saved.getFechaRespuesta());
    }
}
