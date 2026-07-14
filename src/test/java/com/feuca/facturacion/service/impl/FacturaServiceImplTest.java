package com.feuca.facturacion.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;
import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest;
import com.feuca.facturacion.dto.response.Factura.FacturaEmissionResponse;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;
import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.IntentoEmision;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.exception.Cliente.ClienteNotFoundException;
import com.feuca.facturacion.exception.Factura.FacturaNoEditableException;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.exception.Item.ItemNotFoundException;
import com.feuca.facturacion.repository.ClienteRepository;
import com.feuca.facturacion.repository.EmpresaMonedaRepository;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.repository.ItemRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.DteJsonValidationService;
import com.feuca.facturacion.service.DteService;
import com.feuca.facturacion.service.EmisionEvidenceService;
import com.feuca.facturacion.service.FacturaTotalsService;
import com.feuca.facturacion.service.FacturaStateValidator;
import com.feuca.facturacion.service.HaciendaService;
import com.feuca.facturacion.service.OperationalMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FacturaServiceImplTest {

    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final FacturaLineaRepository facturaLineaRepository = mock(FacturaLineaRepository.class);
    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final EmpresaMonedaRepository empresaMonedaRepository = mock(EmpresaMonedaRepository.class);
    private final DteService dteService = mock(DteService.class);
    private final DteJsonValidationService dteJsonValidationService = mock(DteJsonValidationService.class);
    private final AccessControlService accessControlService = mock(AccessControlService.class);
    private final FacturaTotalsService facturaTotalsService = mock(FacturaTotalsService.class);
    private final HaciendaService haciendaService = mock(HaciendaService.class);
    private final EmisionEvidenceService emisionEvidenceService = mock(EmisionEvidenceService.class);
    private final FacturaStateValidator facturaStateValidator = new FacturaStateValidator();
    private final OperationalMetricsService operationalMetricsService = mock(OperationalMetricsService.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final FacturaServiceImpl facturaService = new FacturaServiceImpl(
            facturaRepository,
            facturaLineaRepository,
            clienteRepository,
            itemRepository,
            empresaMonedaRepository,
            dteService,
            dteJsonValidationService,
            accessControlService,
            facturaTotalsService,
            haciendaService,
            emisionEvidenceService,
            facturaStateValidator,
            operationalMetricsService
    );

    {
        when(empresaMonedaRepository.existsByEmpresa_idAndMoneda_codigo(any(UUID.class), anyString())).thenReturn(true);
        when(dteJsonValidationService.validarYSerializar(any())).thenReturn("{\"identificacion\":{\"tipoDte\":\"01\"}}");
    }

    @Test
    void createWithSingleLineCalculatesLineTotalsAndRecalculatesInvoice() {
        UUID empresaId = UUID.randomUUID();
        AtomicReference<Factura> savedFacturaRef = prepareCreatePersistenceMocks();
        FacturaRequest request = facturaRequest(empresaId, null, null);

        facturaService.create(request);

        List<FacturaLinea> lineas = capturarLineasGuardadas();
        FacturaLinea linea = lineas.getFirst();
        assertEquals(new BigDecimal("1.00"), linea.getCantidad());
        assertEquals(new BigDecimal("10.00000000"), linea.getPrecioSinIva());
        assertEquals(new BigDecimal("13.00"), linea.getIvaPorcentaje());
        assertEquals(new BigDecimal("10.00000000"), linea.getSubtotalSinIva());
        assertEquals(new BigDecimal("1.30000000"), linea.getTotalIva());
        assertEquals(new BigDecimal("11.30000000"), linea.getTotalConIva());
        verify(facturaTotalsService).recalcularTotalesFactura(savedFacturaRef.get().getId());
    }

    @Test
    void createIgnoresManipulatedTotalsSentByClient() throws Exception {
        UUID empresaId = UUID.randomUUID();
        AtomicReference<Factura> savedFacturaRef = prepareCreatePersistenceMocks();
        FacturaRequest request = objectMapper.readValue("""
                {
                  "empresaId": "%s",
                  "numero": "F-MANIPULADA",
                  "moneda_codigo": "USD",
                  "subtotalSinIva": 999999,
                  "totalIva": 999999,
                  "totalConIva": 999999,
                  "lineas": [
                    {
                      "descripcion": "Servicio",
                      "cantidad": 2,
                      "precio_sin_iva": 10,
                      "iva_porcentaje": 13,
                      "subtotalSinIva": 1,
                      "totalIva": 1,
                      "totalConIva": 1
                    }
                  ]
                }
                """.formatted(empresaId), FacturaRequest.class);
        request.setFechaEmision(LocalDate.now());

        facturaService.create(request);

        List<FacturaLinea> lineas = capturarLineasGuardadas();
        assertEquals(new BigDecimal("20.00000000"), lineas.getFirst().getSubtotalSinIva());
        assertEquals(new BigDecimal("2.60000000"), lineas.getFirst().getTotalIva());
        assertEquals(new BigDecimal("22.60000000"), lineas.getFirst().getTotalConIva());
        verify(facturaTotalsService).recalcularTotalesFactura(savedFacturaRef.get().getId());
    }

    @Test
    void createDraftDoesNotGenerateDefinitiveDteCodes() {
        UUID empresaId = UUID.randomUUID();
        prepareCreatePersistenceMocks();

        facturaService.create(facturaRequest(empresaId, null, null));

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(captor.capture());
        Factura draft = captor.getValue();
        assertEquals(null, draft.getNumeroControl());
        assertEquals(null, draft.getCodigoGeneracion());
        verify(dteService, never()).asignarCodigos(any(Factura.class));
    }

    @Test
    void createWithMultipleTaxRatesCalculatesEveryLineFromBackend() {
        UUID empresaId = UUID.randomUUID();
        AtomicReference<Factura> savedFacturaRef = prepareCreatePersistenceMocks();
        FacturaRequest request = FacturaRequest.builder()
                .empresaId(empresaId)
                .numero("F-MULTI")
                .fechaEmision(LocalDate.now())
                .monedaCodigo("USD")
                .lineas(List.of(
                        facturaLineaRequest("Servicio gravado", new BigDecimal("2.00"), new BigDecimal("10.00000000"), new BigDecimal("13.00")),
                        facturaLineaRequest("Servicio exento", new BigDecimal("3.00"), new BigDecimal("5.00000000"), BigDecimal.ZERO)
                ))
                .build();

        facturaService.create(request);

        List<FacturaLinea> lineas = capturarLineasGuardadas();
        assertEquals(2, lineas.size());
        assertEquals(new BigDecimal("20.00000000"), lineas.get(0).getSubtotalSinIva());
        assertEquals(new BigDecimal("2.60000000"), lineas.get(0).getTotalIva());
        assertEquals(new BigDecimal("22.60000000"), lineas.get(0).getTotalConIva());
        assertEquals(new BigDecimal("15.00000000"), lineas.get(1).getSubtotalSinIva());
        assertEquals(new BigDecimal("0E-8"), lineas.get(1).getTotalIva());
        assertEquals(new BigDecimal("15.00000000"), lineas.get(1).getTotalConIva());
        verify(facturaTotalsService).recalcularTotalesFactura(savedFacturaRef.get().getId());
    }

    @Test
    void createRejectsClienteFromAnotherEmpresaBeforeSaving() {
        UUID empresaId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        FacturaRequest request = facturaRequest(empresaId, clienteId, null);
        when(clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> facturaService.create(request));

        verify(facturaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createRejectsItemFromAnotherEmpresaBeforeSaving() {
        UUID empresaId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        FacturaRequest request = facturaRequest(empresaId, null, itemId);
        when(itemRepository.findByIdAndEmpresaId(itemId, empresaId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> facturaService.create(request));

        verify(facturaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createRejectsCurrencyNotAssignedToEmpresaBeforeSaving() {
        UUID empresaId = UUID.randomUUID();
        FacturaRequest request = facturaRequest(empresaId, null, null);
        request.setMonedaCodigo("EUR");
        when(empresaMonedaRepository.existsByEmpresa_idAndMoneda_codigo(empresaId, "EUR")).thenReturn(false);

        assertThrows(FacturaValidationException.class, () -> facturaService.create(request));

        verify(facturaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void prepararParaEnvioWithoutRealAcceptedResponseDoesNotMarkFacturaAsEmitida() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .id(facturaId)
                .empresaId(empresaId)
                .estado("BORRADOR")
                .build();
        FacturaLinea linea = FacturaLinea.builder()
                .id(UUID.randomUUID())
                .facturaId(facturaId)
                .subtotalSinIva(BigDecimal.TEN)
                .totalIva(new BigDecimal("1.30000000"))
                .totalConIva(new BigDecimal("11.30000000"))
                .build();
        when(facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.findAndLockByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(linea));
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FacturaEmissionResponse response = facturaService.prepararParaEnvio(empresaId, facturaId);

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(captor.capture());
        assertEquals("LISTA_PARA_EMITIR", captor.getValue().getEstado());
        assertEquals("LISTA_PARA_EMITIR", response.getEstado());
    }

    @Test
    void enviarAHaciendaMarksEmitidaOnlyWithAcceptedResponseAndSello() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        Factura factura = facturaReadyForSend(empresaId, facturaId);
        FacturaLinea linea = facturaLinea(facturaId);
        OffsetDateTime fechaRecepcion = OffsetDateTime.now();
        HaciendaRecepcionResponse haciendaResponse = HaciendaRecepcionResponse.builder()
                .aceptada(true)
                .selloRecibido("SELLO-ACEPTADO")
                .fechaRecepcion(fechaRecepcion)
                .codigoRespuesta("001")
                .mensajeRespuesta("Procesado correctamente")
                .responseJson("{\"estado\":\"PROCESADO\"}")
                .build();
        when(facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.findAndLockByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(linea));
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emisionEvidenceService.registrarInicio(any(), any(), any(), any()))
                .thenReturn(IntentoEmision.builder().id(UUID.randomUUID()).build());
        when(haciendaService.enviarDte(any())).thenReturn(haciendaResponse);
        when(haciendaService.respuestaAceptada(haciendaResponse)).thenReturn(true);

        FacturaEmissionResponse response = facturaService.enviarAHacienda(empresaId, facturaId, "idem-123");

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository, times(2)).save(captor.capture());
        Factura saved = captor.getAllValues().get(1);
        assertEquals("EMITIDA", saved.getEstado());
        assertEquals("SELLO-ACEPTADO", saved.getSelloRecibido());
        assertEquals(fechaRecepcion, saved.getFechaRecepcion());
        assertEquals("001", saved.getHaciendaCodigoRespuesta());
        assertEquals("EMITIDA", response.getEstado());
        verify(emisionEvidenceService).registrarInicio(any(), any(), any(), any());
        verify(emisionEvidenceService).registrarRespuesta(any(), org.mockito.ArgumentMatchers.eq("EMITIDA"), any());
    }

    @Test
    void acceptanceFlowEmitsOnlyAfterAcceptedResponseStoresEvidenceAndBlocksLaterAccess() {
        UUID empresaId = UUID.randomUUID();
        UUID otraEmpresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        Factura factura = facturaReadyForSend(empresaId, facturaId);
        FacturaLinea linea = facturaLinea(facturaId);
        IntentoEmision intento = IntentoEmision.builder()
                .id(UUID.randomUUID())
                .numeroIntento(1)
                .build();
        OffsetDateTime fechaRecepcion = OffsetDateTime.now();
        HaciendaRecepcionResponse haciendaResponse = HaciendaRecepcionResponse.builder()
                .aceptada(true)
                .selloRecibido("SELLO-ACEPTACION-FINAL")
                .fechaRecepcion(fechaRecepcion)
                .codigoRespuesta("001")
                .mensajeRespuesta("Procesado correctamente")
                .responseJson("{\"estado\":\"PROCESADO\"}")
                .build();
        when(facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.findAndLockByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(linea));
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emisionEvidenceService.registrarInicio(any(), eq("acceptance-idem"), any(), any())).thenReturn(intento);
        when(haciendaService.enviarDte(any())).thenReturn(haciendaResponse);
        when(haciendaService.respuestaAceptada(haciendaResponse)).thenReturn(true);

        FacturaEmissionResponse emitida = facturaService.enviarAHacienda(empresaId, facturaId, "acceptance-idem");

        assertEquals("EMITIDA", emitida.getEstado());
        assertEquals("EMITIDA", factura.getEstado());
        assertEquals("SELLO-ACEPTACION-FINAL", factura.getSelloRecibido());
        assertEquals(fechaRecepcion, factura.getFechaRecepcion());
        assertEquals("001", factura.getHaciendaCodigoRespuesta());
        verify(emisionEvidenceService).registrarInicio(any(), eq("acceptance-idem"), any(), any());
        verify(emisionEvidenceService).registrarRespuesta(intento, "EMITIDA", haciendaResponse);

        assertThrows(FacturaNoEditableException.class,
                () -> facturaService.update(empresaId, facturaId, FacturaUpdateRequest.builder()
                        .monedaCodigo("USD")
                        .build()));

        doThrow(new AccessDeniedException("No tiene acceso a la empresa indicada."))
                .when(accessControlService).requireEmpresaAccess(otraEmpresaId);

        assertThrows(AccessDeniedException.class, () -> facturaService.getById(otraEmpresaId, facturaId));
        verify(facturaRepository, never()).findByIdAndEmpresaId(facturaId, otraEmpresaId);
    }

    @Test
    void enviarAHaciendaRejectedResponseDoesNotMarkEmitida() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        Factura factura = facturaReadyForSend(empresaId, facturaId);
        FacturaLinea linea = facturaLinea(facturaId);
        HaciendaRecepcionResponse haciendaResponse = HaciendaRecepcionResponse.builder()
                .aceptada(false)
                .codigoRespuesta("901")
                .mensajeRespuesta("Rechazado")
                .errores("Firma invalida")
                .build();
        when(facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.findAndLockByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(linea));
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emisionEvidenceService.registrarInicio(any(), any(), any(), any()))
                .thenReturn(IntentoEmision.builder().id(UUID.randomUUID()).build());
        when(haciendaService.enviarDte(any())).thenReturn(haciendaResponse);
        when(haciendaService.respuestaAceptada(haciendaResponse)).thenReturn(false);

        FacturaEmissionResponse response = facturaService.enviarAHacienda(empresaId, facturaId);

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository, times(2)).save(captor.capture());
        Factura saved = captor.getAllValues().get(1);
        assertEquals("RECHAZADA", saved.getEstado());
        assertEquals("901", saved.getHaciendaCodigoRespuesta());
        assertEquals("Firma invalida", saved.getHaciendaErrores());
        assertEquals("RECHAZADA", response.getEstado());
    }

    @Test
    void enviarAHaciendaAllowsRetryFromRejectedState() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        Factura factura = facturaReadyForSend(empresaId, facturaId);
        factura.setEstado("RECHAZADA");
        FacturaLinea linea = facturaLinea(facturaId);
        List<String> estadosGuardados = new java.util.ArrayList<>();
        HaciendaRecepcionResponse haciendaResponse = HaciendaRecepcionResponse.builder()
                .aceptada(false)
                .codigoRespuesta("902")
                .mensajeRespuesta("Rechazado nuevamente")
                .build();
        when(facturaRepository.findAndLockByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(linea));
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> {
            Factura saved = invocation.getArgument(0);
            estadosGuardados.add(saved.getEstado());
            return saved;
        });
        when(emisionEvidenceService.registrarInicio(any(), any(), any(), any()))
                .thenReturn(IntentoEmision.builder().id(UUID.randomUUID()).build());
        when(haciendaService.enviarDte(any())).thenReturn(haciendaResponse);
        when(haciendaService.respuestaAceptada(haciendaResponse)).thenReturn(false);

        FacturaEmissionResponse response = facturaService.enviarAHacienda(empresaId, facturaId);

        assertEquals(List.of("ENVIANDO", "RECHAZADA"), estadosGuardados);
        assertEquals("RECHAZADA", response.getEstado());
    }

    @Test
    void enviarAHaciendaRejectsAlreadyEmitidaWithoutCallingHacienda() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        Factura factura = facturaReadyForSend(empresaId, facturaId);
        factura.setEstado("EMITIDA");
        when(facturaRepository.findAndLockByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));

        assertThrows(com.feuca.facturacion.exception.Factura.FacturaNoEditableException.class,
                () -> facturaService.enviarAHacienda(empresaId, facturaId, "idem-emitida"));

        verify(haciendaService, never()).enviarDte(any());
        verify(emisionEvidenceService, never()).registrarInicio(any(), any(), any(), any());
    }

    @Test
    void enviarAHaciendaRejectsAlreadySendingWithoutCallingHacienda() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        Factura factura = facturaReadyForSend(empresaId, facturaId);
        factura.setEstado("ENVIANDO");
        when(facturaRepository.findAndLockByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));

        assertThrows(com.feuca.facturacion.exception.Factura.FacturaNoEditableException.class,
                () -> facturaService.enviarAHacienda(empresaId, facturaId, "idem-enviando"));

        verify(haciendaService, never()).enviarDte(any());
        verify(emisionEvidenceService, never()).registrarInicio(any(), any(), any(), any());
    }

    @Test
    void enviarAHaciendaRecordsTechnicalErrorWhenHaciendaClientFails() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        Factura factura = facturaReadyForSend(empresaId, facturaId);
        FacturaLinea linea = facturaLinea(facturaId);
        IntentoEmision intento = IntentoEmision.builder().id(UUID.randomUUID()).build();
        RuntimeException exception = new RuntimeException("Timeout Hacienda");
        when(facturaRepository.findAndLockByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(linea));
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emisionEvidenceService.registrarInicio(any(), any(), any(), any())).thenReturn(intento);
        when(haciendaService.enviarDte(any())).thenThrow(exception);

        assertThrows(RuntimeException.class, () -> facturaService.enviarAHacienda(empresaId, facturaId, "idem-timeout"));

        verify(emisionEvidenceService).registrarInicio(any(), any(), any(), any());
        verify(emisionEvidenceService).registrarErrorTecnico(intento, exception);
        verify(operationalMetricsService).recordEmissionFailure("ERROR_TECNICO");
    }

    @Test
    void createStoresClienteAndItemSnapshots() {
        UUID empresaId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        AtomicReference<Factura> savedFacturaRef = new AtomicReference<>();
        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .empresaId(empresaId)
                .nombreRazonSocial("Cliente historico")
                .nifCif("06142802901012")
                .direccion("Direccion historica")
                .tipoDocumento("36")
                .nrc("12345678")
                .codActividad("620100")
                .descActividad("Actividad historica")
                .departamento("06")
                .municipio("14")
                .distrito("0001")
                .telefono("22223333")
                .email("cliente@example.com")
                .build();
        Item item = Item.builder()
                .id(itemId)
                .empresaId(empresaId)
                .codigoInterno("ITEM-001")
                .unidadMedida(59)
                .categoria(ItemCategoria.SERVICIO)
                .activo(true)
                .build();
        when(clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)).thenReturn(Optional.of(cliente));
        when(itemRepository.findByIdAndEmpresaId(itemId, empresaId)).thenReturn(Optional.of(item));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> {
            Factura factura = invocation.getArgument(0);
            savedFacturaRef.set(factura);
            return factura;
        });
        when(facturaLineaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(facturaTotalsService.recalcularTotalesFactura(any(UUID.class))).thenAnswer(invocation -> savedFacturaRef.get());

        facturaService.create(facturaRequest(empresaId, clienteId, itemId));

        ArgumentCaptor<Factura> facturaCaptor = ArgumentCaptor.forClass(Factura.class);
        ArgumentCaptor<List<FacturaLinea>> lineasCaptor = ArgumentCaptor.forClass(List.class);
        verify(facturaRepository).save(facturaCaptor.capture());
        verify(facturaLineaRepository).saveAll(lineasCaptor.capture());

        Factura factura = facturaCaptor.getValue();
        FacturaLinea linea = lineasCaptor.getValue().getFirst();
        assertEquals("Cliente historico", factura.getClienteNombreRazonSocial());
        assertEquals("12345678", factura.getClienteNrc());
        assertEquals("Actividad historica", factura.getClienteDescActividad());
        assertEquals("ITEM-001", linea.getItemCodigoInterno());
        assertEquals(59, linea.getItemUnidadMedida());
        assertEquals(2, linea.getItemTipo());
        assertEquals("SERVICIO", linea.getItemCategoria());
    }

    private FacturaRequest facturaRequest(UUID empresaId, UUID clienteId, UUID itemId) {
        return FacturaRequest.builder()
                .empresaId(empresaId)
                .clienteId(clienteId)
                .numero("F-TEST")
                .fechaEmision(LocalDate.now())
                .monedaCodigo("USD")
                .lineas(List.of(FacturaLineaRequest.builder()
                        .itemId(itemId)
                        .descripcion("Servicio")
                        .cantidad(BigDecimal.ONE)
                        .precioSinIva(BigDecimal.TEN)
                        .ivaPorcentaje(BigDecimal.valueOf(13))
                        .build()))
                .build();
    }

    private AtomicReference<Factura> prepareCreatePersistenceMocks() {
        AtomicReference<Factura> savedFacturaRef = new AtomicReference<>();
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> {
            Factura factura = invocation.getArgument(0);
            savedFacturaRef.set(factura);
            return factura;
        });
        when(facturaLineaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(facturaTotalsService.recalcularTotalesFactura(any(UUID.class))).thenAnswer(invocation -> savedFacturaRef.get());
        return savedFacturaRef;
    }

    private List<FacturaLinea> capturarLineasGuardadas() {
        ArgumentCaptor<List<FacturaLinea>> captor = ArgumentCaptor.forClass(List.class);
        verify(facturaLineaRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    private FacturaLineaRequest facturaLineaRequest(String descripcion,
                                                    BigDecimal cantidad,
                                                    BigDecimal precioSinIva,
                                                    BigDecimal ivaPorcentaje) {
        return FacturaLineaRequest.builder()
                .descripcion(descripcion)
                .cantidad(cantidad)
                .precioSinIva(precioSinIva)
                .ivaPorcentaje(ivaPorcentaje)
                .build();
    }

    private Factura facturaReadyForSend(UUID empresaId, UUID facturaId) {
        return Factura.builder()
                .id(facturaId)
                .empresaId(empresaId)
                .estado("LISTA_PARA_EMITIR")
                .numeroControl("DTE-01-M001P001-000000000000001")
                .codigoGeneracion(UUID.randomUUID().toString().toUpperCase())
                .build();
    }

    private FacturaLinea facturaLinea(UUID facturaId) {
        return FacturaLinea.builder()
                .id(UUID.randomUUID())
                .facturaId(facturaId)
                .subtotalSinIva(BigDecimal.TEN)
                .totalIva(new BigDecimal("1.30000000"))
                .totalConIva(new BigDecimal("11.30000000"))
                .build();
    }
}
