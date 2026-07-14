package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.entity.AuditLog;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.repository.AuditLogRepository;
import com.feuca.facturacion.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditServiceImplTest {

    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final AuditServiceImpl auditService = new AuditServiceImpl(auditLogRepository, usuarioRepository);

    @Test
    void recordLoginSuccessStoresUserAndDoesNotRequireTokenOrPassword() {
        UUID userId = UUID.randomUUID();
        when(usuarioRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(Usuario.builder().id(userId).email("user@example.com").build()));

        auditService.recordLogin(" USER@example.com ", true, "reason=credentials_valid");

        AuditLog log = capturedLog();
        assertEquals(userId, log.getUsuarioId());
        assertEquals("user@example.com", log.getUsuarioEmail());
        assertEquals("LOGIN", log.getAccion());
        assertEquals("Usuario", log.getRecurso());
        assertEquals("SUCCESS", log.getResultado());
        assertEquals("reason=credentials_valid", log.getMetadata());
        assertNotNull(log.getCreatedAt());
    }

    @Test
    void recordLoginFailureStoresOnlyNormalizedEmailWhenUserDoesNotExist() {
        when(usuarioRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        auditService.recordLogin("missing@example.com", false, "reason=InvalidCredentialsException");

        AuditLog log = capturedLog();
        assertEquals(null, log.getUsuarioId());
        assertEquals("missing@example.com", log.getUsuarioEmail());
        assertEquals("FAILURE", log.getResultado());
        assertEquals("reason=InvalidCredentialsException", log.getMetadata());
    }

    private AuditLog capturedLog() {
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        return captor.getValue();
    }
}
