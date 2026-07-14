package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Usuario.UsuarioRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.dto.response.Usuario.UsuarioResponse;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.util.DataNormalizer;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.feuca.facturacion.entity.Empresa;
import java.util.stream.Collectors;

public class UsuarioMapper {

    private UsuarioMapper(){}

    public static Usuario toEntityCreate(UsuarioRequest req, String passwordHash) {
        String rol = req.getRol();

        java.util.List<Empresa> empresas = req.getEmpresaIds() != null ? 
            req.getEmpresaIds().stream().map(id -> Empresa.builder().id(id).build()).collect(Collectors.toList()) : 
            new java.util.ArrayList<>();

        return Usuario.builder()
                .id(UUID.randomUUID())
                .empresas(empresas)
                .nombre(DataNormalizer.displayText(req.getNombre()))
                .email(DataNormalizer.email(req.getEmail()))
                .passwordHash(passwordHash)
                .esAdmin(esAdminLegacyFromRol(rol))
                .rol(rol)
                .activo(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static void applyProfileUpdate(Usuario u, UsuarioUpdateRequest req, String newPasswordHashOrNull) {
        if (req.getNombre() != null) u.setNombre(DataNormalizer.displayText(req.getNombre()));
        if (req.getEmail() != null) u.setEmail(DataNormalizer.email(req.getEmail()));
        if (newPasswordHashOrNull != null) u.setPasswordHash(newPasswordHashOrNull);
        u.setUpdatedAt(OffsetDateTime.now());
    }

    public static void applyAdminSecurityUpdate(Usuario u, UsuarioUpdateRequest req) {
        if (req.getRol() != null) {
            u.setRol(req.getRol());
            u.setEsAdmin(esAdminLegacyFromRol(req.getRol()));
        }
        if (req.getActivo() != null) u.setActivo(req.getActivo());
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
                .rol(u.getRol())
                .activo(u.getActivo())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    private static boolean esAdminLegacyFromRol(String rol) {
        return "SUPERADMIN".equals(rol) || "ADMINISTRADOR".equals(rol);
    }
}
