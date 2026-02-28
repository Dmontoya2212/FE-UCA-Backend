package com.feuca.facturacion.dto.request.Factura;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaUpdateRequest {
    private UUID clienteId;
    private String numero;
    private LocalDate fechaEmision;
    private String monedaCodigo;
}
