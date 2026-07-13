package com.feuca.facturacion.dto.dte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DteResumen {

    @JsonProperty("totalNoSuj")
    private BigDecimal totalNoSuj;

    @JsonProperty("totalExenta")
    private BigDecimal totalExenta;

    @JsonProperty("totalGravada")
    private BigDecimal totalGravada;

    @JsonProperty("subTotalVentas")
    private BigDecimal subTotalVentas;

    @JsonProperty("descuNoSuj")
    private BigDecimal descuNoSuj;

    @JsonProperty("descuExenta")
    private BigDecimal descuExenta;

    @JsonProperty("descuGravada")
    private BigDecimal descuGravada;

    @JsonProperty("porcentajeDescuento")
    private BigDecimal porcentajeDescuento;

    @JsonProperty("totalDescu")
    private BigDecimal totalDescu;

    @JsonProperty("tributos")
    private List<DteTributoResumen> tributos;

    @JsonProperty("subTotal")
    private BigDecimal subTotal;

    @JsonProperty("ivaRete")
    private BigDecimal ivaRete;

    @JsonProperty("montoTotalOperacion")
    private BigDecimal montoTotalOperacion;

    @JsonProperty("totalNoGravado")
    private BigDecimal totalNoGravado;

    @JsonProperty("totalPagar")
    private BigDecimal totalPagar;

    @JsonProperty("totalLetras")
    private String totalLetras;

    @JsonProperty("totalIva")
    private BigDecimal totalIva;

    @JsonProperty("saldoFavor")
    private BigDecimal saldoFavor;

    @JsonProperty("condicionOperacion")
    private Integer condicionOperacion;

    @JsonProperty("pagos")
    private List<DtePago> pagos;

    @JsonProperty("numPagoElectronico")
    private String numPagoElectronico;

    @JsonProperty("observaciones")
    private String observaciones;
}
