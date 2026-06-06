package com.feuca.facturacion.dto.request.Empresa;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaUpdateRequest {

    @JsonProperty("razon_social")
    private String razonSocial;

    @JsonProperty("nombre_legal")
    @Size(max = 180, message = "El nombre legal no puede exceder 180 caracteres")
    private String nombreLegal;

    @JsonProperty("nombre_comercial")
    @Size(max = 180, message = "El nombre comercial no puede exceder 180 caracteres")
    private String nombreComercial;

    @Pattern(
            regexp = "^\\d{4}-\\d{6}-\\d{3}-\\d{1}$",
            message = "El NIT debe tener el formato XXXX-XXXXXX-XXX-X (Ej: 0614-123456-101-1)"
    )
    @JsonProperty("nit")
    private String nit;

    @JsonProperty("registro")
    private String registro;

    @JsonProperty("actividad_economica")
    private String actividadEconomica;

    @JsonProperty("sector_empresa")
    private String sectorEmpresa;

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

    @JsonProperty("pais")
    private String pais;

    @JsonProperty("usuario")
    private String usuario;

    @JsonProperty("password")
    private String password;

    @JsonProperty("clave_primaria")
    private String clavePrimaria;

    @JsonProperty("token")
    private String token;

    @JsonProperty("expire_token")
    private String expireToken;
}
