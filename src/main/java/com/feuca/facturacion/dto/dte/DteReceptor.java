package com.feuca.facturacion.dto.dte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DteReceptor {

    @JsonProperty("tipoDocumento")
    private String tipoDocumento;

    @JsonProperty("numDocumento")
    private String numDocumento;

    @JsonProperty("nrc")
    private String nrc;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("codActividad")
    private String codActividad;

    @JsonProperty("descActividad")
    private String descActividad;

    @JsonProperty("direccion")
    private DteDireccion direccion;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("correo")
    private String correo;
}
