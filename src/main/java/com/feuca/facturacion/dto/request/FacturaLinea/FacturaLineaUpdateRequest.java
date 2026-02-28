package com.feuca.facturacion.dto.request.FacturaLinea;

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
    private BigDecimal cantidad;
    private BigDecimal precioSinIva;
    private BigDecimal ivaPorcentaje;
}
