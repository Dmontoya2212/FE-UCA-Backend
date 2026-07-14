package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Auth.LoginRequest;
import com.feuca.facturacion.dto.response.Auth.LoginResponse;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.exception.Usuario.InvalidCredentialsException;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.AuditService;
import com.feuca.facturacion.service.JwtService;
import com.feuca.facturacion.service.OperationalMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuditService auditService = mock(AuditService.class);
    private final OperationalMetricsService operationalMetricsService = mock(OperationalMetricsService.class);
    private final AuthServiceImpl authService = new AuthServiceImpl(
            usuarioRepository,
            passwordEncoder,
            jwtService,
            auditService,
            operationalMetricsService
    );

    @Test
    void loginDoesNotSelectFirstEmpresaAsActiveEmpresa() {
        UUID userId = UUID.randomUUID();
        UUID empresaA = UUID.randomUUID();
        UUID empresaB = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(userId)
                .nombre("Usuario")
                .email("user@example.com")
                .passwordHash("hash")
                .activo(true)
                .rol(AccessControlService.ADMINISTRADOR)
                .empresas(List.of(
                        Empresa.builder().id(empresaA).build(),
                        Empresa.builder().id(empresaB).build()
                ))
                .build();
        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtService.generateToken(userId, "user@example.com", AccessControlService.ADMINISTRADOR))
                .thenReturn("token");

        LoginResponse response = authService.login(LoginRequest.builder()
                .email("USER@example.com ")
                .password("secret")
                .build());

        assertEquals(List.of(empresaA, empresaB), response.getEmpresaIds());
        assertEquals("token", response.getToken());
        verify(jwtService).generateToken(userId, "user@example.com", AccessControlService.ADMINISTRADOR);
        verify(auditService).recordLogin("USER@example.com ", true, "reason=credentials_valid");
    }

    @Test
    void loginWithoutAssignedEmpresasDoesNotGenerateFallbackEmpresaId() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(userId)
                .nombre("Usuario")
                .email("user@example.com")
                .passwordHash("hash")
                .activo(true)
                .rol(AccessControlService.USUARIO)
                .empresas(List.of())
                .build();
        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtService.generateToken(userId, "user@example.com", AccessControlService.USUARIO))
                .thenReturn("token");

        LoginResponse response = authService.login(LoginRequest.builder()
                .email("user@example.com")
                .password("secret")
                .build());

        assertTrue(response.getEmpresaIds().isEmpty());
        verify(jwtService).generateToken(userId, "user@example.com", AccessControlService.USUARIO);
    }

    @Test
    void loginDoesNotInferRoleFromLegacyEsAdmin() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre("Usuario")
                .email("user@example.com")
                .passwordHash("hash")
                .activo(true)
                .esAdmin(true)
                .rol(null)
                .empresas(List.of())
                .build();
        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(LoginRequest.builder()
                .email("user@example.com")
                .password("secret")
                .build()));

        verify(jwtService, never()).generateToken(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(auditService).recordLogin("user@example.com", false, "reason=InvalidCredentialsException");
        verify(operationalMetricsService).recordAuthenticationFailure("InvalidCredentialsException");
    }
}
