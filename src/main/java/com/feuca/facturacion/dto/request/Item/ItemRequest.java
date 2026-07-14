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
    @DecimalMin(value = "0.00", message = "El precio sin IVA no puede ser menor a 0.")
    @Digits(integer = 10, fraction = 8, message = "El precio sin IVA permite hasta 10 enteros y 8 decimales.")
    private BigDecimal precioSinIva;

    @JsonProperty("codigo_interno")
    @Size(max = 25, message = "El codigo interno no puede exceder 25 caracteres.")
    private String codigoInterno;

    @JsonProperty("unidad_medida")
    @Min(value = 1, message = "La unidad de medida DTE debe ser mayor o igual a 1.")
    @Max(value = 99, message = "La unidad de medida DTE debe ser menor o igual a 99.")
    private Integer unidadMedida;

    @JsonProperty("activo")
    private Boolean activo;
}
