package com.feuca.facturacion.dto.request.Item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feuca.facturacion.entity.ItemCategoria;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemUpdateRequest {

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("categoria")
    private ItemCategoria categoria;

    @JsonProperty("iva_id")
    private UUID ivaId;

    @JsonProperty("precio_sin_iva")
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
