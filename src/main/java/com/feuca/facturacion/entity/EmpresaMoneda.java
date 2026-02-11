package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "empresa_monedas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(EmpresaMonedaId.class)
public class EmpresaMoneda {
    @Id
    @Column(name = "empresa_id", columnDefinition = "uuid")
    private UUID empresa_id;

    @Id
    @Column(name = "moneda_codigo", length = 3)
    private String moneda_codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", insertable = false, updatable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moneda_codigo", insertable = false, updatable = false)
    private Moneda moneda;
}
