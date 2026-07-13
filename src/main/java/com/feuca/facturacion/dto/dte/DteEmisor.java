package com.feuca.facturacion.dto.dte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DteEmisor {

    @JsonProperty("nit")
    private String nit;

    @JsonProperty("nrc")
    private String nrc;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("codActividad")
    private String codActividad;

    @JsonProperty("descActividad")
    private String descActividad;

    @JsonProperty("nombreComercial")
    private String nombreComercial;

    @JsonProperty("direccion")
    private DteDireccion direccion;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("correo")
    private String correo;

    @JsonProperty("codEstable")
    private String codEstable;

    @JsonProperty("codPuntoVenta")
    private String codPuntoVenta;
}
