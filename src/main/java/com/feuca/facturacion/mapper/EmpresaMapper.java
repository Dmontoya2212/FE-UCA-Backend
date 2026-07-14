package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaIntegrationUpdateRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.service.SecretEncryptionService;
import com.feuca.facturacion.util.DataNormalizer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmpresaMapper {
    private EmpresaMapper() {}

    public static Empresa toEntityCreate(EmpresaRequest req, SecretEncryptionService secretEncryptionService) {
        return Empresa.builder()
                .id(UUID.randomUUID())
                .razonSocial(DataNormalizer.displayText(req.getRazonSocial()))
                .nombreLegal(DataNormalizer.displayText(req.getNombreLegal()))
                .nombreComercial(DataNormalizer.displayText(req.getNombreComercial()))
                .nit(DataNormalizer.identifier(req.getNit()))
                .registro(DataNormalizer.identifier(req.getRegistro()))
                .actividadEconomica(DataNormalizer.displayText(req.getActividadEconomica()))
                .sectorEmpresa(DataNormalizer.displayText(req.getSectorEmpresa()))
                .email(DataNormalizer.email(req.getEmail()))
                .telefono(DataNormalizer.phone(req.getTelefono()))
                .direccion(DataNormalizer.displayText(req.getDireccion()))
                .ciudad(DataNormalizer.displayText(req.getCiudad()))
                .codigoPostal(DataNormalizer.identifier(req.getCodigoPostal()))
                .pais(DataNormalizer.displayText(req.getPais()))
                .usuario(DataNormalizer.displayText(req.getUsuario()))
                .passwordHash(secretEncryptionService.encrypt(req.getPassword()))
                .clavePrimaria(secretEncryptionService.encrypt(req.getClavePrimaria()))
                .token(secretEncryptionService.encrypt(req.getToken()))
                .expireToken(req.getExpireToken())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static void applyBusinessUpdate(Empresa e, EmpresaUpdateRequest req) {
        if (req.getRazonSocial() != null) e.setRazonSocial(DataNormalizer.displayText(req.getRazonSocial()));
        if (req.getNombreLegal() != null) e.setNombreLegal(DataNormalizer.displayText(req.getNombreLegal()));
        if (req.getNombreComercial() != null) e.setNombreComercial(DataNormalizer.displayText(req.getNombreComercial()));
        if (req.getNit() != null) e.setNit(DataNormalizer.identifier(req.getNit()));
        if (req.getRegistro() != null) e.setRegistro(DataNormalizer.identifier(req.getRegistro()));
        if (req.getActividadEconomica() != null) e.setActividadEconomica(DataNormalizer.displayText(req.getActividadEconomica()));
        if (req.getSectorEmpresa() != null) e.setSectorEmpresa(DataNormalizer.displayText(req.getSectorEmpresa()));
        if (req.getEmail() != null) e.setEmail(DataNormalizer.email(req.getEmail()));
        if (req.getTelefono() != null) e.setTelefono(DataNormalizer.phone(req.getTelefono()));
        if (req.getDireccion() != null) e.setDireccion(DataNormalizer.displayText(req.getDireccion()));
        if (req.getCiudad() != null) e.setCiudad(DataNormalizer.displayText(req.getCiudad()));
        if (req.getCodigoPostal() != null) e.setCodigoPostal(DataNormalizer.identifier(req.getCodigoPostal()));
        if (req.getPais() != null) e.setPais(DataNormalizer.displayText(req.getPais()));
        if (req.getUsuario() != null) e.setUsuario(DataNormalizer.displayText(req.getUsuario()));
        e.setUpdatedAt(OffsetDateTime.now());
    }

    public static void applyIntegrationCredentialsUpdate(Empresa e, EmpresaIntegrationUpdateRequest req, SecretEncryptionService secretEncryptionService) {
        if (req.getPassword() != null) e.setPasswordHash(secretEncryptionService.encrypt(req.getPassword()));
        if (req.getClavePrimaria() != null) e.setClavePrimaria(secretEncryptionService.encrypt(req.getClavePrimaria()));
        if (req.getToken() != null) e.setToken(secretEncryptionService.encrypt(req.getToken()));
        if (req.getExpireToken() != null) e.setExpireToken(req.getExpireToken());
        e.setUpdatedAt(OffsetDateTime.now());
    }

    public static EmpresaResponse toDTO(Empresa e, List<MonedaResponse> monedas) {
        return EmpresaResponse.builder()
                .id(e.getId())
                .razonSocial(e.getRazonSocial())
                .nombreLegal(e.getNombreLegal())
                .nombreComercial(e.getNombreComercial())
                .nit(e.getNit())
                .registro(e.getRegistro())
                .actividadEconomica(e.getActividadEconomica())
                .sectorEmpresa(e.getSectorEmpresa())
                .email(e.getEmail())
                .telefono(e.getTelefono())
                .direccion(e.getDireccion())
                .ciudad(e.getCiudad())
                .codigoPostal(e.getCodigoPostal())
                .pais(e.getPais())
                .usuario(e.getUsuario())
                .expireToken(e.getExpireToken())
                .monedas(monedas)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static List<EmpresaResponse> toDTOList(List<Empresa> empresas, Map<UUID, List<MonedaResponse>> monedasXEmpresa) {
        return empresas.stream()
                .map(empresa -> toDTO(
                        empresa,
                        monedasXEmpresa.getOrDefault(empresa.getId(), List.of())
                ))
                .toList();
    }
}
