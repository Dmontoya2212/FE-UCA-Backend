package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Item.ItemRequest;
import com.feuca.facturacion.dto.request.Item.ItemUpdateRequest;
import com.feuca.facturacion.dto.response.Item.ItemResponse;
import com.feuca.facturacion.entity.Item;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ItemMapper {

    public static Item to_entity(ItemRequest request, UUID empresa_id) {
        return Item.builder()
                .id(UUID.randomUUID())
                .empresa_id(empresa_id)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .categoria(request.getCategoria())
                .iva_id(request.getIvaId())
                // Nota: este campo lo llena el trigger sync_item_iva_snapshot() en BD
                .iva_porcentaje_snapshot(null)
                .precio_sin_iva(request.getPrecioSinIva())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .created_at(OffsetDateTime.now())
                .updated_at(OffsetDateTime.now())
                .deleted_at(null)
                .build();
    }

    public static void update_entity(Item entity, ItemUpdateRequest request) {
        if (request.getNombre() != null) entity.setNombre(request.getNombre());
        if (request.getDescripcion() != null) entity.setDescripcion(request.getDescripcion());
        if (request.getCategoria() != null) entity.setCategoria(request.getCategoria());
        if (request.getIvaId() != null) entity.setIva_id(request.getIvaId());
        if (request.getPrecioSinIva() != null) entity.setPrecio_sin_iva(request.getPrecioSinIva());
        if (request.getActivo() != null) entity.setActivo(request.getActivo());

        entity.setUpdated_at(OffsetDateTime.now());
    }

    public static ItemResponse to_response(Item entity) {
        return ItemResponse.builder()
                .id(entity.getId())
                .empresaId(entity.getEmpresa_id())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .categoria(entity.getCategoria())
                .ivaId(entity.getIva_id())
                .ivaPorcentajeSnapshot(entity.getIva_porcentaje_snapshot())
                .precioSinIva(entity.getPrecio_sin_iva())
                .activo(entity.getActivo())
                .createdAt(entity.getCreated_at())
                .updatedAt(entity.getUpdated_at())
                .deletedAt(entity.getDeleted_at())
                .build();
    }
}