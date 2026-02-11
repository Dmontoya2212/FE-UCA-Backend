package com.feuca.facturacion.dto.request.Item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feuca.facturacion.entity.ItemCategoria;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {

    @JsonProperty("empresa_id")
    @NotNull(message = "El ID de la empresa no puede ser nulo.")
    private UUID empresaId;

    @JsonProperty("nombre")
    @NotBlank(message = "El nombre del item no puede estar vacío.")
    private String nombre;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("categoria")
    @NotNull(message = "La categoría no puede ser nula.")
    private ItemCategoria categoria;

    @JsonProperty("iva_id")
    @NotNull(message = "El ID del IVA no puede ser nulo.")
    private UUID ivaId;

    @JsonProperty("precio_sin_iva")
    @NotNull(message = "El precio sin IVA no puede ser nulo.")
//  @DecimalMin(value = "0.00", message = "El precio sin IVA no puede ser menor a 0.")
    private BigDecimal precioSinIva;

    @JsonProperty("activo")
    private Boolean activo;
}
