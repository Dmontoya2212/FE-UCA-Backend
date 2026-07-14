package com.feuca.facturacion.dto.request.Empresa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaIntegrationUpdateRequest {

    @JsonProperty("password")
    private String password;

    @JsonProperty("clave_primaria")
    private String clavePrimaria;

    @JsonProperty("token")
    private String token;

    @JsonProperty("expire_token")
    private String expireToken;
}
