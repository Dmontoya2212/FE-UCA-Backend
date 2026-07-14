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
    private java.util.List<UUID> empresaIds;
    private String nombre;
    private String email;
    private String rol;
    private Boolean activo;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
