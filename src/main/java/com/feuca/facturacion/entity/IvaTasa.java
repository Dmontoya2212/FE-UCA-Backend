package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "iva_tasas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IvaTasa {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", columnDefinition = "uuid")
    private UUID empresa_id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "porcentaje", precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "created_at")
    private OffsetDateTime created_at;

    @Column(name = "updated_at")
    private OffsetDateTime updated_at;
}
