package com.feuca.facturacion.dto.request.Empresa;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaRequest {
    @JsonProperty("nombre_legal")
    @NotBlank(message = "El nombre legal es obligatorio")
    @Size(max = 180, message = "El nombre legal no puede exceder 180 caracteres")
    private String nombreLegal;

    @JsonProperty("nombre_comercial")
    @Size(max = 180, message = "El nombre comercial no puede exceder 180 caracteres")
    private String nombreComercial;

    @JsonProperty("nif_cif")
    @Size(max = 30, message = "El NIF/CIF no puede exceder 30 caracteres")
    @Pattern(
            regexp = "^[A-Za-z0-9-]*$",
            message = "El NIF/CIF solo puede contener letras, números y guiones"
    )
    private String nifCif;

    @JsonProperty("email")
    @Email(message = "Debe ser un email válido")
    @Size(max = 180, message = "El email no puede exceder 180 caracteres")
    private String email;

    @JsonProperty("telefono")
    @Size(max = 40, message = "El teléfono no puede exceder 40 caracteres")
    private String telefono;

    @JsonProperty("direccion")
    @Size(max = 220, message = "La dirección no puede exceder 220 caracteres")
    private String direccion;

    @JsonProperty("ciudad")
    @Size(max = 120, message = "La ciudad no puede exceder 120 caracteres")
    private String ciudad;

    @JsonProperty("codigo_postal")
    @Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
    private String codigoPostal;

//    @JsonProperty("pais")
//    @Size(max = 80, message = "El país no puede exceder 80 caracteres")
//    private String pais;
}
