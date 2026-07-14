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
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.ItemService;
import com.feuca.facturacion.util.DataNormalizer;
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
    private final AccessControlService accessControlService;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, IvaTasaRepository ivaTasaRepository, AccessControlService accessControlService) {
        this.itemRepository = itemRepository;
        this.ivaTasaRepository = ivaTasaRepository;
        this.accessControlService = accessControlService;
    }

    // CREATE
    @Override
    @Transactional
    public ItemResponse create(ItemRequest request) {

        UUID empresaId = request.getEmpresaId();
        accessControlService.requireEmpresaAccess(empresaId);

        String nombre = DataNormalizer.displayText(request.getNombre());
        if (itemRepository.existsByEmpresaIdAndNombreIgnoreCase(empresaId, nombre)) {
            throw new ItemAlreadyExistsException("Ya existe un item con ese nombre para esta empresa.");
        }

        String codigoInterno = DataNormalizer.identifier(request.getCodigoInterno());
        if (codigoInterno != null && itemRepository.existsByEmpresaIdAndCodigoInternoIgnoreCase(empresaId, codigoInterno)) {
            throw new ItemAlreadyExistsException("Ya existe un item con ese codigo interno para esta empresa.");
        }

        IvaTasa iva = validateIvaBelongsToEmpresa(request.getIvaId(), empresaId);

        Item entity = ItemMapper.to_entity(request, empresaId);
        entity.setIvaPorcentajeSnapshot(iva.getPorcentaje());
        Item saved = itemRepository.save(entity);

        return toResponseWithIva(saved);
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
        accessControlService.requireEmpresaAccess(entity.getEmpresaId());

        return toResponseWithIva(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getByEmpresaIdAndNombre(UUID empresaId, String nombre) {
        accessControlService.requireEmpresaAccess(empresaId);
        Item entity = itemRepository.findByEmpresaIdAndNombreIgnoreCase(empresaId, DataNormalizer.displayText(nombre))
                .orElseThrow(() -> new ItemNotFoundException("Item no encontrado para esa empresa con nombre: " + nombre));

        if (entity.getDeletedAt() != null) {
            throw new ItemNotFoundException("Item no encontrado para esa empresa con nombre: " + nombre);
        }

        return toResponseWithIva(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllByEmpresaId(UUID empresaId) {
        accessControlService.requireEmpresaAccess(empresaId);
        return itemRepository.findAllByEmpresaId(empresaId).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(this::toResponseWithIva)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllActivosByEmpresaId(UUID empresaId) {
        accessControlService.requireEmpresaAccess(empresaId);
        // repo devuelve activos true, pero igual filtramos deleted_at
        return itemRepository.findAllByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(this::toResponseWithIva)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllByEmpresaIdAndCategoria(UUID empresaId, ItemCategoria categoria) {
        accessControlService.requireEmpresaAccess(empresaId);
        return itemRepository.findAllByEmpresaIdAndCategoria(empresaId, categoria).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(this::toResponseWithIva)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllByEmpresaIdAndIvaId(UUID empresaId, UUID ivaId) {
        accessControlService.requireEmpresaAccess(empresaId);
        return itemRepository.findAllByEmpresaIdAndIvaId(empresaId, ivaId).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(this::toResponseWithIva)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> searchByNombre(UUID empresaId, String nombre) {
        accessControlService.requireEmpresaAccess(empresaId);
        return itemRepository.findAllByEmpresaIdAndNombreContainingIgnoreCase(empresaId, nombre).stream()
                .filter(i -> i.getDeletedAt() == null)
                .map(this::toResponseWithIva)
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
        accessControlService.requireEmpresaAccess(empresaId);


        String nombre = DataNormalizer.displayText(request.getNombre());
        if (nombre != null) {
            itemRepository.findByEmpresaIdAndNombreIgnoreCase(empresaId, nombre)
                    .ifPresent(found -> {
                        if (!found.getId().equals(entity.getId())) {
                            throw new ItemAlreadyExistsException("Ya existe un item con ese nombre para esta empresa.");
                        }
                    });
        }

        String codigoInterno = DataNormalizer.identifier(request.getCodigoInterno());
        if (codigoInterno != null) {
            itemRepository.findByEmpresaIdAndCodigoInternoIgnoreCase(empresaId, codigoInterno)
                    .ifPresent(found -> {
                        if (!found.getId().equals(entity.getId())) {
                            throw new ItemAlreadyExistsException("Ya existe un item con ese codigo interno para esta empresa.");
                        }
                    });
        }

        IvaTasa iva = validateIvaBelongsToEmpresa(request.getIvaId(), empresaId);

        ItemMapper.update_entity(entity, request);
        if (request.getIvaId() != null) {
            entity.setIvaPorcentajeSnapshot(iva.getPorcentaje());
        }

        Item updated = itemRepository.save(entity);

        return toResponseWithIva(updated);
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
        accessControlService.requireEmpresaAccess(entity.getEmpresaId());

        entity.setDeletedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        Item saved = itemRepository.save(entity);

        return toResponseWithIva(saved);
    }

    // HELPERS
    private IvaTasa validateIvaBelongsToEmpresa(UUID ivaId, UUID empresaId) {
        IvaTasa iva = ivaTasaRepository.findById(ivaId)
                .orElseThrow(() -> new ItemIvaNotFoundException("El IVA indicado no existe."));

        if (!iva.getEmpresaId().equals(empresaId)) {
            throw new ItemIvaNotFoundException("El IVA indicado no pertenece a esta empresa.");
        }
        if (Boolean.FALSE.equals(iva.getActivo()) || iva.getDeletedAt() != null) {
            throw new ItemIvaNotFoundException("El IVA indicado no esta activo.");
        }
        return iva;
    }

    private ItemResponse toResponseWithIva(Item entity) {
        IvaTasa ivaTasa = ivaTasaRepository.findById(entity.getIvaId()).orElse(null);
        return ItemMapper.to_response(entity, ivaTasa);
    }
}
