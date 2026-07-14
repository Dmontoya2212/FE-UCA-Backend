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

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "nombre_legal")
    private String nombreLegal;

    @Column(name = "nombre_comercial")
    private String nombreComercial;

    @Column(name = "nit", unique = true)
    private String nit;

    @Column(name = "registro")
    private String registro;

    @Column(name = "actividad_economica")
    private String actividadEconomica;

    @Column(name = "cod_actividad", length = 6)
    private String codActividad;

    @Column(name = "sector_empresa")
    private String sectorEmpresa;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "telefono", unique = true)
    private String telefono;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    @Column(name = "departamento", length = 2)
    private String departamento;

    @Column(name = "municipio", length = 2)
    private String municipio;

    @Column(name = "distrito", length = 4)
    private String distrito;

    @Column(name = "cod_establecimiento", length = 4)
    private String codEstablecimiento;

    @Column(name = "cod_punto_venta", length = 15)
    private String codPuntoVenta;

    @Column(name = "pais")
    private String pais;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    @Column(name = "clave_primaria", columnDefinition = "TEXT")
    private String clavePrimaria;

    @Column(name = "token", columnDefinition = "TEXT")
    private String token;

    @Column(name = "expire_token")
    private String expireToken;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
