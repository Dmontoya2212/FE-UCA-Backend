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
    @NotNull(message = "El ID de la empresa no puede ser nulo.")
    private UUID empresaId;

    @JsonProperty("nombre")
    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombre;

    @JsonProperty("porcentaje")
    @NotNull(message = "El porcentaje no puede ser nulo.")
    private BigDecimal porcentaje;
}
