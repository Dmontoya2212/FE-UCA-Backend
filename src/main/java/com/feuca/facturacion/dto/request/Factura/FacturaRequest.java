package com.feuca.facturacion.dto.request.Factura;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FacturaRequest {
    @NotNull
    private UUID empresaId;

    private UUID clienteId;

    private String numero;

    @NotNull
    private LocalDate fechaEmision;

    @JsonProperty("moneda_codigo")
    private String monedaCodigo;

    @JsonProperty("tipo_dte")
    private String tipoDte;

    @NotEmpty
    @Valid
    @JsonProperty("lineas")
    private List<FacturaLineaRequest> lineas;
}
