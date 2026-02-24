package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.request.Cliente.ClienteRequest;
import com.feuca.facturacion.dto.request.Cliente.ClienteUpdateRequest;
import com.feuca.facturacion.dto.response.Cliente.ClienteResponse;

import java.util.List;
import java.util.UUID;

public interface ClienteService {

    // CREATE
    ClienteResponse create(ClienteRequest cliente);

    // READ
    ClienteResponse getById(UUID id);
    ClienteResponse getByEmpresaIdAndNifCif(UUID empresaId, String nifCif);
    ClienteResponse getByEmpresaIdAndEmail(UUID empresaId, String email);

    List<ClienteResponse> getAllByEmpresaId(UUID empresaId);
    List<ClienteResponse> getAllActivosByEmpresaId(UUID empresaId);
    List<ClienteResponse> searchByNombre(UUID empresaId, String nombre);

    // UPDATE
    ClienteResponse update(UUID id, ClienteUpdateRequest cliente);

    // DELETE (soft delete)
    ClienteResponse deleteById(UUID id);
}