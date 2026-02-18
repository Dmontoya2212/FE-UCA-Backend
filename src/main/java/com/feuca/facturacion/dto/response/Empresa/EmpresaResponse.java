package com.feuca.facturacion.dto.response.Empresa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaResponse {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("nombre_legal")
    private String nombreLegal;

    @JsonProperty("nombre_comercial")
    private String nombreComercial;

    @JsonProperty("nif_cif")
    private String nifCif;

    @JsonProperty("email")
    private String email;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("ciudad")
    private String ciudad;

    @JsonProperty("codigo_postal")
    private String codigoPostal;

    @JsonProperty("pais")
    private String pais;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
