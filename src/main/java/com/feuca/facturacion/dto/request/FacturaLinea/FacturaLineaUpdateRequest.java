package com.feuca.facturacion.dto.request.FacturaLinea;

import jakarta.validation.constraints.DecimalMin;
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

    @Positive
    private BigDecimal cantidad;

    @DecimalMin("0.0")
    private BigDecimal precioSinIva;

    @DecimalMin("0.0")
    private BigDecimal ivaPorcentaje;
}
