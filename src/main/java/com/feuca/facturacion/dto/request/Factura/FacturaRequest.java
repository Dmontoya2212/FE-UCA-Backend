package com.feuca.facturacion.dto.request.Factura;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FacturaRequest {
    @NotNull
    private UUID empresaId;

    private UUID clienteId;

    @NotBlank
    private String numero;

    @NotNull
    private LocalDate fechaEmision;

    private String monedaCodigo;
}
