package com.feuca.facturacion.service;

import com.feuca.facturacion.enums.EstadoFactura;
import com.feuca.facturacion.exception.Factura.FacturaNoEditableException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacturaStateValidatorTest {

    private final FacturaStateValidator validator = new FacturaStateValidator();

    @Test
    void allowsDefinedEmissionTransitions() {
        assertAllowed(EstadoFactura.BORRADOR, EstadoFactura.LISTA_PARA_EMITIR);
        assertAllowed(EstadoFactura.BORRADOR, EstadoFactura.ENVIANDO);
        assertAllowed(EstadoFactura.LISTA_PARA_EMITIR, EstadoFactura.ENVIANDO);
        assertAllowed(EstadoFactura.ENVIANDO, EstadoFactura.EMITIDA);
        assertAllowed(EstadoFactura.ENVIANDO, EstadoFactura.RECHAZADA);
        assertAllowed(EstadoFactura.RECHAZADA, EstadoFactura.ENVIANDO);
        assertAllowed(EstadoFactura.EMITIDA, EstadoFactura.PAGADA);
        assertAllowed(EstadoFactura.EMITIDA, EstadoFactura.ANULADA);
        assertAllowed(EstadoFactura.ENVIANDO, EstadoFactura.CONTINGENCIA);
        assertAllowed(EstadoFactura.CONTINGENCIA, EstadoFactura.ENVIANDO);
    }

    @Test
    void rejectsInvalidBusinessTransitions() {
        assertRejected(EstadoFactura.BORRADOR, EstadoFactura.PAGADA);
        assertRejected(EstadoFactura.RECHAZADA, EstadoFactura.PAGADA);
        assertRejected(EstadoFactura.EMITIDA, EstadoFactura.BORRADOR);
        assertRejected(EstadoFactura.ANULADA, EstadoFactura.EMITIDA);
        assertRejected(EstadoFactura.PAGADA, EstadoFactura.BORRADOR);
        assertRejected(EstadoFactura.PAGADA, EstadoFactura.EMITIDA);
    }

    @Test
    void exposesBooleanTransitionPolicy() {
        assertTrue(validator.esTransicionPermitida(EstadoFactura.ENVIANDO, EstadoFactura.EMITIDA));
        assertFalse(validator.esTransicionPermitida(EstadoFactura.BORRADOR, EstadoFactura.PAGADA));
    }

    private void assertAllowed(EstadoFactura actual, EstadoFactura destino) {
        assertDoesNotThrow(() -> validator.validarTransicion(actual, destino));
    }

    private void assertRejected(EstadoFactura actual, EstadoFactura destino) {
        assertThrows(FacturaNoEditableException.class, () -> validator.validarTransicion(actual, destino));
    }
}
