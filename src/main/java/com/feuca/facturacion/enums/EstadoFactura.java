package com.feuca.facturacion.enums;

import java.util.Arrays;

public enum EstadoFactura {
    BORRADOR("Editable; aun no debe considerarse documento emitido."),
    LISTA_PARA_EMITIR("DTE generado y validado internamente; no enviado ni aceptado por Hacienda."),
    ENVIANDO("Solicitud de emision enviada; pendiente de respuesta definitiva de Hacienda."),
    EMITIDA("Documento aceptado por Hacienda con sello de recepcion u otro comprobante valido."),
    RECHAZADA("Hacienda rechazo el documento o la respuesta valida indica rechazo."),
    CONTINGENCIA("Documento manejado bajo flujo de contingencia autorizado."),
    ANULADA("Documento invalidado mediante el flujo legal correspondiente."),
    PAGADA("Documento emitido y registrado como pagado.");

    private final String descripcion;

    EstadoFactura(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esEditable() {
        return this == BORRADOR;
    }

    public boolean requiereRespuestaAceptadaDeHacienda() {
        return this == EMITIDA;
    }

    public static EstadoFactura fromValue(String value) {
        return Arrays.stream(values())
                .filter(estado -> estado.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de factura no reconocido: " + value));
    }
}
