package com.feuca.facturacion.dto.dte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DtePago {

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("montoPago")
    private BigDecimal montoPago;

    @JsonProperty("referencia")
    private String referencia;

    @JsonProperty("plazo")
    private String plazo;

    @JsonProperty("periodo")
    private Integer periodo;
}
