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

    @Column(name = "cliente_nombre_razon_social")
    private String clienteNombreRazonSocial;

    @Column(name = "cliente_nif_cif")
    private String clienteNifCif;

    @Column(name = "cliente_direccion")
    private String clienteDireccion;

    @Column(name = "numero_control", length = 31)
    private String numeroControl;

    @Column(name = "codigo_generacion", length = 36)
    private String codigoGeneracion;

    @Column(name = "condicion_operacion")
    private Integer condicionOperacion;

    @Column(name = "sello_recibido")
    private String selloRecibido;

    @Column(name = "fecha_recepcion")
    private OffsetDateTime fechaRecepcion;

    @Column(name = "tipo_dte", length = 2)
    private String tipoDte;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
