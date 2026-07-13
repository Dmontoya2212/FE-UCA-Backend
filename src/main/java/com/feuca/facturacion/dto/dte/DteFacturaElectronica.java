package com.feuca.facturacion.dto.dte;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DteFacturaElectronica {

    @JsonProperty("identificacion")
    private DteIdentificacion identificacion;

    @JsonProperty("documentoRelacionado")
    private Object documentoRelacionado; // null

    @JsonProperty("emisor")
    private DteEmisor emisor;

    @JsonProperty("receptor")
    private DteReceptor receptor;

    @JsonProperty("otrosDocumentos")
    private Object otrosDocumentos; // null

    @JsonProperty("ventaTercero")
    private Object ventaTercero; // null

    @JsonProperty("cuerpoDocumento")
    private List<DteCuerpoDocumento> cuerpoDocumento;

    @JsonProperty("resumen")
    private DteResumen resumen;

    @JsonProperty("apendice")
    private Object apendice; // null
}
