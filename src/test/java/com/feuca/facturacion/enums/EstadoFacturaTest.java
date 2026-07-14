package com.feuca.facturacion.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoFacturaTest {

    @Test
    void onlyBorradorIsEditable() {
        assertTrue(EstadoFactura.BORRADOR.esEditable());
        assertFalse(EstadoFactura.LISTA_PARA_EMITIR.esEditable());
        assertFalse(EstadoFactura.ENVIANDO.esEditable());
        assertFalse(EstadoFactura.EMITIDA.esEditable());
        assertFalse(EstadoFactura.RECHAZADA.esEditable());
        assertFalse(EstadoFactura.CONTINGENCIA.esEditable());
        assertFalse(EstadoFactura.ANULADA.esEditable());
        assertFalse(EstadoFactura.PAGADA.esEditable());
    }

    @Test
    void onlyEmitidaRequiresAcceptedHaciendaResponse() {
        assertTrue(EstadoFactura.EMITIDA.requiereRespuestaAceptadaDeHacienda());
        assertFalse(EstadoFactura.LISTA_PARA_EMITIR.requiereRespuestaAceptadaDeHacienda());
        assertFalse(EstadoFactura.ENVIANDO.requiereRespuestaAceptadaDeHacienda());
    }

    @Test
    void fromValueAcceptsStoredStringValues() {
        assertSame(EstadoFactura.BORRADOR, EstadoFactura.fromValue("BORRADOR"));
        assertSame(EstadoFactura.EMITIDA, EstadoFactura.fromValue("emitida"));
    }
}
