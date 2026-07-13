package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Usuario.UsuarioRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.dto.response.Usuario.UsuarioResponse;
import com.feuca.facturacion.entity.Usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.feuca.facturacion.entity.Empresa;
import java.util.stream.Collectors;

public class UsuarioMapper {

    private UsuarioMapper(){}

    public static Usuario toEntityCreate(UsuarioRequest req, String passwordHash) {
        String rol = req.getRol();
        if (rol == null || rol.isBlank()) {
            rol = (req.getEsAdmin() != null && req.getEsAdmin()) ? "ADMINISTRADOR" : "USUARIO";
        }

        java.util.List<Empresa> empresas = req.getEmpresaIds() != null ? 
            req.getEmpresaIds().stream().map(id -> Empresa.builder().id(id).build()).collect(Collectors.toList()) : 
            new java.util.ArrayList<>();

        return Usuario.builder()
                .id(UUID.randomUUID())
                .empresas(empresas)
                .nombre(req.getNombre())
                .email(req.getEmail())
                .passwordHash(passwordHash)
                .esAdmin(req.getEsAdmin() != null ? req.getEsAdmin() : false)
                .rol(rol)
                .activo(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static void applyUpdate(Usuario u, UsuarioUpdateRequest req, String newPasswordHashOrNull) {
        if (req.getNombre() != null) u.setNombre(req.getNombre());
        if (req.getEmail() != null) u.setEmail(req.getEmail());
        if (req.getEsAdmin() != null) u.setEsAdmin(req.getEsAdmin());
        if (req.getRol() != null) u.setRol(req.getRol());
        if (req.getActivo() != null) u.setActivo(req.getActivo());
        if (newPasswordHashOrNull != null) u.setPasswordHash(newPasswordHashOrNull);
        if (req.getEmpresaIds() != null) {
            u.setEmpresas(req.getEmpresaIds().stream()
                .map(id -> Empresa.builder().id(id).build())
                .collect(Collectors.toList()));
        }
        u.setUpdatedAt(OffsetDateTime.now());
    }

    public static UsuarioResponse toResponse(Usuario u) {
        java.util.List<UUID> empresaIds = u.getEmpresas() != null ? 
            u.getEmpresas().stream().map(Empresa::getId).collect(Collectors.toList()) : 
            new java.util.ArrayList<>();

        return UsuarioResponse.builder()
                .id(u.getId())
                .empresaIds(empresaIds)
                .nombre(u.getNombre())
                .email(u.getEmail())
                .esAdmin(u.getEsAdmin())
                .rol(u.getRol())
                .activo(u.getActivo())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
