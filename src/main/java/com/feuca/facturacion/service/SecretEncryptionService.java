package com.feuca.facturacion.service;

public interface SecretEncryptionService {
    String encrypt(String plainText);

    String decrypt(String encryptedText);

    boolean isEncrypted(String value);
}
