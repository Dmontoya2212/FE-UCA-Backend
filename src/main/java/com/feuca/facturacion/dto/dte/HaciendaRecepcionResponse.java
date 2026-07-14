package com.feuca.facturacion.dto.dte;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class HaciendaRecepcionResponse {
    private boolean aceptada;
    private Integer codigoHttp;
    private String selloRecibido;
    private OffsetDateTime fechaRecepcion;
    private String codigoRespuesta;
    private String mensajeRespuesta;
    private String errores;
    private String responseJson;
}
