package com.feuca.facturacion.dto.request.FacturaLinea;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
    @Digits(integer = 10, fraction = 2, message = "La cantidad permite hasta 10 enteros y 2 decimales.")
    @JsonProperty("cantidad")
    private BigDecimal cantidad;

    @NotNull
    @DecimalMin(value = "0.00", message = "El precio sin IVA no puede ser menor a 0.")
    @Digits(integer = 10, fraction = 8, message = "El precio sin IVA permite hasta 10 enteros y 8 decimales.")
    @JsonProperty("precio_sin_iva")
    private BigDecimal precioSinIva;

    @NotNull
    @DecimalMin(value = "0.00", message = "El porcentaje de IVA no puede ser menor a 0.")
    @DecimalMax(value = "100.00", message = "El porcentaje de IVA no puede ser mayor a 100.")
    @Digits(integer = 3, fraction = 2, message = "El porcentaje de IVA permite hasta 3 enteros y 2 decimales.")
    @JsonProperty("iva_porcentaje")
    private BigDecimal ivaPorcentaje;
}
