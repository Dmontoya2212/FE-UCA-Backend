package com.feuca.facturacion.dto.request.IvaTasa;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IvaTasaUpdateRequest {

    @JsonProperty("nombre")
    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombre;

    @JsonProperty("porcentaje")
    @NotNull(message = "El porcentaje no puede ser nulo.")
//    @DecimalMin(value = "0.00", message = "El porcentaje no puede ser menor a 0.")
//    @DecimalMax(value = "100.00", message = "El porcentaje no puede ser mayor a 100.")
    private BigDecimal porcentaje;
}
