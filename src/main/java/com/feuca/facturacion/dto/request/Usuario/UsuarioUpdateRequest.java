package com.feuca.facturacion.dto.request.Usuario;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UsuarioUpdateRequest {

    private String nombre;
    private java.util.List<java.util.UUID> empresaIds;

    @Email(message = "Correo Invalido")
    private String email;

    private String rol;
    private Boolean activo;
    private String password;

}
