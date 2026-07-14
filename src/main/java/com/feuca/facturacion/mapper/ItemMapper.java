package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Item.ItemRequest;
import com.feuca.facturacion.dto.request.Item.ItemUpdateRequest;
import com.feuca.facturacion.dto.response.Item.ItemIvaResponse;
import com.feuca.facturacion.dto.response.Item.ItemResponse;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.IvaTasa;
import com.feuca.facturacion.util.DataNormalizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;    
import java.util.UUID;

public class ItemMapper {

    public static Item to_entity(ItemRequest request, UUID empresaId) {
        return Item.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .nombre(DataNormalizer.displayText(request.getNombre()))
                .descripcion(DataNormalizer.displayText(request.getDescripcion()))
                .categoria(request.getCategoria())
                .ivaId(request.getIvaId())
                .ivaPorcentajeSnapshot(null)
                .precioSinIva(request.getPrecioSinIva())
                .codigoInterno(DataNormalizer.identifier(request.getCodigoInterno()))
                .unidadMedida(request.getUnidadMedida() != null ? request.getUnidadMedida() : 59)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .deletedAt(null)
                .build();
    }

    public static void update_entity(Item entity, ItemUpdateRequest request) {
        if (request.getNombre() != null) entity.setNombre(DataNormalizer.displayText(request.getNombre()));
        if (request.getDescripcion() != null) entity.setDescripcion(DataNormalizer.displayText(request.getDescripcion()));
        if (request.getCategoria() != null) entity.setCategoria(request.getCategoria());
        if (request.getIvaId() != null) entity.setIvaId(request.getIvaId());
        if (request.getPrecioSinIva() != null) entity.setPrecioSinIva(request.getPrecioSinIva());
        if (request.getCodigoInterno() != null) entity.setCodigoInterno(DataNormalizer.identifier(request.getCodigoInterno()));
        if (request.getUnidadMedida() != null) entity.setUnidadMedida(request.getUnidadMedida());
        if (request.getActivo() != null) entity.setActivo(request.getActivo());

        entity.setUpdatedAt(OffsetDateTime.now());
    }

    public static ItemResponse to_response(Item entity, IvaTasa ivaTasa) {
        ItemIvaResponse ivaResponse = null;
        if (ivaTasa != null) {
            ivaResponse = ItemIvaResponse.builder()
                    .nombre(ivaTasa.getNombre())
                    .porcentaje(ivaTasa.getPorcentaje().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .build();
        }

        return ItemResponse.builder()
                .id(entity.getId())
                .empresaId(entity.getEmpresaId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .categoria(entity.getCategoria())
                .iva(ivaResponse)
                .precioSinIva(entity.getPrecioSinIva())
                .codigoInterno(entity.getCodigoInterno())
                .unidadMedida(entity.getUnidadMedida())
                .ivaPorcentajeSnapshot(entity.getIvaPorcentajeSnapshot())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
