package com.feuca.facturacion.dto.request.Factura;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
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
    private UUID empresaId;

    private UUID clienteId;

    @Size(max = 255, message = "El número no puede exceder 255 caracteres.")
    private String numero;

    @NotNull
    private LocalDate fechaEmision;

    @JsonProperty("moneda_codigo")
    @NotBlank(message = "La moneda es obligatoria.")
    @Size(max = 3, message = "La moneda debe usar un codigo de hasta 3 caracteres.")
    private String monedaCodigo;

    @JsonProperty("tipo_dte")
    @Size(max = 2, message = "El tipo DTE debe usar hasta 2 caracteres.")
    private String tipoDte;

    @NotEmpty
    @Size(max = 1000, message = "Una factura no puede contener más de 1000 líneas.")
    @Valid
    @JsonProperty("lineas")
    private List<FacturaLineaRequest> lineas;
}
