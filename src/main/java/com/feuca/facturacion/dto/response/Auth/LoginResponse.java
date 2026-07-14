package com.feuca.facturacion.dto.response.Auth;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private UUID id;
    private List<UUID> empresaIds;
    private String nombre;
    private String email;
    private String rol;
    private String token;
}
