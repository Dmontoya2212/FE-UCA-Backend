package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaRequest;
import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaUpdateRequest;
import com.feuca.facturacion.dto.response.IvaTasa.IvaTasaResponse;
import com.feuca.facturacion.entity.IvaTasa;
import com.feuca.facturacion.exception.IvaTasa.IvaTasaAlreadyExistsException;
import com.feuca.facturacion.exception.IvaTasa.IvaTasaNotFoundException;
import com.feuca.facturacion.mapper.IvaTasaMapper;
import com.feuca.facturacion.repository.IvaTasaRepository;
import com.feuca.facturacion.service.IvaTasaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class IvaTasaServiceImpl implements IvaTasaService {

    private final IvaTasaRepository ivaTasaRepository;

    @Autowired
    public IvaTasaServiceImpl(IvaTasaRepository ivaTasaRepository) {
        this.ivaTasaRepository = ivaTasaRepository;
    }

    // CREATE
    @Override
    @Transactional
    public IvaTasaResponse create(IvaTasaRequest request) {

        UUID empresaId = request.getEmpresaId();
        BigDecimal porcentaje = request.getPorcentaje();

        if (ivaTasaRepository.existsByEmpresaIdAndNombre(empresaId, request.getNombre())) {
            throw new IvaTasaAlreadyExistsException(
                    "Ya existe una tasa de IVA con ese nombre para esta empresa."
            );
        }

        if (ivaTasaRepository.existsByEmpresaIdAndPorcentaje(empresaId, porcentaje)) {
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

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public IvaTasaResponse getByEmpresaIdAndNombre(UUID empresaId, String nombre) {

        IvaTasa entity = ivaTasaRepository
                .findByEmpresaIdAndNombre(empresaId, nombre)
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado para esa empresa con nombre: " + nombre));

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public IvaTasaResponse getByEmpresaIdAndPorcentaje(UUID empresaId, BigDecimal porcentaje) {

        IvaTasa entity = ivaTasaRepository
                .findByEmpresaIdAndPorcentaje(empresaId, porcentaje)
                .orElseThrow(() ->
                        new IvaTasaNotFoundException("IVA no encontrado para esa empresa con porcentaje: " + porcentaje)
                );

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IvaTasaResponse> getAllByEmpresaId(UUID empresaId) {

        return ivaTasaRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(IvaTasaMapper::to_response)
                .toList();
    }

    // UPDATE
    @Override
    @Transactional
    public IvaTasaResponse update(UUID id, IvaTasaUpdateRequest request) {

        IvaTasa entity = ivaTasaRepository.findById(id)
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado con id: " + id));

        UUID empresaId = entity.getEmpresaId();

        if (request.getNombre() != null) {
            ivaTasaRepository.findByEmpresaIdAndNombre(empresaId, request.getNombre())
                    .ifPresent(found -> {
                        if (!found.getId().equals(entity.getId())) {
                            throw new IvaTasaAlreadyExistsException(
                                    "Ya existe una tasa de IVA con ese nombre para esta empresa."
                            );
                        }
                    });
        }

        if (request.getPorcentaje() != null) {
            ivaTasaRepository.findByEmpresaIdAndPorcentaje(empresaId, request.getPorcentaje())
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
    public IvaTasaResponse deleteById(UUID id) {

        IvaTasa entity = ivaTasaRepository.findById(id)
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado con id: " + id));

        ivaTasaRepository.delete(entity);

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional
    public IvaTasaResponse deleteByEmpresaIdAndNombre(UUID empresaId, String nombre) {

        IvaTasa entity = ivaTasaRepository
                .findByEmpresaIdAndNombre(empresaId, nombre)
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado con ese nombre."));

        ivaTasaRepository.delete(entity);

        return IvaTasaMapper.to_response(entity);
    }

    @Override
    @Transactional
    public IvaTasaResponse deleteByEmpresaIdAndPorcentaje(UUID empresaId, BigDecimal porcentaje) {

        IvaTasa entity = ivaTasaRepository
                .findByEmpresaIdAndPorcentaje(empresaId, porcentaje)
                .orElseThrow(() -> new IvaTasaNotFoundException("IVA no encontrado con ese porcentaje."));

        ivaTasaRepository.delete(entity);

        return IvaTasaMapper.to_response(entity);
    }
}