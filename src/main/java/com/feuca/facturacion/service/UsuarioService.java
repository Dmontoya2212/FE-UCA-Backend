package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.request.Usuario.UsuarioRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.dto.response.Usuario.UsuarioResponse;

import java.util.List;
import java.util.UUID;

public interface UsuarioService {
    UsuarioResponse create(UsuarioRequest request);
    UsuarioResponse getById(UUID empresaId, UUID usuarioId);
    UsuarioResponse getByEmail(UUID empresaId, String email);
    List<UsuarioResponse> getAllByEmpresa(UUID empresaId);
    UsuarioResponse update(UUID empresaId, UUID usuarioId, UsuarioUpdateRequest request);
    void delete(UUID empresaId, UUID usuarioId);
}
