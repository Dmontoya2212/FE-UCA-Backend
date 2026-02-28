package com.feuca.facturacion.dto.response.Usuario;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UsuarioResponse {
    private UUID id;
    private UUID empresa_id;
    private String nombre;
    private String email;
    private Boolean es_admin;
    private Boolean activo;
    private OffsetDateTime created_at;
    private OffsetDateTime updated_at;
}
