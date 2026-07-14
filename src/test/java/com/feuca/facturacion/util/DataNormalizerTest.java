package com.feuca.facturacion.util;

import com.feuca.facturacion.dto.request.Cliente.ClienteRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.mapper.ClienteMapper;
import com.feuca.facturacion.mapper.EmpresaMapper;
import com.feuca.facturacion.service.SecretEncryptionService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DataNormalizerTest {

    @Test
    void normalizesEmailAndIdentifiersWithoutRemovingSeparators() {
        assertEquals("user@example.com", DataNormalizer.email("  USER@Example.COM  "));
        assertEquals("0614-120392-101-4", DataNormalizer.identifier(" 0614-120392-101-4 "));
        assertEquals("+503 2264-8500", DataNormalizer.phone(" +503 2264-8500 "));
    }

    @Test
    void clienteMapperNormalizesUniqueFieldsBeforePersisting() {
        Cliente cliente = ClienteMapper.to_entity(ClienteRequest.builder()
                .empresaId(UUID.randomUUID())
                .nombreRazonSocial("  Industrias La Constancia S.A.  ")
                .nifCif(" abc-123 ")
                .email(" COMPRAS@Example.COM ")
                .telefono(" +503 2222-3000 ")
                .build(), UUID.randomUUID());

        assertEquals("Industrias La Constancia S.A.", cliente.getNombreRazonSocial());
        assertEquals("ABC-123", cliente.getNifCif());
        assertEquals("compras@example.com", cliente.getEmail());
        assertEquals("+503 2222-3000", cliente.getTelefono());
    }

    @Test
    void empresaMapperDoesNotLowercaseLegalNames() {
        Empresa empresa = EmpresaMapper.toEntityCreate(EmpresaRequest.builder()
                .nombreLegal("  Corporacion de Alimentos S.A. de C.V.  ")
                .nit("0614-120392-101-4")
                .email(" CONTACTO@SELECTOS.COM.SV ")
                .build(), mock(SecretEncryptionService.class));

        assertEquals("Corporacion de Alimentos S.A. de C.V.", empresa.getNombreLegal());
        assertEquals("contacto@selectos.com.sv", empresa.getEmail());
    }
}
