package com.feuca.facturacion.dto.request.Factura;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaUpdateRequest {
    private UUID clienteId;
    private LocalDate fechaEmision;

    @Size(max = 3, message = "La moneda debe usar un codigo de hasta 3 caracteres.")
    private String monedaCodigo;

    @Size(max = 2, message = "El tipo DTE debe usar hasta 2 caracteres.")
    private String tipoDte;
}
