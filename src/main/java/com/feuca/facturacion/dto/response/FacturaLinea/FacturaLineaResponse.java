package com.feuca.facturacion.dto.response.FacturaLinea;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FacturaLineaResponse {
    private UUID id;
    private UUID facturaId;
    private UUID itemId;

    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioSinIva;
    private BigDecimal ivaPorcentaje;

    private BigDecimal subtotalSinIva;
    private BigDecimal totalIva;
    private BigDecimal totalConIva;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
