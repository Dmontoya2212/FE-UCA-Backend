package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "dte_secuencias", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"empresa_id", "tipo_dte"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DteSecuencia {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", columnDefinition = "uuid", nullable = false)
    private UUID empresaId;

    @Column(name = "tipo_dte", length = 2, nullable = false)
    private String tipoDte;

    @Column(name = "ultimo_correlativo", nullable = false)
    private Long ultimoCorrelativo;
}
