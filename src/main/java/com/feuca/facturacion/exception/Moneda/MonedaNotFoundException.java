package com.feuca.facturacion.exception.Moneda;

public class MonedaNotFoundException extends RuntimeException {
    public MonedaNotFoundException(String message) {
        super(message);
    }
}
