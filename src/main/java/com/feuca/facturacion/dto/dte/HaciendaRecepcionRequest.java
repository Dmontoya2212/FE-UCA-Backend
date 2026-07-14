package com.feuca.facturacion.dto.dte;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class HaciendaRecepcionRequest {
    private UUID empresaId;
    private UUID facturaId;
    private String numeroControl;
    private String codigoGeneracion;
    private DteFacturaElectronica dte;
    private String dteJson;
    private String documentoFirmado;
}
