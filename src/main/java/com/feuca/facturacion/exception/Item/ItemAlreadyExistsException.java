package com.feuca.facturacion.exception.Item;

public class ItemAlreadyExistsException extends RuntimeException {
    public ItemAlreadyExistsException(String message) {
        super(message);
    }
}