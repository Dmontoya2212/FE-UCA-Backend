package com.feuca.facturacion.dto.response.IvaTasa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IvaTasaResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("empresa_id")
    private UUID empresaId;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("porcentaje")
    private BigDecimal porcentaje;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}
