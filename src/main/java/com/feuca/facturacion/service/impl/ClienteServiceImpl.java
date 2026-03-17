package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Cliente.ClienteRequest;
import com.feuca.facturacion.dto.request.Cliente.ClienteUpdateRequest;
import com.feuca.facturacion.dto.response.Cliente.ClienteResponse;
import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.exception.Cliente.ClienteAlreadyExistsException;
import com.feuca.facturacion.exception.Cliente.ClienteNotFoundException;
import com.feuca.facturacion.mapper.ClienteMapper;
import com.feuca.facturacion.repository.ClienteRepository;
import com.feuca.facturacion.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // CREATE
    @Override
    @Transactional
    public ClienteResponse create(ClienteRequest request) {

        UUID empresaId = request.getEmpresaId();

        if (request.getNifCif() != null && clienteRepository.existsByEmpresaIdAndNifCif(empresaId, request.getNifCif())) {
            throw new ClienteAlreadyExistsException("Ya existe un cliente con ese NIF/CIF para esta empresa.");
        }

        if (request.getEmail() != null && clienteRepository.existsByEmpresaIdAndEmail(empresaId, request.getEmail())) {
            throw new ClienteAlreadyExistsException("Ya existe un cliente con ese email para esta empresa.");
        }

        Cliente entity = ClienteMapper.to_entity(request, empresaId);
        Cliente saved = clienteRepository.save(entity);

        return ClienteMapper.to_response(saved);
    }

    // READ
    @Override
    @Transactional(readOnly = true)
    public ClienteResponse getById(UUID id) {

        Cliente entity = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con id: " + id));

        if (entity.getDeletedAt() != null) {
            throw new ClienteNotFoundException("Cliente no encontrado con id: " + id);
        }

        return ClienteMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse getByEmpresaIdAndNifCif(UUID empresaId, String nifCif) {

        Cliente entity = clienteRepository.findByEmpresaIdAndNifCif(empresaId, nifCif)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ese NIF/CIF."));

        if (entity.getDeletedAt() != null) {
            throw new ClienteNotFoundException("Cliente no encontrado con ese NIF/CIF.");
        }

        return ClienteMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse getByEmpresaIdAndEmail(UUID empresaId, String email) {

        Cliente entity = clienteRepository.findByEmpresaIdAndEmail(empresaId, email)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ese email."));

        if (entity.getDeletedAt() != null) {
            throw new ClienteNotFoundException("Cliente no encontrado con ese email.");
        }

        return ClienteMapper.to_response(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> getAllByEmpresaId(UUID empresaId) {
        return clienteRepository.findAllByEmpresaId(empresaId).stream()
                .filter(c -> c.getDeletedAt() == null)
                .map(ClienteMapper::to_response)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> getAllActivosByEmpresaId(UUID empresaId) {
        return clienteRepository.findAllByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(c -> c.getDeletedAt() == null)
                .map(ClienteMapper::to_response)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> searchByNombre(UUID empresaId, String nombre) {
        return clienteRepository.findAllByEmpresaIdAndNombreRazonSocialContainingIgnoreCase(empresaId, nombre).stream()
                .filter(c -> c.getDeletedAt() == null)
                .map(ClienteMapper::to_response)
                .toList();
    }

    //  UPDATE
    @Override
    @Transactional
    public ClienteResponse update(UUID id, ClienteUpdateRequest request) {

        Cliente entity = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con id: " + id));

        if (entity.getDeletedAt() != null) {
            throw new ClienteNotFoundException("Cliente no encontrado con id: " + id);
        }

        UUID empresaId = entity.getEmpresaId();

        if (request.getNifCif() != null) {
            clienteRepository.findByEmpresaIdAndNifCif(empresaId, request.getNifCif())
                    .ifPresent(found -> {
                        if (!found.getId().equals(entity.getId())) {
                            throw new ClienteAlreadyExistsException("Ya existe un cliente con ese NIF/CIF para esta empresa.");
                        }
                    });
        }

        if (request.getEmail() != null) {
            clienteRepository.findByEmpresaIdAndEmail(empresaId, request.getEmail())
                    .ifPresent(found -> {
                        if (!found.getId().equals(entity.getId())) {
                            throw new ClienteAlreadyExistsException("Ya existe un cliente con ese email para esta empresa.");
                        }
                    });
        }

        ClienteMapper.update_entity(entity, request);

        Cliente updated = clienteRepository.save(entity);
        return ClienteMapper.to_response(updated);
    }

    //  DELETE
    @Override
    @Transactional
    public ClienteResponse deleteById(UUID id) {

        Cliente entity = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con id: " + id));

        if (entity.getDeletedAt() != null) {
            throw new ClienteNotFoundException("Cliente no encontrado con id: " + id);
        }

        entity.setDeletedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        Cliente saved = clienteRepository.save(entity);
        return ClienteMapper.to_response(saved);
    }
}