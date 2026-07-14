package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.entity.AuditLog;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.repository.AuditLogRepository;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AuditService;
import com.feuca.facturacion.util.DataNormalizer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuditServiceImpl implements AuditService {

    private static final int MAX_USER_AGENT_LENGTH = 500;
    private static final int MAX_METADATA_LENGTH = 4000;

    private final AuditLogRepository auditLogRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository, UsuarioRepository usuarioRepository) {
        this.auditLogRepository = auditLogRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String accion, String recurso, String recursoId, UUID empresaId, String metadata) {
        record(accion, recurso, recursoId, empresaId, "SUCCESS", metadata, currentUserId().orElse(null), currentUserEmail().orElse(null));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String accion, String recurso, String recursoId, UUID empresaId, String metadata) {
        record(accion, recurso, recursoId, empresaId, "FAILURE", metadata, currentUserId().orElse(null), currentUserEmail().orElse(null));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLogin(String email, boolean success, String metadata) {
        String normalizedEmail = DataNormalizer.email(email);
        Optional<Usuario> user = normalizedEmail == null ? Optional.empty() : usuarioRepository.findByEmail(normalizedEmail);
        record("LOGIN", "Usuario", user.map(usuario -> usuario.getId().toString()).orElse(null), null,
                success ? "SUCCESS" : "FAILURE", metadata, user.map(Usuario::getId).orElse(null), normalizedEmail);
    }

    private void record(String accion,
                        String recurso,
                        String recursoId,
                        UUID empresaId,
                        String resultado,
                        String metadata,
                        UUID usuarioId,
                        String usuarioEmail) {
        auditLogRepository.save(AuditLog.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .usuarioEmail(limit(usuarioEmail, 255))
                .empresaId(empresaId)
                .accion(limit(accion, 120))
                .recurso(limit(recurso, 120))
                .recursoId(limit(recursoId, 120))
                .resultado(resultado)
                .ip(limit(currentIp(), 80))
                .userAgent(limit(currentUserAgent(), MAX_USER_AGENT_LENGTH))
                .metadata(limit(metadata, MAX_METADATA_LENGTH))
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private Optional<UUID> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(authentication.getName()));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private Optional<String> currentUserEmail() {
        return currentUserId()
                .flatMap(usuarioRepository::findById)
                .map(Usuario::getEmail);
    }

    private String currentIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String currentUserAgent() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getHeader("User-Agent");
    }

    private HttpServletRequest currentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        return attributes.getRequest();
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
