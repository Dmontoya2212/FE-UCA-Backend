package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaIntegrationUpdateRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.service.SecretEncryptionService;
import com.feuca.facturacion.service.impl.AesGcmSecretEncryptionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmpresaMapperSecretTest {

    private final SecretEncryptionService secretEncryptionService =
            new AesGcmSecretEncryptionService("01234567890123456789012345678901");

    @Test
    void createEncryptsIntegrationSecretsReversibly() {
        Empresa empresa = EmpresaMapper.toEntityCreate(EmpresaRequest.builder()
                .nombreLegal("Empresa DTE")
                .nit("0614-120392-101-4")
                .password("password-hacienda")
                .clavePrimaria("clave-firma")
                .token("token-hacienda")
                .build(), secretEncryptionService);

        assertEncryptedRecoverable("password-hacienda", empresa.getPasswordHash());
        assertEncryptedRecoverable("clave-firma", empresa.getClavePrimaria());
        assertEncryptedRecoverable("token-hacienda", empresa.getToken());
    }

    @Test
    void updateEncryptsIntegrationSecretsReversibly() {
        Empresa empresa = Empresa.builder()
                .nombreLegal("Empresa DTE")
                .build();

        EmpresaMapper.applyIntegrationCredentialsUpdate(empresa, EmpresaIntegrationUpdateRequest.builder()
                .password("nuevo-password")
                .clavePrimaria("nueva-clave")
                .token("nuevo-token")
                .build(), secretEncryptionService);

        assertEncryptedRecoverable("nuevo-password", empresa.getPasswordHash());
        assertEncryptedRecoverable("nueva-clave", empresa.getClavePrimaria());
        assertEncryptedRecoverable("nuevo-token", empresa.getToken());
    }

    @Test
    void businessUpdateDoesNotModifyIntegrationSecrets() {
        Empresa empresa = Empresa.builder()
                .nombreLegal("Empresa anterior")
                .passwordHash("enc:v1:password")
                .clavePrimaria("enc:v1:clave")
                .token("enc:v1:token")
                .build();

        EmpresaMapper.applyBusinessUpdate(empresa, EmpresaUpdateRequest.builder()
                .nombreLegal("Empresa nueva")
                .build());

        assertEquals("Empresa nueva", empresa.getNombreLegal());
        assertEquals("enc:v1:password", empresa.getPasswordHash());
        assertEquals("enc:v1:clave", empresa.getClavePrimaria());
        assertEquals("enc:v1:token", empresa.getToken());
    }

    @Test
    void empresaResponseDoesNotExposeIntegrationToken() {
        EmpresaResponse response = EmpresaMapper.toDTO(Empresa.builder()
                .token("enc:v1:token")
                .build(), List.of());

        assertThrows(NoSuchMethodException.class, () -> response.getClass().getMethod("getToken"));
    }

    private void assertEncryptedRecoverable(String plainText, String storedValue) {
        assertTrue(secretEncryptionService.isEncrypted(storedValue));
        assertNotEquals(plainText, storedValue);
        assertEquals(plainText, secretEncryptionService.decrypt(storedValue));
    }
}
