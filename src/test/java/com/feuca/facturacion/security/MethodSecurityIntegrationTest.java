package com.feuca.facturacion.security;

import com.feuca.facturacion.service.EmpresaService;
import com.feuca.facturacion.service.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.expiration-ms=900000",
        "jwt.issuer=feuca-test"
})
@ActiveProfiles("test")
class MethodSecurityIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmpresaService empresaService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usuarioServiceCannotBeInvokedDirectlyByNonSuperadmin() {
        authenticateAs("ADMINISTRADOR");

        assertThrows(AccessDeniedException.class, () -> usuarioService.create(null));
    }

    @Test
    void empresaAdminOperationCannotBeInvokedDirectlyByNonSuperadmin() {
        authenticateAs("USUARIO");

        assertThrows(AccessDeniedException.class, () -> empresaService.create(null));
    }

    private void authenticateAs(String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-id", null, authority)
        );
    }
}
