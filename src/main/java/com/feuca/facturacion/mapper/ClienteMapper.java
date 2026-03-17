package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Cliente.ClienteRequest;
import com.feuca.facturacion.dto.request.Cliente.ClienteUpdateRequest;
import com.feuca.facturacion.dto.response.Cliente.ClienteResponse;
import com.feuca.facturacion.entity.Cliente;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ClienteMapper {

    public static Cliente to_entity(ClienteRequest request, UUID empresaId) {
        return Cliente.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .nombreRazonSocial(request.getNombreRazonSocial())
                .nifCif(request.getNifCif())
                .email(request.getEmail())
                .direccion(request.getDireccion())
                .ciudad(request.getCiudad())
                .codigoPostal(request.getCodigoPostal())
                .telefono(request.getTelefono())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .deletedAt(null)
                .build();
    }

    public static void update_entity(Cliente entity, ClienteUpdateRequest request) {
        if (request.getNombreRazonSocial() != null) entity.setNombreRazonSocial(request.getNombreRazonSocial());
        if (request.getNifCif() != null) entity.setNifCif(request.getNifCif());
        if (request.getEmail() != null) entity.setEmail(request.getEmail());
        if (request.getDireccion() != null) entity.setDireccion(request.getDireccion());
        if (request.getCiudad() != null) entity.setCiudad(request.getCiudad());
        if (request.getCodigoPostal() != null) entity.setCodigoPostal(request.getCodigoPostal());
        if (request.getTelefono() != null) entity.setTelefono(request.getTelefono());
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
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}