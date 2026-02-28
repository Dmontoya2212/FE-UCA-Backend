package com.feuca.facturacion.dto.request.FacturaLinea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FacturaLineaRequest {
    @NotNull
    private UUID facturaId;

    private UUID itemId;

    @NotBlank
    private String descripcion;

    @NotNull
    private BigDecimal cantidad;

    @NotNull
    private BigDecimal precioSinIva;

    @NotNull
    private BigDecimal ivaPorcentaje;
}
