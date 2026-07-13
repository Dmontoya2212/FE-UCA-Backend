package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_empresas",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "empresa_id")
    )
    private java.util.List<Empresa> empresas;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "es_admin", nullable = false)
    private Boolean esAdmin;

    @Column(name = "rol", length = 20)
    private String rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
