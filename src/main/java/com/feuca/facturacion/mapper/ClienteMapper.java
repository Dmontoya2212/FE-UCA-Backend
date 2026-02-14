package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Cliente.ClienteRequest;
import com.feuca.facturacion.dto.request.Cliente.ClienteUpdateRequest;
import com.feuca.facturacion.dto.response.Cliente.ClienteResponse;
import com.feuca.facturacion.entity.Cliente;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ClienteMapper {

    public static Cliente to_entity(ClienteRequest request, UUID empresa_id) {
        return Cliente.builder()
                .id(UUID.randomUUID())
                .empresa_id(empresa_id)
                .nombre_razon_social(request.getNombreRazonSocial())
                .nif_cif(request.getNifCif())
                .email(request.getEmail())
                .direccion(request.getDireccion())
                .ciudad(request.getCiudad())
                .codigo_postal(request.getCodigoPostal())
                .telefono(request.getTelefono())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .created_at(OffsetDateTime.now())
                .updated_at(OffsetDateTime.now())
                .deleted_at(null)
                .build();
    }

    public static void update_entity(Cliente entity, ClienteUpdateRequest request) {
        if (request.getNombreRazonSocial() != null) entity.setNombre_razon_social(request.getNombreRazonSocial());
        if (request.getNifCif() != null) entity.setNif_cif(request.getNifCif());
        if (request.getEmail() != null) entity.setEmail(request.getEmail());
        if (request.getDireccion() != null) entity.setDireccion(request.getDireccion());
        if (request.getCiudad() != null) entity.setCiudad(request.getCiudad());
        if (request.getCodigoPostal() != null) entity.setCodigo_postal(request.getCodigoPostal());
        if (request.getTelefono() != null) entity.setTelefono(request.getTelefono());
        if (request.getActivo() != null) entity.setActivo(request.getActivo());

        entity.setUpdated_at(OffsetDateTime.now());
    }

    public static ClienteResponse to_response(Cliente entity) {
        return ClienteResponse.builder()
                .id(entity.getId())
                .empresaId(entity.getEmpresa_id())
                .nombreRazonSocial(entity.getNombre_razon_social())
                .nifCif(entity.getNif_cif())
                .email(entity.getEmail())
                .direccion(entity.getDireccion())
                .ciudad(entity.getCiudad())
                .codigoPostal(entity.getCodigo_postal())
                .telefono(entity.getTelefono())
                .activo(entity.getActivo())
                .createdAt(entity.getCreated_at())
                .updatedAt(entity.getUpdated_at())
                .deletedAt(entity.getDeleted_at())
                .build();
    }
}
