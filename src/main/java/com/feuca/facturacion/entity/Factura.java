package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Factura {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "cliente_id")
    private UUID clienteId;

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "moneda_codigo")
    private String monedaCodigo;

    @Column(name = "subtotal_sin_iva")
    private BigDecimal subtotalSinIva;

    @Column(name = "total_iva")
    private BigDecimal totalIva;

    @Column(name = "total_con_iva")
    private BigDecimal totalConIva;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
