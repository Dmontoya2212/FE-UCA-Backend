package com.feuca.facturacion.exception.Factura;

public class FacturaAlreadyExistsException extends RuntimeException {
    public FacturaAlreadyExistsException(String message) {
        super(message);
    }
}
