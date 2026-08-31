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
    private UUID empresaId;

    @JsonProperty("nombre_razon_social")
    @NotBlank(message = "El nombre o razón social no puede estar vacío.")
    @Size(max = 255, message = "El nombre o razón social no puede exceder 255 caracteres.")
    private String nombreRazonSocial;

    @JsonProperty("nif_cif")
    @NotBlank(message = "El NIF/CIF es obligatorio.")
    @Size(max = 255, message = "El NIF/CIF no puede exceder 255 caracteres.")
    private String nifCif;

    @JsonProperty("email")
    @Email(message = "El email no tiene un formato válido.")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres.")
    private String email;

    @JsonProperty("direccion")
    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres.")
    private String direccion;

    @JsonProperty("ciudad")
    @Size(max = 255, message = "La ciudad no puede exceder 255 caracteres.")
    private String ciudad;

    @JsonProperty("codigo_postal")
    @Size(max = 255, message = "El código postal no puede exceder 255 caracteres.")
    private String codigoPostal;

    @JsonProperty("telefono")
    @Size(max = 255, message = "El teléfono no puede exceder 255 caracteres.")
    private String telefono;

    @JsonProperty("tipo_documento")
    @Size(max = 2, message = "El tipo de documento no puede exceder 2 caracteres.")
    private String tipoDocumento;

    @JsonProperty("nrc")
    @Size(max = 8, message = "El NRC no puede exceder 8 caracteres.")
    private String nrc;

    @JsonProperty("cod_actividad")
    @Size(max = 6, message = "El código de actividad no puede exceder 6 caracteres.")
    private String codActividad;

    @JsonProperty("desc_actividad")
    @Size(max = 150, message = "La descripción de actividad no puede exceder 150 caracteres.")
    private String descActividad;

    @JsonProperty("departamento")
    @Size(max = 2, message = "El departamento no puede exceder 2 caracteres.")
    private String departamento;

    @JsonProperty("municipio")
    @Size(max = 2, message = "El municipio no puede exceder 2 caracteres.")
    private String municipio;

    @JsonProperty("distrito")
    @Size(max = 4, message = "El distrito no puede exceder 4 caracteres.")
    private String distrito;

    @JsonProperty("activo")
    private Boolean activo;
}
