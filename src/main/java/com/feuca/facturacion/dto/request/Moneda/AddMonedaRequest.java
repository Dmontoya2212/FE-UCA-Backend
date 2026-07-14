package com.feuca.facturacion.dto.request.Moneda;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddMonedaRequest {

    @JsonProperty("codigo_moneda")
    @NotEmpty(message = "Debe enviar al menos una moneda")
    private List<String> codigos;

    @JsonProperty("moneda_principal")
    private String monedaPrincipal;
}
