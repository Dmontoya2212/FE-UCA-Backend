package com.feuca.facturacion.exception.Factura;

import java.util.List;

public class FacturaValidationException extends RuntimeException {
    private final List<String> errors;

    public FacturaValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public FacturaValidationException(List<String> errors) {
        super("La factura no cumple las validaciones previas a emision.");
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
