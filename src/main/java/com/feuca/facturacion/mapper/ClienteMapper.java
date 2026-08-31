package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Cliente.ClienteRequest;
import com.feuca.facturacion.dto.request.Cliente.ClienteUpdateRequest;
import com.feuca.facturacion.dto.response.Cliente.ClienteResponse;
import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.util.DataNormalizer;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ClienteMapper {

    public static Cliente to_entity(ClienteRequest request, UUID empresaId) {
        return Cliente.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .nombreRazonSocial(DataNormalizer.displayText(request.getNombreRazonSocial()))
                .nifCif(DataNormalizer.identifier(request.getNifCif()))
                .email(DataNormalizer.email(request.getEmail()))
                .direccion(DataNormalizer.displayText(request.getDireccion()))
                .ciudad(DataNormalizer.displayText(request.getCiudad()))
                .codigoPostal(DataNormalizer.identifier(request.getCodigoPostal()))
                .telefono(DataNormalizer.phone(request.getTelefono()))
                .tipoDocumento(DataNormalizer.identifier(request.getTipoDocumento()))
                .nrc(DataNormalizer.identifier(request.getNrc()))
                .codActividad(DataNormalizer.identifier(request.getCodActividad()))
                .descActividad(DataNormalizer.displayText(request.getDescActividad()))
                .departamento(DataNormalizer.identifier(request.getDepartamento()))
                .municipio(DataNormalizer.identifier(request.getMunicipio()))
                .distrito(DataNormalizer.identifier(request.getDistrito()))
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .deletedAt(null)
                .build();
    }

    public static void update_entity(Cliente entity, ClienteUpdateRequest request) {
        if (request.getNombreRazonSocial() != null) entity.setNombreRazonSocial(DataNormalizer.displayText(request.getNombreRazonSocial()));
        if (request.getNifCif() != null) entity.setNifCif(DataNormalizer.identifier(request.getNifCif()));
        if (request.getEmail() != null) entity.setEmail(DataNormalizer.email(request.getEmail()));
        if (request.getDireccion() != null) entity.setDireccion(DataNormalizer.displayText(request.getDireccion()));
        if (request.getCiudad() != null) entity.setCiudad(DataNormalizer.displayText(request.getCiudad()));
        if (request.getCodigoPostal() != null) entity.setCodigoPostal(DataNormalizer.identifier(request.getCodigoPostal()));
        if (request.getTelefono() != null) entity.setTelefono(DataNormalizer.phone(request.getTelefono()));
        if (request.getTipoDocumento() != null) entity.setTipoDocumento(DataNormalizer.identifier(request.getTipoDocumento()));
        if (request.getNrc() != null) entity.setNrc(DataNormalizer.identifier(request.getNrc()));
        if (request.getCodActividad() != null) entity.setCodActividad(DataNormalizer.identifier(request.getCodActividad()));
        if (request.getDescActividad() != null) entity.setDescActividad(DataNormalizer.displayText(request.getDescActividad()));
        if (request.getDepartamento() != null) entity.setDepartamento(DataNormalizer.identifier(request.getDepartamento()));
        if (request.getMunicipio() != null) entity.setMunicipio(DataNormalizer.identifier(request.getMunicipio()));
        if (request.getDistrito() != null) entity.setDistrito(DataNormalizer.identifier(request.getDistrito()));
        if (request.getActivo() != null) entity.setActivo(request.getActivo());

        entity.setUpdatedAt(OffsetDateTime.now());
    }

    public static ClienteResponse to_response(Cliente entity) {
        return ClienteResponse.builder()
                .id(entity.getId())
                .empresaId(entity.getEmpresaId())
                .nombreRazonSocial(entity.getNombreRazonSocial())
                .nifCif(entity.getNifCif())
                .email(entity.getEmail())
                .direccion(entity.getDireccion())
                .ciudad(entity.getCiudad())
                .codigoPostal(entity.getCodigoPostal())
                .telefono(entity.getTelefono())
                .tipoDocumento(entity.getTipoDocumento())
                .nrc(entity.getNrc())
                .codActividad(entity.getCodActividad())
                .descActividad(entity.getDescActividad())
                .departamento(entity.getDepartamento())
                .municipio(entity.getMunicipio())
                .distrito(entity.getDistrito())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
