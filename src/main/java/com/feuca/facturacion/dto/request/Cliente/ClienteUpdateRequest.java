package com.feuca.facturacion.dto.request.Cliente;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteUpdateRequest {

    @JsonProperty("nombre_razon_social")
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
