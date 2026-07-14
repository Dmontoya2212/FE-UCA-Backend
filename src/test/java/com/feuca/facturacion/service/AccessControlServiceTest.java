package com.feuca.facturacion.service;

import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.exception.GlobalExceptionHandler;
import com.feuca.facturacion.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccessControlServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final AccessControlService accessControlService = new AccessControlService(usuarioRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void superAdminCanAccessAnyEmpresa() {
        authenticate(UUID.randomUUID(), AccessControlService.SUPERADMIN);

        assertDoesNotThrow(() -> accessControlService.requireEmpresaAccess(UUID.randomUUID()));
    }

    @Test
    void usuarioCanAccessAssignedEmpresa() {
        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        authenticate(userId, AccessControlService.USUARIO);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuarioWithEmpresa(userId, empresaId)));

        assertDoesNotThrow(() -> accessControlService.requireEmpresaAccess(empresaId));
    }

    @Test
    void usuarioCannotAccessUnassignedEmpresa() {
        UUID userId = UUID.randomUUID();
        authenticate(userId, AccessControlService.USUARIO);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuarioWithEmpresa(userId, UUID.randomUUID())));

        assertThrows(AccessDeniedException.class,
                () -> accessControlService.requireEmpresaAccess(UUID.randomUUID()));
    }

    @Test
    void invalidRoleIsRejected() {
        assertThrows(AccessDeniedException.class,
                () -> accessControlService.validateRole("ROOT"));
    }

    @Test
    void inactiveAuthenticatedUserIsRejected() {
        UUID userId = UUID.randomUUID();
        authenticate(userId, AccessControlService.USUARIO);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(Usuario.builder()
                .id(userId)
                .activo(false)
                .empresas(List.of(Empresa.builder().id(UUID.randomUUID()).build()))
                .build()));

        assertThrows(AccessDeniedException.class,
                () -> accessControlService.requireEmpresaAccess(UUID.randomUUID()));
    }

    @Test
    void usuarioReceivesForbiddenWhenOperatingOnAnotherEmpresa() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID empresaAsignada = UUID.randomUUID();
        UUID empresaAjena = UUID.randomUUID();
        authenticate(userId, AccessControlService.USUARIO);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuarioWithEmpresa(userId, empresaAsignada)));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new EmpresaAccessProbeController(accessControlService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/test/empresa/{empresaId}", empresaAjena))
                .andExpect(status().isForbidden());
    }

    private void authenticate(UUID userId, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                )
        );
    }

    private Usuario usuarioWithEmpresa(UUID userId, UUID empresaId) {
        return Usuario.builder()
                .id(userId)
                .activo(true)
                .empresas(List.of(Empresa.builder().id(empresaId).build()))
                .build();
    }

    @RestController
    @RequestMapping("/test/empresa")
    private static class EmpresaAccessProbeController {
        private final AccessControlService accessControlService;

        private EmpresaAccessProbeController(AccessControlService accessControlService) {
            this.accessControlService = accessControlService;
        }

        @GetMapping("/{empresaId}")
        ResponseEntity<Void> operate(@PathVariable UUID empresaId) {
            accessControlService.validarAccesoAEmpresa(empresaId);
            return ResponseEntity.ok().build();
        }
    }
}
