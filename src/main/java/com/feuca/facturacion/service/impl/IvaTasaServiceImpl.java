package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaRequest;
import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaUpdateRequest;
import com.feuca.facturacion.dto.response.IvaTasa.IvaTasaResponse;
import com.feuca.facturacion.entity.IvaTasa;
import com.feuca.facturacion.exception.IvaTasa.IvaTasaAlreadyExistsException;
import com.feuca.facturacion.exception.IvaTasa.IvaTasaNotFoundException;
import com.feuca.facturacion.mapper.IvaTasaMapper;
import com.feuca.facturacion.repository.IvaTasaRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.IvaTasaService;
import com.feuca.facturacion.util.DataNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class IvaTasaServiceImpl implements IvaTasaService {

    private final IvaTasaRepository ivaTasaRepository;
    private final AccessControlService accessControlService;

    @Autowired
    public IvaTasaServiceImpl(IvaTasaRepository ivaTasaRepository, AccessControlService accessControlService) {
        this.ivaTasaRepository = ivaTasaRepository;
        this.accessControlService = accessControlService;
    }

    // CREATE
    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('SUPERADMIN','ADMINISTRADOR')")
    public IvaTasaResponse create(IvaTasaRequest request) {
        accessControlService.requireAdministradorOrSuperAdmin();

        UUID empresaId = request.getEmpresaId();
        accessControlService.requireEmpresaAccess(empresaId);
        BigDecimal porcentaje = request.getPorcentaje();

        String nombre = DataNormalizer.displayText(request.getNombre());
        if (ivaTasaRepository.existsByEmpresaIdAndNombreIgnoreCaseAndDeletedAtIsNull(empresaId, nombre)) {
            throw new IvaTasaAlreadyExistsException(
                    "Ya existe una tasa de IVA con ese nombre para esta empresa."
            );
        }

        if (ivaTasaRepository.existsByEmpresaIdAndPorcentajeAndDeletedAtIsNull(empresaId, porcentaje)) {
            throw new IvaTasaAlreadyExistsException(
                    "Ya existe una tasa de IVA con ese porcentaje para esta empresa."
            );
        }

        IvaTasa entity = IvaTasaMapper.to_entity(request, empresaId);
        IvaTasa saved = ivaTasaRepository.save(entity);

        return IvaTasaMapper.to_response(saved);
    }

    // READ
    @Override
    @Transactional(readOnly = true)
    public IvaTasaResponse getById(UUID id) {

        IvaTasa entity = ivaTasaRepository.findById(id)
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado con id: " + id));
        accessControlService.requireEmpresaAccess(entity.getEmpresaId());
        ensureNotDeleted(entity);

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public IvaTasaResponse getByEmpresaIdAndNombre(UUID empresaId, String nombre) {
        accessControlService.requireEmpresaAccess(empresaId);

        IvaTasa entity = ivaTasaRepository
                .findByEmpresaIdAndNombreIgnoreCaseAndActivoTrueAndDeletedAtIsNull(empresaId, DataNormalizer.displayText(nombre))
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado para esa empresa con nombre: " + nombre));

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public IvaTasaResponse getByEmpresaIdAndPorcentaje(UUID empresaId, BigDecimal porcentaje) {
        accessControlService.requireEmpresaAccess(empresaId);

        IvaTasa entity = ivaTasaRepository
                .findByEmpresaIdAndPorcentajeAndActivoTrueAndDeletedAtIsNull(empresaId, porcentaje)
                .orElseThrow(() ->
                        new IvaTasaNotFoundException("IVA no encontrado para esa empresa con porcentaje: " + porcentaje)
                );

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IvaTasaResponse> getAllByEmpresaId(UUID empresaId) {
        accessControlService.requireEmpresaAccess(empresaId);

        return ivaTasaRepository.findAllByEmpresaIdAndActivoTrueAndDeletedAtIsNull(empresaId)
                .stream()
                .map(IvaTasaMapper::to_response)
                .toList();
    }

    // UPDATE
    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('SUPERADMIN','ADMINISTRADOR')")
    public IvaTasaResponse update(UUID id, IvaTasaUpdateRequest request) {
        accessControlService.requireAdministradorOrSuperAdmin();

        IvaTasa entity = ivaTasaRepository.findById(id)
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado con id: " + id));
        ensureNotDeleted(entity);

        UUID empresaId = entity.getEmpresaId();
        accessControlService.requireEmpresaAccess(empresaId);

        String nombre = DataNormalizer.displayText(request.getNombre());
        if (nombre != null) {
            ivaTasaRepository.findByEmpresaIdAndNombreIgnoreCaseAndActivoTrueAndDeletedAtIsNull(empresaId, nombre)
                    .ifPresent(found -> {
                        if (!found.getId().equals(entity.getId())) {
                            throw new IvaTasaAlreadyExistsException(
                                    "Ya existe una tasa de IVA con ese nombre para esta empresa."
                            );
                        }
                    });
        }

        if (request.getPorcentaje() != null) {
            ivaTasaRepository.findByEmpresaIdAndPorcentajeAndActivoTrueAndDeletedAtIsNull(empresaId, request.getPorcentaje())
                    .ifPresent(found -> {
                        if (!found.getId().equals(entity.getId())) {
                            throw new IvaTasaAlreadyExistsException(
                                    "Ya existe una tasa de IVA con ese porcentaje para esta empresa."
                            );
                        }
                    });
        }

        IvaTasaMapper.update_entity(entity, request);

        IvaTasa updated = ivaTasaRepository.save(entity);

        return IvaTasaMapper.to_response(updated);
    }

    // DELETE
    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('SUPERADMIN','ADMINISTRADOR')")
    public IvaTasaResponse deleteById(UUID id) {
        accessControlService.requireAdministradorOrSuperAdmin();

        IvaTasa entity = ivaTasaRepository.findById(id)
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado con id: " + id));
        ensureNotDeleted(entity);
        accessControlService.requireEmpresaAccess(entity.getEmpresaId());

        softDelete(entity);
        ivaTasaRepository.save(entity);

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('SUPERADMIN','ADMINISTRADOR')")
    public IvaTasaResponse deleteByEmpresaIdAndNombre(UUID empresaId, String nombre) {
        accessControlService.requireAdministradorOrSuperAdmin();
        accessControlService.requireEmpresaAccess(empresaId);

        IvaTasa entity = ivaTasaRepository
                .findByEmpresaIdAndNombreIgnoreCaseAndActivoTrueAndDeletedAtIsNull(empresaId, DataNormalizer.displayText(nombre))
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado con ese nombre."));

        softDelete(entity);
        ivaTasaRepository.save(entity);

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('SUPERADMIN','ADMINISTRADOR')")
    public IvaTasaResponse deleteByEmpresaIdAndPorcentaje(UUID empresaId, BigDecimal porcentaje) {
        accessControlService.requireAdministradorOrSuperAdmin();
        accessControlService.requireEmpresaAccess(empresaId);

        IvaTasa entity = ivaTasaRepository
                .findByEmpresaIdAndPorcentajeAndActivoTrueAndDeletedAtIsNull(empresaId, porcentaje)
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado con ese porcentaje."));

        softDelete(entity);
        ivaTasaRepository.save(entity);

        return IvaTasaMapper.to_response(entity);
    }

    private void ensureNotDeleted(IvaTasa entity) {
        if (entity.getDeletedAt() != null) {
            throw new IvaTasaNotFoundException("IVA no encontrado.");
        }
    }

    private void softDelete(IvaTasa entity) {
        entity.setActivo(false);
        entity.setDeletedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
    }
}
