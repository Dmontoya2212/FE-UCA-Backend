package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.request.Item.ItemRequest;
import com.feuca.facturacion.dto.request.Item.ItemUpdateRequest;
import com.feuca.facturacion.dto.response.Item.ItemResponse;
import com.feuca.facturacion.entity.ItemCategoria;

import java.util.List;
import java.util.UUID;

public interface ItemService {

    // CREATE
    ItemResponse create(ItemRequest item);

    // READ
    ItemResponse getById(UUID id);
    ItemResponse getByEmpresaIdAndNombre(UUID empresaId, String nombre);

    List<ItemResponse> getAllByEmpresaId(UUID empresaId);
    List<ItemResponse> getAllActivosByEmpresaId(UUID empresaId);
    List<ItemResponse> getAllByEmpresaIdAndCategoria(UUID empresaId, ItemCategoria categoria);
    List<ItemResponse> getAllByEmpresaIdAndIvaId(UUID empresaId, UUID ivaId);
    List<ItemResponse> searchByNombre(UUID empresaId, String nombre);

    // UPDATE
    ItemResponse update(UUID id, ItemUpdateRequest item);

    // DELETE (soft delete)
    ItemResponse deleteById(UUID id);
}