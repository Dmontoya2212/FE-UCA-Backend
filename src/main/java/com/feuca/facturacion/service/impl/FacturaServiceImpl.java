package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionRequest;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;
import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.response.Factura.FacturaEmissionResponse;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.IntentoEmision;
import com.feuca.facturacion.enums.EstadoFactura;
import com.feuca.facturacion.exception.Cliente.ClienteNotFoundException;
import com.feuca.facturacion.exception.Factura.FacturaAlreadyExistsException;
import com.feuca.facturacion.exception.Factura.FacturaNoEditableException;
import com.feuca.facturacion.exception.Factura.FacturaNotFoundException;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.exception.Item.ItemNotFoundException;
import com.feuca.facturacion.mapper.FacturaMapper;
import com.feuca.facturacion.repository.ClienteRepository;
import com.feuca.facturacion.repository.EmpresaMonedaRepository;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.repository.ItemRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.DteJsonValidationService;
import com.feuca.facturacion.service.DteService;
import com.feuca.facturacion.service.EmisionEvidenceService;
import com.feuca.facturacion.service.FacturaService;
import com.feuca.facturacion.service.FacturaStateValidator;
import com.feuca.facturacion.service.FacturaTotalsService;
import com.feuca.facturacion.service.HaciendaService;
import com.feuca.facturacion.service.OperationalMetricsService;
import com.feuca.facturacion.util.DataNormalizer;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FacturaServiceImpl implements FacturaService {
    private final FacturaRepository facturaRepository;
    private final FacturaLineaRepository facturaLineaRepository;
    private final ClienteRepository clienteRepository;
    private final ItemRepository itemRepository;
    private final EmpresaMonedaRepository empresaMonedaRepository;
    private final DteService dteService;
    private final DteJsonValidationService dteJsonValidationService;
    private final AccessControlService accessControlService;
    private final FacturaTotalsService facturaTotalsService;
    private final HaciendaService haciendaService;
    private final EmisionEvidenceService emisionEvidenceService;
    private final FacturaStateValidator facturaStateValidator;
    private final OperationalMetricsService operationalMetricsService;

    public FacturaServiceImpl(FacturaRepository facturaRepository,
                              FacturaLineaRepository facturaLineaRepository,
                              ClienteRepository clienteRepository,
                              ItemRepository itemRepository,
                              EmpresaMonedaRepository empresaMonedaRepository,
                              DteService dteService,
                              DteJsonValidationService dteJsonValidationService,
                              AccessControlService accessControlService,
                              FacturaTotalsService facturaTotalsService,
                              HaciendaService haciendaService,
                              EmisionEvidenceService emisionEvidenceService,
                              FacturaStateValidator facturaStateValidator,
                              OperationalMetricsService operationalMetricsService) {
        this.facturaRepository = facturaRepository;
        this.facturaLineaRepository = facturaLineaRepository;
        this.clienteRepository = clienteRepository;
        this.itemRepository = itemRepository;
        this.empresaMonedaRepository = empresaMonedaRepository;
        this.dteService = dteService;
        this.dteJsonValidationService = dteJsonValidationService;
        this.accessControlService = accessControlService;
        this.facturaTotalsService = facturaTotalsService;
        this.haciendaService = haciendaService;
        this.emisionEvidenceService = emisionEvidenceService;
        this.facturaStateValidator = facturaStateValidator;
        this.operationalMetricsService = operationalMetricsService;
    }

    @Override
    @Transactional
    public FacturaResponse create(FacturaRequest request) {
        accessControlService.requireEmpresaAccess(request.getEmpresaId());
        validateClienteBelongsToEmpresa(request.getClienteId(), request.getEmpresaId());
        validateLineItemsBelongToEmpresa(request.getLineas(), request.getEmpresaId());
        request.setMonedaCodigo(validateMonedaAsignadaAEmpresa(request.getMonedaCodigo(), request.getEmpresaId()));
        Factura factura = FacturaMapper.toEntityCreate(request);

        String clienteNombre = null;
        if (factura.getClienteId() != null) {
            clienteRepository.findByIdAndEmpresaId(factura.getClienteId(), factura.getEmpresaId())
                    .ifPresent(c -> applyClienteSnapshot(factura, c));
        }

        Factura savedFactura = facturaRepository.save(factura);
        UUID savedFacturaId = savedFactura.getId();

        List<FacturaLinea> lineas = request.getLineas().stream()
                .map(lineaReq -> FacturaMapper.toLineaEntity(lineaReq, savedFacturaId))
                .peek(linea -> applyItemSnapshot(linea, request.getEmpresaId()))
                .collect(Collectors.toList());

        List<FacturaLinea> savedLineas = facturaLineaRepository.saveAll(lineas);

        savedFactura = facturaTotalsService.recalcularTotalesFactura(savedFacturaId);

        clienteNombre = null;
        if (savedFactura.getClienteId() != null) {
            clienteNombre = clienteRepository.findByIdAndEmpresaId(savedFactura.getClienteId(), savedFactura.getEmpresaId())
                    .map(c -> c.getNombreRazonSocial())
                    .orElse(null);
        }

        return FacturaMapper.toResponse(savedFactura, savedLineas, clienteNombre);
    }

    @Override
    public FacturaResponse getById(UUID empresaId, UUID facturaId) {
        accessControlService.requireEmpresaAccess(empresaId);
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));
        validateClienteBelongsToEmpresa(f.getClienteId(), empresaId);

        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);

        String clienteNombre = null;
        if (f.getClienteId() != null) {
            clienteNombre = clienteRepository.findByIdAndEmpresaId(f.getClienteId(), empresaId)
                    .map(c -> c.getNombreRazonSocial())
                    .orElse(null);
        }

        return FacturaMapper.toResponse(f, lineas, clienteNombre);
    }

    @Override
    public List<FacturaResponse> getAllByEmpresa(UUID empresaId) {
        accessControlService.requireEmpresaAccess(empresaId);
        return facturaRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(f -> {
                    List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(f.getId());
                    String clienteNombre = null;
                    if (f.getClienteId() != null) {
                        clienteNombre = clienteRepository.findByIdAndEmpresaId(f.getClienteId(), empresaId)
                                .map(c -> c.getNombreRazonSocial())
                                .orElse(null);
                    }
                    return FacturaMapper.toResponse(f, lineas, clienteNombre);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FacturaResponse update(UUID empresaId, UUID facturaId, FacturaUpdateRequest request) {
        accessControlService.requireEmpresaAccess(empresaId);
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        if (!EstadoFactura.fromValue(f.getEstado()).esEditable()) {
            throw new FacturaNoEditableException("La factura ya fue enviada y no se puede editar.");
        }
        validateClienteBelongsToEmpresa(request.getClienteId(), empresaId);
        if (request.getMonedaCodigo() != null) {
            request.setMonedaCodigo(validateMonedaAsignadaAEmpresa(request.getMonedaCodigo(), empresaId));
        }

        FacturaMapper.applyUpdate(f, request);
        if (request.getClienteId() != null) {
            clienteRepository.findByIdAndEmpresaId(request.getClienteId(), empresaId)
                    .ifPresent(c -> applyClienteSnapshot(f, c));
        }
        Factura saved = facturaRepository.save(f);

        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);
        String clienteNombre = null;
        if (saved.getClienteId() != null) {
            clienteNombre = clienteRepository.findByIdAndEmpresaId(saved.getClienteId(), empresaId)
                    .map(c -> c.getNombreRazonSocial())
                    .orElse(null);
        }

        return FacturaMapper.toResponse(saved, lineas, clienteNombre);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('SUPERADMIN','ADMINISTRADOR')")
    public void delete(UUID empresaId, UUID facturaId) {
        accessControlService.requireAdministradorOrSuperAdmin();
        accessControlService.requireEmpresaAccess(empresaId);
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        if (!EstadoFactura.fromValue(f.getEstado()).esEditable()) {
            throw new FacturaNoEditableException("La factura ya fue enviada y no se puede eliminar.");
        }

        facturaLineaRepository.deleteAllByFacturaId(facturaId);
        facturaRepository.delete(f);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('SUPERADMIN','ADMINISTRADOR')")
    public FacturaEmissionResponse prepararParaEnvio(UUID empresaId, UUID facturaId) {
        accessControlService.requireAdministradorOrSuperAdmin();
        accessControlService.requireEmpresaAccess(empresaId);
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        if (!EstadoFactura.fromValue(f.getEstado()).esEditable()) {
            throw new FacturaNoEditableException("La factura ya fue enviada.");
        }
        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);
        validateFacturaHasLines(lineas);

        DteFacturaElectronica dte = dteService.generarDte(empresaId, facturaId);
        dteJsonValidationService.validarYSerializar(dte);
        haciendaService.validarDte(dte);

        f = facturaRepository.findById(facturaId).orElseThrow();
        cambiarEstado(f, EstadoFactura.LISTA_PARA_EMITIR);
        Factura saved = facturaRepository.save(f);

        return FacturaMapper.toEmissionResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('SUPERADMIN','ADMINISTRADOR')")
    public FacturaEmissionResponse enviarAHacienda(UUID empresaId, UUID facturaId) {
        return enviarAHacienda(empresaId, facturaId, null);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('SUPERADMIN','ADMINISTRADOR')")
    public FacturaEmissionResponse enviarAHacienda(UUID empresaId, UUID facturaId, String idempotencyKey) {
        long inicioEmisionNanos = System.nanoTime();
        accessControlService.requireAdministradorOrSuperAdmin();
        accessControlService.requireEmpresaAccess(empresaId);
        Factura f = facturaRepository.findAndLockByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        EstadoFactura estado = EstadoFactura.fromValue(f.getEstado());
        if (estado == EstadoFactura.EMITIDA) {
            throw new FacturaNoEditableException("La factura ya fue emitida por Hacienda.");
        }
        if (estado == EstadoFactura.ENVIANDO) {
            throw new FacturaNoEditableException("La factura ya tiene un envio en proceso.");
        }
        if (estado != EstadoFactura.BORRADOR
                && estado != EstadoFactura.LISTA_PARA_EMITIR
                && estado != EstadoFactura.RECHAZADA) {
            throw new FacturaNoEditableException("La factura no esta en un estado valido para envio.");
        }
        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);
        validateFacturaHasLines(lineas);

        DteFacturaElectronica dte = dteService.generarDte(empresaId, facturaId);
        String dteJson = dteJsonValidationService.validarYSerializar(dte);
        haciendaService.validarDte(dte);
        String documentoFirmado = haciendaService.firmarDte(dte);

        f = facturaRepository.findById(facturaId).orElseThrow();
        cambiarEstado(f, EstadoFactura.ENVIANDO);
        facturaRepository.save(f);
        HaciendaRecepcionRequest recepcionRequest = HaciendaRecepcionRequest.builder()
                .empresaId(empresaId)
                .facturaId(facturaId)
                .numeroControl(f.getNumeroControl())
                .codigoGeneracion(f.getCodigoGeneracion())
                .dte(dte)
                .dteJson(dteJson)
                .documentoFirmado(documentoFirmado)
                .build();
        IntentoEmision intento = emisionEvidenceService.registrarInicio(
                f,
                idempotencyKey,
                obtenerAmbiente(dte),
                recepcionRequest
        );
        log.info(
                "Inicio de emision DTE facturaId={} codigoGeneracion={} intento={}",
                facturaId,
                f.getCodigoGeneracion(),
                intento.getNumeroIntento()
        );

        HaciendaRecepcionResponse response;
        Timer.Sample haciendaTimer = operationalMetricsService.startHaciendaTimer();
        try {
            response = haciendaService.enviarDte(recepcionRequest);
        } catch (RuntimeException exception) {
            operationalMetricsService.stopHaciendaTimer(haciendaTimer, "ERROR_TECNICO");
            operationalMetricsService.recordEmissionFailure("ERROR_TECNICO");
            emisionEvidenceService.registrarErrorTecnico(intento, exception);
            log.warn(
                    "Emision DTE con error tecnico facturaId={} codigoGeneracion={} intento={} resultado=ERROR_TECNICO duracionMs={}",
                    facturaId,
                    f.getCodigoGeneracion(),
                    intento.getNumeroIntento(),
                    duracionMs(inicioEmisionNanos)
            );
            throw exception;
        }

        applyHaciendaResponse(f, response);
        if (haciendaService.respuestaAceptada(response)) {
            operationalMetricsService.stopHaciendaTimer(haciendaTimer, "ACEPTADA");
            cambiarEstado(f, EstadoFactura.EMITIDA);
            f.setSelloRecibido(response.getSelloRecibido());
            f.setFechaRecepcion(response.getFechaRecepcion());
        } else {
            operationalMetricsService.stopHaciendaTimer(haciendaTimer, "RECHAZADA");
            operationalMetricsService.recordEmissionFailure("RECHAZADA");
            cambiarEstado(f, EstadoFactura.RECHAZADA);
        }
        emisionEvidenceService.registrarRespuesta(intento, f.getEstado(), response);
        log.info(
                "Emision DTE finalizada facturaId={} codigoGeneracion={} intento={} resultado={} duracionMs={} codigoExterno={}",
                facturaId,
                f.getCodigoGeneracion(),
                intento.getNumeroIntento(),
                f.getEstado(),
                duracionMs(inicioEmisionNanos),
                response != null ? response.getCodigoRespuesta() : null
        );
        Factura saved = facturaRepository.save(f);

        return FacturaMapper.toEmissionResponse(saved);
    }

    private void validateClienteBelongsToEmpresa(UUID clienteId, UUID empresaId) {
        if (clienteId == null) {
            return;
        }
        clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado en la empresa indicada."));
    }

    private void validateLineItemsBelongToEmpresa(List<com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest> lineas, UUID empresaId) {
        if (lineas == null) {
            return;
        }
        lineas.stream()
                .map(com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest::getItemId)
                .filter(id -> id != null)
                .forEach(itemId -> validateItemBelongsToEmpresa(itemId, empresaId));
    }

    private void validateItemBelongsToEmpresa(UUID itemId, UUID empresaId) {
        var item = itemRepository.findByIdAndEmpresaId(itemId, empresaId)
                .orElseThrow(() -> new ItemNotFoundException("Item no encontrado en la empresa indicada."));
        if (Boolean.FALSE.equals(item.getActivo())) {
            throw new FacturaValidationException("No se puede usar un item inactivo en la factura.");
        }
    }

    private String validateMonedaAsignadaAEmpresa(String monedaCodigo, UUID empresaId) {
        String codigoNormalizado = DataNormalizer.identifier(monedaCodigo);
        if (codigoNormalizado == null || codigoNormalizado.isBlank()) {
            throw new FacturaValidationException("La moneda de la factura es obligatoria.");
        }
        if (!empresaMonedaRepository.existsByEmpresa_idAndMoneda_codigo(empresaId, codigoNormalizado)) {
            throw new FacturaValidationException("La moneda de la factura no esta asignada a la empresa.");
        }
        return codigoNormalizado;
    }

    private void applyClienteSnapshot(Factura factura, com.feuca.facturacion.entity.Cliente cliente) {
        factura.setClienteNombreRazonSocial(cliente.getNombreRazonSocial());
        factura.setClienteNifCif(cliente.getNifCif());
        factura.setClienteDireccion(cliente.getDireccion());
        factura.setClienteTipoDocumento(cliente.getTipoDocumento());
        factura.setClienteNrc(cliente.getNrc());
        factura.setClienteCodActividad(cliente.getCodActividad());
        factura.setClienteDescActividad(cliente.getDescActividad());
        factura.setClienteDepartamento(cliente.getDepartamento());
        factura.setClienteMunicipio(cliente.getMunicipio());
        factura.setClienteDistrito(cliente.getDistrito());
        factura.setClienteTelefono(cliente.getTelefono());
        factura.setClienteEmail(cliente.getEmail());
    }

    private void applyItemSnapshot(FacturaLinea linea, UUID empresaId) {
        if (linea.getItemId() == null) {
            return;
        }
        itemRepository.findByIdAndEmpresaId(linea.getItemId(), empresaId)
                .ifPresent(item -> {
                    linea.setItemCodigoInterno(item.getCodigoInterno());
                    linea.setItemUnidadMedida(item.getUnidadMedida());
                    linea.setItemTipo(item.getCategoria() == com.feuca.facturacion.entity.ItemCategoria.PRODUCTO ? 1 : 2);
                    linea.setItemCategoria(item.getCategoria() != null ? item.getCategoria().name() : null);
                });
    }

    private void validateFacturaHasLines(List<FacturaLinea> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            throw new FacturaValidationException("La factura debe tener al menos una linea antes de emitirse.");
        }
    }

    private void applyHaciendaResponse(Factura factura, HaciendaRecepcionResponse response) {
        if (response == null) {
            factura.setHaciendaMensajeRespuesta("Hacienda no devolvio respuesta.");
            return;
        }
        factura.setHaciendaCodigoRespuesta(response.getCodigoRespuesta());
        factura.setHaciendaMensajeRespuesta(response.getMensajeRespuesta());
        factura.setHaciendaErrores(response.getErrores());
        factura.setHaciendaResponseJson(response.getResponseJson());
    }

    private String obtenerAmbiente(DteFacturaElectronica dte) {
        if (dte == null || dte.getIdentificacion() == null) {
            return null;
        }
        return dte.getIdentificacion().getAmbiente();
    }

    private void cambiarEstado(Factura factura, EstadoFactura estadoDestino) {
        facturaStateValidator.validarTransicion(factura.getEstado(), estadoDestino);
        factura.setEstado(estadoDestino.name());
    }

    private long duracionMs(long inicioNanos) {
        return java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNanos);
    }
}
