package com.feuca.facturacion.dto.request.Cliente;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequest {

    @JsonProperty("empresa_id")
    @NotNull(message = "El ID de la empresa no puede ser nulo.")
    private UUID empresaId;

    @JsonProperty("nombre_razon_social")
    @NotBlank(message = "El nombre o razón social no puede estar vacío.")
    private String nombreRazonSocial;

    @JsonProperty("nif_cif")
    private String nifCif;

    @JsonProperty("email")
    @Email(message = "El email no tiene un formato válido.")
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
}
