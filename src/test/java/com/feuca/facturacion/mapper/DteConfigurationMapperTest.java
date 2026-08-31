package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Cliente.ClienteRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.response.Cliente.ClienteResponse;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.service.impl.AesGcmSecretEncryptionService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DteConfigurationMapperTest {

    @Test
    void mapsEmpresaDteConfigurationFromRequestToResponse() {
        Empresa empresa = EmpresaMapper.toEntityCreate(EmpresaRequest.builder()
                .nombreLegal("Emisor")
                .nit("0614-120392-101-4")
                .codActividad("620100")
                .departamento("06")
                .municipio("14")
                .distrito("0002")
                .codEstablecimiento("M001")
                .codPuntoVenta("P001")
                .build(), new AesGcmSecretEncryptionService("01234567890123456789012345678901"));

        EmpresaResponse response = EmpresaMapper.toDTO(empresa, List.of());

        assertEquals("620100", response.getCodActividad());
        assertEquals("06", response.getDepartamento());
        assertEquals("14", response.getMunicipio());
        assertEquals("0002", response.getDistrito());
        assertEquals("M001", response.getCodEstablecimiento());
        assertEquals("P001", response.getCodPuntoVenta());
    }

    @Test
    void mapsClienteDteConfigurationFromRequestToResponse() {
        Cliente cliente = ClienteMapper.to_entity(ClienteRequest.builder()
                .nombreRazonSocial("Receptor")
                .nifCif("06141203921014")
                .tipoDocumento("36")
                .nrc("12345678")
                .codActividad("620100")
                .descActividad("Servicios")
                .departamento("06")
                .municipio("14")
                .distrito("0002")
                .build(), UUID.randomUUID());

        ClienteResponse response = ClienteMapper.to_response(cliente);

        assertEquals("36", response.getTipoDocumento());
        assertEquals("12345678", response.getNrc());
        assertEquals("620100", response.getCodActividad());
        assertEquals("Servicios", response.getDescActividad());
        assertEquals("06", response.getDepartamento());
        assertEquals("14", response.getMunicipio());
        assertEquals("0002", response.getDistrito());
    }
}
