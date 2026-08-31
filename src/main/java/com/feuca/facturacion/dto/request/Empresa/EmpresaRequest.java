package com.feuca.facturacion.dto.request.Empresa;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaRequest {

    @JsonProperty("razon_social")
    @Size(max = 255, message = "La razón social no puede exceder 255 caracteres")
    private String razonSocial;

    @JsonProperty("nombre_legal")
    @NotBlank(message = "El nombre legal es obligatorio")
    @Size(max = 180, message = "El nombre legal no puede exceder 180 caracteres")
    private String nombreLegal;

    @JsonProperty("nombre_comercial")
    @Size(max = 180, message = "El nombre comercial no puede exceder 180 caracteres")
    private String nombreComercial;

    @NotBlank(message = "El NIT es obligatorio")
    @Pattern(
            regexp = "^\\d{4}-\\d{6}-\\d{3}-\\d{1}$",
            message = "El NIT debe tener el formato XXXX-XXXXXX-XXX-X (Ej: 0614-123456-101-1)"
    )
    @JsonProperty("nit")
    private String nit;

    @JsonProperty("registro")
    @Size(max = 255, message = "El registro no puede exceder 255 caracteres")
    private String registro;

    @JsonProperty("actividad_economica")
    @Size(max = 255, message = "La actividad económica no puede exceder 255 caracteres")
    private String actividadEconomica;

    @JsonProperty("cod_actividad")
    @Size(max = 6, message = "El código de actividad no puede exceder 6 caracteres")
    private String codActividad;

    @JsonProperty("sector_empresa")
    @Size(max = 255, message = "El sector no puede exceder 255 caracteres")
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
    @Size(max = 255, message = "El país no puede exceder 255 caracteres")
    private String pais;

    @JsonProperty("departamento")
    @Size(max = 2, message = "El departamento no puede exceder 2 caracteres")
    private String departamento;

    @JsonProperty("municipio")
    @Size(max = 2, message = "El municipio no puede exceder 2 caracteres")
    private String municipio;

    @JsonProperty("distrito")
    @Size(max = 4, message = "El distrito no puede exceder 4 caracteres")
    private String distrito;

    @JsonProperty("cod_establecimiento")
    @Size(max = 4, message = "El código de establecimiento no puede exceder 4 caracteres")
    private String codEstablecimiento;

    @JsonProperty("cod_punto_venta")
    @Size(max = 15, message = "El código de punto de venta no puede exceder 15 caracteres")
    private String codPuntoVenta;

    @JsonProperty("usuario")
    @Size(max = 255, message = "El usuario de integración no puede exceder 255 caracteres")
    private String usuario;

    @JsonProperty("password")
    @Size(max = 4096, message = "La credencial no puede exceder 4096 caracteres")
    private String password;

    @JsonProperty("clave_primaria")
    @Size(max = 4096, message = "La clave primaria no puede exceder 4096 caracteres")
    private String clavePrimaria;

    @JsonProperty("token")
    @Size(max = 8192, message = "El token no puede exceder 8192 caracteres")
    private String token;

    @JsonProperty("expire_token")
    @Size(max = 255, message = "La expiración del token no puede exceder 255 caracteres")
    private String expireToken;
}
