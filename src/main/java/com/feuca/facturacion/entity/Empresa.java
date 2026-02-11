package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "empresas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "nombre_legal")
    private String nombre_legal;

    @Column(name = "nombre_comercial")
    private String nombre_comercial;

    @Column(name = "nif_cif")
    private String nif_cif;

    @Column(name = "email")
    private String email;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "codigo_postal")
    private String codigo_postal;

    @Column(name = "pais")
    private String pais;

    @Column(name = "created_at")
    private OffsetDateTime created_at;

    @Column(name = "updated_at")
    private OffsetDateTime updated_at;

    @Column(name = "deleted_at")
    private OffsetDateTime deleted_at;
}
