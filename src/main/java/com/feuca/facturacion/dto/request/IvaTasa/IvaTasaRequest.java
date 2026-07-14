package com.feuca.facturacion.dto.request.IvaTasa;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IvaTasaRequest {

    @JsonProperty("empresa_id")
    private UUID empresaId;

    @JsonProperty("nombre")
    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombre;

    @JsonProperty("porcentaje")
    @NotNull(message = "El porcentaje no puede ser nulo.")
    @DecimalMin(value = "0.00", message = "El porcentaje no puede ser menor a 0.")
    @DecimalMax(value = "100.00", message = "El porcentaje no puede ser mayor a 100.")
    @Digits(integer = 3, fraction = 2, message = "El porcentaje permite hasta 3 enteros y 2 decimales.")
    private BigDecimal porcentaje;

    @JsonProperty("activo")
    private Boolean activo;
}
