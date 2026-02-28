package com.feuca.facturacion.dto.request.Usuario;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UsuarioUpdateRequest {

    private String nombre;

    @Email(message = "Correo Invalido")
    private String email;

    private Boolean esAdmin;
    private Boolean activo;
    private String password;

}
