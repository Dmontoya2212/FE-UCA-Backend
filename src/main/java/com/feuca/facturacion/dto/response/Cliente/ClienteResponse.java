package com.feuca.facturacion.dto.response.Cliente;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("empresa_id")
    private UUID empresaId;

    @JsonProperty("nombre_razon_social")
    private String nombreRazonSocial;

    @JsonProperty("nif_cif")
    private String nifCif;

    @JsonProperty("email")
    private String email;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("ciudad")
    private String ciudad;

    @JsonProperty("codigo_postal")
    private String codigoPostal;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @JsonProperty("deleted_at")
    private OffsetDateTime deletedAt;
}
