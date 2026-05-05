package com.feuca.facturacion.dto.response.Item;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemIvaResponse {

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("porcentaje")
    private BigDecimal porcentaje;
}
