package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "factura_lineas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FacturaLinea {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "factura_id", columnDefinition = "uuid", nullable = false)
    private UUID facturaId;

    @Column(name = "item_id", columnDefinition = "uuid")
    private UUID itemId;

    @Column(name = "descripcion", nullable = false, length = 220)
    private String descripcion;

    @Column(name = "item_codigo_interno", length = 25)
    private String itemCodigoInterno;

    @Column(name = "item_unidad_medida")
    private Integer itemUnidadMedida;

    @Column(name = "item_tipo")
    private Integer itemTipo;

    @Column(name = "item_categoria")
    private String itemCategoria;

    @Column(name = "cantidad", precision = 12, scale = 2, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "precio_sin_iva", precision = 18, scale = 8, nullable = false)
    private BigDecimal precioSinIva;

    @Column(name = "iva_porcentaje", precision = 5, scale = 2, nullable = false)
    private BigDecimal ivaPorcentaje;

    @Column(name = "subtotal_sin_iva", precision = 18, scale = 8, nullable = false)
    private BigDecimal subtotalSinIva;

    @Column(name = "total_iva", precision = 18, scale = 8, nullable = false)
    private BigDecimal totalIva;

    @Column(name = "total_con_iva", precision = 18, scale = 8, nullable = false)
    private BigDecimal totalConIva;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
