package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", columnDefinition = "uuid")
    private UUID empresaId;

    @Column(name = "nombre_razon_social")
    private String nombreRazonSocial;

    @Column(name = "nif_cif")
    private String nifCif;

    @Column(name = "email")
    private String email;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "tipo_documento", length = 2)
    private String tipoDocumento;

    @Column(name = "nrc", length = 8)
    private String nrc;

    @Column(name = "cod_actividad", length = 6)
    private String codActividad;

    @Column(name = "desc_actividad", length = 150)
    private String descActividad;

    @Column(name = "departamento", length = 2)
    private String departamento;

    @Column(name = "municipio", length = 2)
    private String municipio;

    @Column(name = "distrito", length = 4)
    private String distrito;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}