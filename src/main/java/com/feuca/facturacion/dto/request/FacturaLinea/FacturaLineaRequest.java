package com.feuca.facturacion.dto.request.FacturaLinea;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaLineaRequest {

    @JsonProperty("item_id")
    private UUID itemId;

    @NotBlank
    @JsonProperty("descripcion")
    private String descripcion;

    @NotNull
    @Positive
    @JsonProperty("cantidad")
    private BigDecimal cantidad;

    @NotNull
    @DecimalMin("0.0")
    @JsonProperty("precio_sin_iva")
    private BigDecimal precioSinIva;

    @NotNull
    @DecimalMin("0.0")
    @JsonProperty("iva_porcentaje")
    private BigDecimal ivaPorcentaje;
}