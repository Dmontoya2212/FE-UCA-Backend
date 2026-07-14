package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.service.SecretEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesGcmSecretEncryptionService implements SecretEncryptionService {

    private static final String PREFIX = "enc:v1:";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final String encryptionKey;
    private final SecureRandom secureRandom;

    @Autowired
    public AesGcmSecretEncryptionService(@Value("${security.secrets.encryption-key:}") String encryptionKey) {
        this(encryptionKey, new SecureRandom());
    }

    AesGcmSecretEncryptionService(String encryptionKey, SecureRandom secureRandom) {
        this.encryptionKey = encryptionKey;
        this.secureRandom = secureRandom;
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank() || isEncrypted(plainText)) {
            return plainText;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return PREFIX
                    + Base64.getEncoder().encodeToString(iv)
                    + ":"
                    + Base64.getEncoder().encodeToString(cipherText);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo cifrar la credencial de integracion", ex);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank() || !isEncrypted(encryptedText)) {
            return encryptedText;
        }

        String[] parts = encryptedText.substring(PREFIX.length()).split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de credencial cifrada invalido");
        }

        try {
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherText = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo descifrar la credencial de integracion", ex);
        }
    }

    @Override
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    private SecretKeySpec keySpec() {
        String normalizedKey = encryptionKey == null ? "" : encryptionKey.trim();
        if (normalizedKey.isBlank()) {
            throw new IllegalStateException("SECRET_ENCRYPTION_KEY debe configurarse para cifrar credenciales de integracion");
        }

        byte[] rawKey = decodeBase64Key(normalizedKey);
        if (rawKey == null) {
            rawKey = normalizedKey.getBytes(StandardCharsets.UTF_8);
        }

        if (rawKey.length == 16 || rawKey.length == 24 || rawKey.length == 32) {
            return new SecretKeySpec(rawKey, "AES");
        }

        if (rawKey.length >= 32) {
            try {
                return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(rawKey), "AES");
            } catch (GeneralSecurityException ex) {
                throw new IllegalStateException("No se pudo preparar la clave de cifrado", ex);
            }
        }

        throw new IllegalStateException("SECRET_ENCRYPTION_KEY debe tener al menos 32 caracteres o ser Base64 de 16, 24 o 32 bytes");
    }

    private byte[] decodeBase64Key(String normalizedKey) {
        try {
            return Base64.getDecoder().decode(normalizedKey);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
