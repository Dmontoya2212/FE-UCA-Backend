package com.feuca.facturacion.dto.request.Empresa;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaIntegrationUpdateRequest {

    @JsonProperty("password")
    @Size(max = 4096, message = "La credencial no puede exceder 4096 caracteres")
    private String password;

    @JsonProperty("clave_primaria")
    @Size(max = 4096, message = "La clave primaria no puede exceder 4096 caracteres")
    private String clavePrimaria;

    @JsonProperty("token")
    @Size(max = 8192, message = "El token no puede exceder 8192 caracteres")
    private String token;

    @JsonProperty("expire_token")
    @Size(max = 255, message = "La expiración del token no puede exceder 255 caracteres")
    private String expireToken;
}
