package com.feuca.facturacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "usuario_email")
    private String usuarioEmail;

    @Column(name = "empresa_id", columnDefinition = "uuid")
    private UUID empresaId;

    @Column(name = "accion", nullable = false, length = 120)
    private String accion;

    @Column(name = "recurso", nullable = false, length = 120)
    private String recurso;

    @Column(name = "recurso_id", length = 120)
    private String recursoId;

    @Column(name = "resultado", nullable = false, length = 40)
    private String resultado;

    @Column(name = "ip", length = 80)
    private String ip;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
