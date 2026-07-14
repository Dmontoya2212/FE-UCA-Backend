package com.feuca.facturacion.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AesGcmSecretEncryptionServiceTest {

    private final AesGcmSecretEncryptionService service =
            new AesGcmSecretEncryptionService("01234567890123456789012345678901");

    @Test
    void encryptsAndDecryptsRecoverableSecrets() {
        String encrypted = service.encrypt("clave-hacienda");

        assertTrue(service.isEncrypted(encrypted));
        assertNotEquals("clave-hacienda", encrypted);
        assertEquals("clave-hacienda", service.decrypt(encrypted));
    }

    @Test
    void encryptionRequiresConfiguredKey() {
        AesGcmSecretEncryptionService withoutKey = new AesGcmSecretEncryptionService("");

        assertThrows(IllegalStateException.class, () -> withoutKey.encrypt("secreto"));
    }

    @Test
    void decryptReturnsLegacyPlainTextUnchanged() {
        assertEquals("valor-legado", service.decrypt("valor-legado"));
    }
}
