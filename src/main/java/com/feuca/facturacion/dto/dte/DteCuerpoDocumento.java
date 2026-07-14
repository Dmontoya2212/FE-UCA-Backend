package com.feuca.facturacion.dto.dte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DteCuerpoDocumento {

    @JsonProperty("numItem")
    private Integer numItem;

    @JsonProperty("tipoItem")
    private Integer tipoItem;

    @JsonProperty("numeroDocumento")
    private String numeroDocumento;

    @JsonProperty("cantidad")
    private BigDecimal cantidad;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("codTributo")
    private String codTributo;

    @JsonProperty("uniMedida")
    private Integer uniMedida;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("precioUni")
    private BigDecimal precioUni;

    @JsonProperty("montoDescu")
    private BigDecimal montoDescu;

    @JsonProperty("ventaNoSuj")
    private BigDecimal ventaNoSuj;

    @JsonProperty("ventaExenta")
    private BigDecimal ventaExenta;

    @JsonProperty("ventaGravada")
    private BigDecimal ventaGravada;

    @JsonProperty("tributos")
    private List<String> tributos;

    @JsonProperty("psv")
    private BigDecimal psv;

    @JsonProperty("noGravado")
    private BigDecimal noGravado;

    @JsonProperty("ivaItem")
    private BigDecimal ivaItem;
}
