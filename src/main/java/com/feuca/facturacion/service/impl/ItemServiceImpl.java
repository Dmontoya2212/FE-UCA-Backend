package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Item.ItemRequest;
import com.feuca.facturacion.dto.request.Item.ItemUpdateRequest;
import com.feuca.facturacion.dto.response.Item.ItemResponse;
import com.feuca.facturacion.entity.IvaTasa;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.exception.Item.ItemAlreadyExistsException;
import com.feuca.facturacion.exception.Item.ItemIvaNotFoundException;
import com.feuca.facturacion.exception.Item.ItemNotFoundException;
import com.feuca.facturacion.mapper.ItemMapper;
import com.feuca.facturacion.repository.IvaTasaRepository;
import com.feuca.facturacion.repository.ItemRepository;
import com.feuca.facturacion.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final IvaTasaRepository ivaTasaRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, IvaTasaRepository ivaTasaRepository) {
        this.itemRepository = itemRepository;
        this.ivaTasaRepository = ivaTasaRepository;
    }

    // CREATE
    @Override
    @Transactional
    public ItemResponse create(ItemRequest request) {

        UUID empresaId = request.getEmpresaId();

        if (itemRepository.existsByEmpresaIdAndNombre(empresaId, request.getNombre())) {
            throw new ItemAlreadyExistsException("Ya existe un item con ese nombre para esta empresa.");
        }

        validateIvaBelongsToEmpresa(request.getIvaId(), empresaId);

        Item entity = ItemMapper.to_entity(request, empresaId);
        Item saved = itemRepository.save(entity);

        return ItemMapper.to_response(saved);
    }

    // READ
    @Override
    @Transactional(readOnly = true)
    public ItemResponse getById(UUID id) {
        Item entity = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item no encontrado con id: " + id));

        if (entity.getDeletedAt() != null) {
            throw new ItemNotFoundException("Item no encontrado con id: " + id);
        }

        return ItemMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getByEmpresaIdAndNombre(UUID empresaId, String nombre) {
        Item entity = itemRepository.findByEmpresaIdAndNombre(empresaId, nombre)
                .orElseThrow(() -> new ItemNotFoundException("Item no encontrado para esa empresa con nombre: " + nombre));

        if (entity.getDeletedAt() != null) {
            throw new ItemNotFoundException("Item no encontrado para esa empresa con nombre: " + nombre);
        }

        return ItemMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllByEmpresaId(UUID empresaId) {
        return itemRepository.findAllByEmpresaId(empresaId).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(ItemMapper::to_response)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllActivosByEmpresaId(UUID empresaId) {
        // repo devuelve activos true, pero igual filtramos deleted_at
        return itemRepository.findAllByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(ItemMapper::to_response)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllByEmpresaIdAndCategoria(UUID empresaId, ItemCategoria categoria) {
        return itemRepository.findAllByEmpresaIdAndCategoria(empresaId, categoria).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(ItemMapper::to_response)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllByEmpresaIdAndIvaId(UUID empresaId, UUID ivaId) {
        return itemRepository.findAllByEmpresaIdAndIvaId(empresaId, ivaId).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(ItemMapper::to_response)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> searchByNombre(UUID empresaId, String nombre) {
        return itemRepository.findAllByEmpresaIdAndNombreContainingIgnoreCase(empresaId, nombre).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(ItemMapper::to_response)
                .toList();
    }

    // UPDATE
    @Override
    @Transactional
    public ItemResponse update(UUID id, ItemUpdateRequest request) {

        Item entity = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item no encontrado con id: " + id));

        if (entity.getDeletedAt() != null) {
            throw new ItemNotFoundException("Item no encontrado con id: " + id);
        }

        UUID empresaId = entity.getEmpresaId();


        itemRepository.findByEmpresaIdAndNombre(empresaId, request.getNombre())
                .ifPresent(found -> {
                    if (!found.getId().equals(entity.getId())) {
                        throw new ItemAlreadyExistsException("Ya existe un item con ese nombre para esta empresa.");
                    }
                });

        validateIvaBelongsToEmpresa(request.getIvaId(), empresaId);

        ItemMapper.update_entity(entity, request);

        Item updated = itemRepository.save(entity);

        return ItemMapper.to_response(updated);
    }

    // DELETE
    @Override
    @Transactional
    public ItemResponse deleteById(UUID id) {

        Item entity = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item no encontrado con id: " + id));

        if (entity.getDeletedAt() != null) {
            throw new ItemNotFoundException("Item no encontrado con id: " + id);
        }

        entity.setDeletedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        Item saved = itemRepository.save(entity);

        return ItemMapper.to_response(saved);
    }

    // HELPERS
    private void validateIvaBelongsToEmpresa(UUID ivaId, UUID empresaId) {
        IvaTasa iva = ivaTasaRepository.findById(ivaId)
                .orElseThrow(() -> new ItemIvaNotFoundException("El IVA indicado no existe."));

        if (!iva.getEmpresaId().equals(empresaId)) {
            throw new ItemIvaNotFoundException("El IVA indicado no pertenece a esta empresa.");
        }
    }
}