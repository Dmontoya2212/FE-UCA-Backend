package com.feuca.facturacion.dto.response.Factura;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class FacturaResponse {
    private UUID id;
    private UUID empresaId;
    private UUID clienteId;
    private String numero;
    private LocalDate fechaEmision;
    private String estado;
    private String monedaCodigo;
    private BigDecimal subtotalSinIva;
    private BigDecimal totalIva;
    private BigDecimal totalConIva;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
