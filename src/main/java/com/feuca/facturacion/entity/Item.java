package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", columnDefinition = "uuid")
    private UUID empresa_id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private ItemCategoria categoria;

    @Column(name = "iva_id", columnDefinition = "uuid")
    private UUID iva_id;

    @Column(name = "iva_porcentaje_snapshot", precision = 5, scale = 2)
    private BigDecimal iva_porcentaje_snapshot;

    @Column(name = "precio_sin_iva", precision = 18, scale = 8)
    private BigDecimal precio_sin_iva;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "created_at")
    private OffsetDateTime created_at;

    @Column(name = "updated_at")
    private OffsetDateTime updated_at;

    @Column(name = "deleted_at")
    private OffsetDateTime deleted_at;
}
