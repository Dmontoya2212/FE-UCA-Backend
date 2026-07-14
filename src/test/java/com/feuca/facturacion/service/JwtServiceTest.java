package com.feuca.facturacion.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String TEST_SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";
    private static final String TEST_ISSUER = "feuca-test";

    @Test
    void generatedTokenDoesNotContainEmpresaId() {
        JwtService jwtService = new JwtService(
                TEST_SECRET,
                60000,
                TEST_ISSUER
        );
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, "user@example.com", AccessControlService.USUARIO);
        Claims claims = jwtService.parseToken(token);

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("user@example.com", claims.get("email", String.class));
        assertEquals(AccessControlService.USUARIO, claims.get("rol", String.class));
        assertEquals(TEST_ISSUER, claims.getIssuer());
        assertFalse(claims.containsKey("empresaId"));
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtService jwtService = new JwtService(TEST_SECRET, -1000, TEST_ISSUER);
        String token = jwtService.generateToken(UUID.randomUUID(), "user@example.com", AccessControlService.USUARIO);

        assertFalse(jwtService.isValid(token));
        assertThrows(JwtException.class, () -> jwtService.parseToken(token));
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        JwtService issuer = new JwtService(TEST_SECRET, 60000, TEST_ISSUER);
        JwtService verifier = new JwtService(
                "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcd",
                60000,
                TEST_ISSUER
        );
        String token = issuer.generateToken(UUID.randomUUID(), "user@example.com", AccessControlService.USUARIO);

        assertFalse(verifier.isValid(token));
        assertThrows(JwtException.class, () -> verifier.parseToken(token));
    }

    @Test
    void tokenWithDifferentIssuerIsRejected() {
        JwtService issuer = new JwtService(TEST_SECRET, 60000, "issuer-a");
        JwtService verifier = new JwtService(TEST_SECRET, 60000, "issuer-b");
        String token = issuer.generateToken(UUID.randomUUID(), "user@example.com", AccessControlService.USUARIO);

        assertFalse(verifier.isValid(token));
        assertThrows(JwtException.class, () -> verifier.parseToken(token));
    }

    @Test
    void weakSecretIsRejectedAtStartup() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", 60000, TEST_ISSUER));
    }
}
