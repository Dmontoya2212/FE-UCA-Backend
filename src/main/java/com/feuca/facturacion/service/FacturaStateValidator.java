package com.feuca.facturacion.service;

import com.feuca.facturacion.enums.EstadoFactura;
import com.feuca.facturacion.exception.Factura.FacturaNoEditableException;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class FacturaStateValidator {

    private static final Map<EstadoFactura, Set<EstadoFactura>> TRANSICIONES_PERMITIDAS = new EnumMap<>(EstadoFactura.class);

    static {
        TRANSICIONES_PERMITIDAS.put(EstadoFactura.BORRADOR, EnumSet.of(
                EstadoFactura.LISTA_PARA_EMITIR,
                EstadoFactura.ENVIANDO,
                EstadoFactura.CONTINGENCIA
        ));
        TRANSICIONES_PERMITIDAS.put(EstadoFactura.LISTA_PARA_EMITIR, EnumSet.of(
                EstadoFactura.ENVIANDO,
                EstadoFactura.CONTINGENCIA
        ));
        TRANSICIONES_PERMITIDAS.put(EstadoFactura.ENVIANDO, EnumSet.of(
                EstadoFactura.EMITIDA,
                EstadoFactura.RECHAZADA,
                EstadoFactura.CONTINGENCIA
        ));
        TRANSICIONES_PERMITIDAS.put(EstadoFactura.RECHAZADA, EnumSet.of(
                EstadoFactura.ENVIANDO
        ));
        TRANSICIONES_PERMITIDAS.put(EstadoFactura.CONTINGENCIA, EnumSet.of(
                EstadoFactura.ENVIANDO,
                EstadoFactura.EMITIDA,
                EstadoFactura.RECHAZADA
        ));
        TRANSICIONES_PERMITIDAS.put(EstadoFactura.EMITIDA, EnumSet.of(
                EstadoFactura.PAGADA,
                EstadoFactura.ANULADA
        ));
        TRANSICIONES_PERMITIDAS.put(EstadoFactura.PAGADA, EnumSet.noneOf(EstadoFactura.class));
        TRANSICIONES_PERMITIDAS.put(EstadoFactura.ANULADA, EnumSet.noneOf(EstadoFactura.class));
    }

    public void validarTransicion(String estadoActual, EstadoFactura estadoDestino) {
        validarTransicion(EstadoFactura.fromValue(estadoActual), estadoDestino);
    }

    public void validarTransicion(EstadoFactura estadoActual, EstadoFactura estadoDestino) {
        if (estadoActual == estadoDestino) {
            return;
        }
        if (!TRANSICIONES_PERMITIDAS.getOrDefault(estadoActual, Set.of()).contains(estadoDestino)) {
            throw new FacturaNoEditableException("Transicion de estado no permitida: "
                    + estadoActual.name() + " -> " + estadoDestino.name() + ".");
        }
    }

    public boolean esTransicionPermitida(EstadoFactura estadoActual, EstadoFactura estadoDestino) {
        return estadoActual == estadoDestino
                || TRANSICIONES_PERMITIDAS.getOrDefault(estadoActual, Set.of()).contains(estadoDestino);
    }
}
