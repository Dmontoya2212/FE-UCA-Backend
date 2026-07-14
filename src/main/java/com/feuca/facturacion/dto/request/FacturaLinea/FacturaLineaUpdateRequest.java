package com.feuca.facturacion.dto.request.FacturaLinea;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FacturaLineaUpdateRequest {
    private UUID itemId;
    private String descripcion;

    @Positive(message = "La cantidad debe ser mayor a 0.")
    @Digits(integer = 10, fraction = 2, message = "La cantidad permite hasta 10 enteros y 2 decimales.")
    private BigDecimal cantidad;

    @DecimalMin(value = "0.00", message = "El precio sin IVA no puede ser menor a 0.")
    @Digits(integer = 10, fraction = 8, message = "El precio sin IVA permite hasta 10 enteros y 8 decimales.")
    private BigDecimal precioSinIva;

    @DecimalMin(value = "0.00", message = "El porcentaje de IVA no puede ser menor a 0.")
    @DecimalMax(value = "100.00", message = "El porcentaje de IVA no puede ser mayor a 100.")
    @Digits(integer = 3, fraction = 2, message = "El porcentaje de IVA permite hasta 3 enteros y 2 decimales.")
    private BigDecimal ivaPorcentaje;
}
