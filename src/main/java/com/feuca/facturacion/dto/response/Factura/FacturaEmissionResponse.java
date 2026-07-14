package com.feuca.facturacion.dto.response.Factura;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacturaEmissionResponse {
    private UUID id;
    private UUID empresaId;
    private String estado;
    private String tipoDte;
    private String codigoGeneracion;
    private String numeroControl;
    private String selloRecibido;
    private OffsetDateTime fechaRecepcion;
    private String haciendaCodigoRespuesta;
    private String haciendaMensajeRespuesta;
    private String haciendaErrores;
    private OffsetDateTime updatedAt;
}
