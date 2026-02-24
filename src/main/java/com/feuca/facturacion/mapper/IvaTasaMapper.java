package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaRequest;
import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaUpdateRequest;
import com.feuca.facturacion.dto.response.IvaTasa.IvaTasaResponse;
import com.feuca.facturacion.entity.IvaTasa;

import java.time.OffsetDateTime;
import java.util.UUID;

public class IvaTasaMapper {

    public static IvaTasa to_entity(IvaTasaRequest request, UUID empresa_id) {
        return IvaTasa.builder()
                .id(UUID.randomUUID())
                .empresa_id(empresa_id)
                .nombre(request.getNombre())
                .porcentaje(request.getPorcentaje())
                .created_at(OffsetDateTime.now())
                .updated_at(OffsetDateTime.now())
                .build();
    }

    public static void update_entity(IvaTasa entity, IvaTasaUpdateRequest request) {
        if (request.getNombre() != null) entity.setNombre(request.getNombre());
        if (request.getPorcentaje() != null) entity.setPorcentaje(request.getPorcentaje());

        entity.setUpdated_at(OffsetDateTime.now());
    }

    public static IvaTasaResponse to_response(IvaTasa entity) {
        return IvaTasaResponse.builder()
                .id(entity.getId())
                .empresaId(entity.getEmpresa_id())
                .nombre(entity.getNombre())
                .porcentaje(entity.getPorcentaje())
                .createdAt(entity.getCreated_at())
                .updatedAt(entity.getUpdated_at())
                .build();
    }
}