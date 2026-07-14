package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Usuario.UsuarioRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.exception.Usuario.UsuarioAlreadyExistsException;
import com.feuca.facturacion.repository.EmpresaRepository;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AccessControlService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceImplTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AccessControlService accessControlService = mock(AccessControlService.class);
    private final UsuarioServiceImpl service = new UsuarioServiceImpl(
            usuarioRepository,
            empresaRepository,
            passwordEncoder,
            accessControlService
    );

    @Test
    void createRejectsWeakPasswordBeforeSaving() {
        UUID empresaId = UUID.randomUUID();
        when(empresaRepository.findAllByIdInAndDeletedAtIsNull(List.of(empresaId)))
                .thenReturn(List.of(Empresa.builder().id(empresaId).build()));

        assertThrows(AccessDeniedException.class, () -> service.create(usuarioRequest(empresaId)
                .password("weak")
                .build()));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateEmailIgnoringCase() {
        UUID empresaId = UUID.randomUUID();
        when(empresaRepository.findAllByIdInAndDeletedAtIsNull(List.of(empresaId)))
                .thenReturn(List.of(Empresa.builder().id(empresaId).build()));
        when(usuarioRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThrows(UsuarioAlreadyExistsException.class, () -> service.create(usuarioRequest(empresaId).build()));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingOrDeletedEmpresa() {
        UUID empresaId = UUID.randomUUID();
        when(empresaRepository.findAllByIdInAndDeletedAtIsNull(List.of(empresaId))).thenReturn(List.of());

        assertThrows(AccessDeniedException.class, () -> service.create(usuarioRequest(empresaId).build()));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void createNormalizesEmailAndDoesNotAcceptPasswordHashFromClient() {
        UUID empresaId = UUID.randomUUID();
        when(empresaRepository.findAllByIdInAndDeletedAtIsNull(List.of(empresaId)))
                .thenReturn(List.of(Empresa.builder().id(empresaId).build()));
        when(passwordEncoder.encode("Strong1!")).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(usuarioRequest(empresaId).email("USER@Example.COM ").build());

        org.mockito.ArgumentCaptor<Usuario> captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario saved = captor.getValue();
        assertEquals("user@example.com", saved.getEmail());
        assertEquals("encoded", saved.getPasswordHash());
    }

    @Test
    void updateWithoutRolDoesNotEscalateLegacyEsAdmin() {
        UUID empresaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .email("user@example.com")
                .rol(AccessControlService.USUARIO)
                .esAdmin(false)
                .activo(true)
                .empresas(List.of(Empresa.builder().id(empresaId).build()))
                .build();
        when(usuarioRepository.findByIdAndEmpresasId(usuarioId, empresaId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(empresaId, usuarioId, UsuarioUpdateRequest.builder().build());

        assertEquals(AccessControlService.USUARIO, usuario.getRol());
        assertEquals(false, usuario.getEsAdmin());
    }

    @Test
    void updateRejectsDeactivatingLastActiveSuperAdmin() {
        UUID empresaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = activeSuperAdmin(usuarioId, empresaId);
        when(usuarioRepository.findByIdAndEmpresasId(usuarioId, empresaId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolAndActivoTrue(AccessControlService.SUPERADMIN)).thenReturn(1L);

        assertThrows(AccessDeniedException.class, () -> service.update(empresaId, usuarioId, UsuarioUpdateRequest.builder()
                .activo(false)
                .build()));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void updateRejectsChangingRoleOfLastActiveSuperAdmin() {
        UUID empresaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = activeSuperAdmin(usuarioId, empresaId);
        when(usuarioRepository.findByIdAndEmpresasId(usuarioId, empresaId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolAndActivoTrue(AccessControlService.SUPERADMIN)).thenReturn(1L);

        assertThrows(AccessDeniedException.class, () -> service.update(empresaId, usuarioId, UsuarioUpdateRequest.builder()
                .rol(AccessControlService.ADMINISTRADOR)
                .build()));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void updateRejectsRemovingAllEmpresaAccessFromLastActiveSuperAdmin() {
        UUID empresaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = activeSuperAdmin(usuarioId, empresaId);
        when(usuarioRepository.findByIdAndEmpresasId(usuarioId, empresaId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolAndActivoTrue(AccessControlService.SUPERADMIN)).thenReturn(1L);

        assertThrows(AccessDeniedException.class, () -> service.update(empresaId, usuarioId, UsuarioUpdateRequest.builder()
                .empresaIds(List.of())
                .build()));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deleteRejectsDeletingLastActiveSuperAdmin() {
        UUID empresaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = activeSuperAdmin(usuarioId, empresaId);
        when(usuarioRepository.findByIdAndEmpresasId(usuarioId, empresaId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolAndActivoTrue(AccessControlService.SUPERADMIN)).thenReturn(1L);

        assertThrows(AccessDeniedException.class, () -> service.delete(empresaId, usuarioId));

        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void updateAllowsDeactivatingSuperAdminWhenAnotherActiveSuperAdminExists() {
        UUID empresaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = activeSuperAdmin(usuarioId, empresaId);
        when(usuarioRepository.findByIdAndEmpresasId(usuarioId, empresaId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolAndActivoTrue(AccessControlService.SUPERADMIN)).thenReturn(2L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(empresaId, usuarioId, UsuarioUpdateRequest.builder()
                .activo(false)
                .build());

        assertEquals(false, usuario.getActivo());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    private UsuarioRequest.UsuarioRequestBuilder usuarioRequest(UUID empresaId) {
        return UsuarioRequest.builder()
                .empresaIds(List.of(empresaId))
                .nombre("Usuario")
                .email("user@example.com")
                .password("Strong1!")
                .rol(AccessControlService.USUARIO);
    }

    private Usuario activeSuperAdmin(UUID usuarioId, UUID empresaId) {
        return Usuario.builder()
                .id(usuarioId)
                .email("admin@example.com")
                .rol(AccessControlService.SUPERADMIN)
                .esAdmin(true)
                .activo(true)
                .empresas(List.of(Empresa.builder().id(empresaId).build()))
                .build();
    }
}
