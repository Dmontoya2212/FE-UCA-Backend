package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Usuario.UsuarioRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.dto.response.Usuario.UsuarioResponse;
import com.feuca.facturacion.entity.Usuario;

import java.util.UUID;

public class UsuarioMapper {

    private UsuarioMapper(){}

    public static Usuario toEntityCreate(UsuarioRequest req, String passwordHash) {
        return Usuario.builder()
                .id(UUID.randomUUID()) // para no depender del default del DB
                .empresaId(req.getEmpresaId())
                .nombre(req.getNombre())
                .email(req.getEmail())
                .password_hash(passwordHash)
                .es_admin(req.getEsAdmin() != null ? req.getEsAdmin() : false)
                .activo(true)
                .build();
    }

    public static void applyUpdate(Usuario u, UsuarioUpdateRequest req, String newPasswordHashOrNull) {
        if (req.getNombre() != null) u.setNombre(req.getNombre());
        if (req.getEmail() != null) u.setEmail(req.getEmail());
        if (req.getEsAdmin() != null) u.setEs_admin(req.getEsAdmin());
        if (req.getActivo() != null) u.setActivo(req.getActivo());
        if (newPasswordHashOrNull != null) u.setPassword_hash(newPasswordHashOrNull);
    }

    public static UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .empresa_id(u.getEmpresaId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .es_admin(u.getEs_admin())
                .activo(u.getActivo())
                .created_at(u.getCreated_at())
                .updated_at(u.getUpdated_at())
                .build();
    }

}
