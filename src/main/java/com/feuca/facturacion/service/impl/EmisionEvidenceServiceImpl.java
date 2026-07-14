package com.feuca.facturacion.service.impl;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionRequest;
import com.feuca.facturacion.dto.dte.HaciendaRecepcionResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.IntentoEmision;
import com.feuca.facturacion.enums.EstadoFactura;
import com.feuca.facturacion.repository.IntentoEmisionRepository;
import com.feuca.facturacion.service.EmisionEvidenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class EmisionEvidenceServiceImpl implements EmisionEvidenceService {

    private final IntentoEmisionRepository intentoEmisionRepository;

    public EmisionEvidenceServiceImpl(IntentoEmisionRepository intentoEmisionRepository) {
        this.intentoEmisionRepository = intentoEmisionRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IntentoEmision registrarInicio(Factura factura, String idempotencyKey, String ambiente, HaciendaRecepcionRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        long intentosPrevios = intentoEmisionRepository.countByFacturaId(factura.getId());
        return intentoEmisionRepository.save(IntentoEmision.builder()
                .id(UUID.randomUUID())
                .facturaId(factura.getId())
                .empresaId(factura.getEmpresaId())
                .codigoGeneracion(factura.getCodigoGeneracion())
                .numeroControl(factura.getNumeroControl())
                .ambiente(ambiente)
                .idempotencyKey(idempotencyKey)
                .estadoIntento(EstadoFactura.ENVIANDO.name())
                .requestJson(toJson(request))
                .numeroIntento(Math.toIntExact(intentosPrevios + 1))
                .fechaIntento(now)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarRespuesta(IntentoEmision intento, String estado, HaciendaRecepcionResponse response) {
        intento.setEstadoIntento(estado);
        intento.setFechaRespuesta(OffsetDateTime.now());
        intento.setUpdatedAt(OffsetDateTime.now());
        if (response != null) {
            intento.setCodigoHttp(response.getCodigoHttp());
            intento.setCodigoHacienda(response.getCodigoRespuesta());
            intento.setDescripcionRespuesta(response.getMensajeRespuesta());
            intento.setSelloRecibido(response.getSelloRecibido());
            intento.setResponseJson(response.getResponseJson());
            intento.setMensaje(response.getMensajeRespuesta());
        }
        intentoEmisionRepository.save(intento);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErrorTecnico(IntentoEmision intento, RuntimeException exception) {
        intento.setEstadoIntento("ERROR_TECNICO");
        intento.setFechaRespuesta(OffsetDateTime.now());
        intento.setUpdatedAt(OffsetDateTime.now());
        intento.setErrorTecnico(safeError(exception));
        intento.setMensaje(exception.getMessage());
        intentoEmisionRepository.save(intento);
    }

    private String toJson(HaciendaRecepcionRequest request) {
        if (request == null) {
            return null;
        }
        return "{"
                + "\"empresaId\":\"" + escape(request.getEmpresaId()) + "\","
                + "\"facturaId\":\"" + escape(request.getFacturaId()) + "\","
                + "\"numeroControl\":\"" + escape(request.getNumeroControl()) + "\","
                + "\"codigoGeneracion\":\"" + escape(request.getCodigoGeneracion()) + "\","
                + "\"documentoFirmado\":\"" + escape(request.getDocumentoFirmado()) + "\","
                + "\"dteJson\":\"" + escape(request.getDteJson()) + "\""
                + "}";
    }

    private String escape(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String safeError(RuntimeException exception) {
        String message = exception.getClass().getSimpleName() + ": " + exception.getMessage();
        if (message.length() <= 2000) {
            return message;
        }
        return message.substring(0, 2000);
    }
}
