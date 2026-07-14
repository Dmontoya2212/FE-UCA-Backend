package com.feuca.facturacion.security;

import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.expiration-ms=900000",
        "jwt.issuer=feuca-facturacion"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    private static final String JWT_SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void withoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/facturacion/empresa"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/facturacion/empresa")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenReturnsUnauthorized() throws Exception {
        JwtService expiredJwtService = new JwtService(JWT_SECRET, -1000, "feuca-facturacion");
        String expiredToken = expiredJwtService.generateToken(
                UUID.randomUUID(),
                "user@example.com",
                AccessControlService.USUARIO
        );

        mockMvc.perform(get("/api/v1/facturacion/empresa")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void undeclaredRouteOutsideApiIsNotPublic() throws Exception {
        mockMvc.perform(get("/ruta-no-declarada"))
                .andExpect(status().isUnauthorized());
    }
}
