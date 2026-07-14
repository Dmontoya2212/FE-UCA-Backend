package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaRequest;
import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaUpdateRequest;
import com.feuca.facturacion.dto.response.IvaTasa.IvaTasaResponse;
import com.feuca.facturacion.entity.IvaTasa;
import com.feuca.facturacion.util.DataNormalizer;

import java.time.OffsetDateTime;
import java.util.UUID;

public class IvaTasaMapper {

    public static IvaTasa to_entity(IvaTasaRequest request, UUID empresaId) {
        return IvaTasa.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .nombre(DataNormalizer.displayText(request.getNombre()))
                .porcentaje(request.getPorcentaje())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .deletedAt(null)
                .build();
    }

    public static void update_entity(IvaTasa entity, IvaTasaUpdateRequest request) {
        if (request.getNombre() != null) entity.setNombre(DataNormalizer.displayText(request.getNombre()));
        if (request.getPorcentaje() != null) entity.setPorcentaje(request.getPorcentaje());
        if (request.getActivo() != null) entity.setActivo(request.getActivo());

        entity.setUpdatedAt(OffsetDateTime.now());
    }

    public static IvaTasaResponse to_response(IvaTasa entity) {
        return IvaTasaResponse.builder()
                .id(entity.getId())
                .empresaId(entity.getEmpresaId())
                .nombre(entity.getNombre())
                .porcentaje(entity.getPorcentaje())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
