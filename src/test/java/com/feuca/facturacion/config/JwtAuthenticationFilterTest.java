package com.feuca.facturacion.config;

import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";
    private static final String ISSUER = "feuca-test";

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final JwtService jwtService = new JwtService(SECRET, 60000, ISSUER);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, usuarioRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesCurrentDatabaseRoleInsteadOfRoleStoredInToken() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "user@example.com", AccessControlService.USUARIO);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(Usuario.builder()
                .id(userId)
                .activo(true)
                .rol(AccessControlService.ADMINISTRADOR)
                .build()));

        filter.doFilter(requestWithToken(token), new MockHttpServletResponse(), new MockFilterChain());

        assertEquals(userId.toString(), SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals(AccessControlService.ADMINISTRADOR,
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void inactiveUserTokenDoesNotAuthenticate() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "user@example.com", AccessControlService.SUPERADMIN);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(Usuario.builder()
                .id(userId)
                .activo(false)
                .rol(AccessControlService.SUPERADMIN)
                .build()));

        filter.doFilter(requestWithToken(token), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void malformedTokenDoesNotAuthenticate() throws Exception {
        filter.doFilter(requestWithToken("malformed.token"), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
