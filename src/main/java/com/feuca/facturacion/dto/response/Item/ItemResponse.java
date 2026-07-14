package com.feuca.facturacion.dto.response.Item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feuca.facturacion.entity.ItemCategoria;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("empresa_id")
    private UUID empresaId;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("categoria")
    private ItemCategoria categoria;

    @JsonProperty("iva")
    private ItemIvaResponse iva;

    @JsonProperty("precio_sin_iva")
    private BigDecimal precioSinIva;

    @JsonProperty("codigo_interno")
    private String codigoInterno;

    @JsonProperty("unidad_medida")
    private Integer unidadMedida;

    @JsonProperty("iva_porcentaje_snapshot")
    private BigDecimal ivaPorcentajeSnapshot;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @JsonProperty("deleted_at")
    private OffsetDateTime deletedAt;
}
