package com.feuca.facturacion.dto.response.Empresa;

import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaResponse {

    private UUID id;
    private String razonSocial;
    private String nombreLegal;
    private String nombreComercial;
    private String nit;
    private String registro;
    private String actividadEconomica;
    private String sectorEmpresa;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String codigoPostal;
    private String pais;
    private String usuario;
    private String token;
    private String expireToken;
    private List<MonedaResponse> monedas;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}