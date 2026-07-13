package com.feuca.facturacion.dto.request.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UsuarioRequest {

    @NotNull(message = "Debe proporcionar al menos una empresa")
    private java.util.List<UUID> empresaIds;

    @NotBlank(message = "El nombre del usuario es obligatorio")
    private String nombre;

    @Email(message = "Correo Invalido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private Boolean esAdmin;

    private String rol;

}
