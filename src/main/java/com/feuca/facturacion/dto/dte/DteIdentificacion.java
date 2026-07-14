package com.feuca.facturacion.dto.dte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DteIdentificacion {

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("ambiente")
    private String ambiente;

    @JsonProperty("tipoDte")
    private String tipoDte;

    @JsonProperty("numeroControl")
    private String numeroControl;

    @JsonProperty("codigoGeneracion")
    private String codigoGeneracion;

    @JsonProperty("tipoModelo")
    private Integer tipoModelo;

    @JsonProperty("tipoOperacion")
    private Integer tipoOperacion;

    @JsonProperty("tipoContingencia")
    private Integer tipoContingencia;

    @JsonProperty("motivoContin")
    private String motivoContin;

    @JsonProperty("fecEmi")
    private String fecEmi;

    @JsonProperty("horEmi")
    private String horEmi;

    @JsonProperty("tipoMoneda")
    private String tipoMoneda;
}
