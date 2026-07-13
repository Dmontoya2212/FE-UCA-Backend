package com.feuca.facturacion.dto.response.Auth;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private UUID id;
    private UUID empresaId;
    private String nombre;
    private String email;
    private Boolean esAdmin;
    private String rol;
    private String token;
}
