package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Usuario.UsuarioRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.service.AccessControlService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioMapperRoleTest {

    @Test
    void createUsesRolAsAuthorizationSourceAndSyncsLegacyEsAdmin() {
        Usuario usuario = UsuarioMapper.toEntityCreate(UsuarioRequest.builder()
                .empresaIds(List.of(UUID.randomUUID()))
                .nombre("Operador")
                .email("user@example.com")
                .password("secret")
                .rol(AccessControlService.USUARIO)
                .build(), "hash");

        assertEquals(AccessControlService.USUARIO, usuario.getRol());
        assertFalse(usuario.getEsAdmin());
    }

    @Test
    void updateKeepsLegacyEsAdminWhenRolIsNotChanged() {
        Usuario usuario = Usuario.builder()
                .rol(AccessControlService.USUARIO)
                .esAdmin(false)
                .build();

        UsuarioMapper.applyProfileUpdate(usuario, UsuarioUpdateRequest.builder().build(), null);

        assertEquals(AccessControlService.USUARIO, usuario.getRol());
        assertFalse(usuario.getEsAdmin());
    }

    @Test
    void updateSyncsLegacyEsAdminFromNewRol() {
        Usuario usuario = Usuario.builder()
                .rol(AccessControlService.USUARIO)
                .esAdmin(false)
                .build();

        UsuarioMapper.applyAdminSecurityUpdate(usuario, UsuarioUpdateRequest.builder()
                .rol(AccessControlService.ADMINISTRADOR)
                .build());

        assertEquals(AccessControlService.ADMINISTRADOR, usuario.getRol());
        assertTrue(usuario.getEsAdmin());
    }
}
