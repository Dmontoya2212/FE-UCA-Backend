package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaRequest;
import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaUpdateRequest;
import com.feuca.facturacion.dto.response.IvaTasa.IvaTasaResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IvaTasaService {

    // CREATE
    IvaTasaResponse create(IvaTasaRequest ivaTasa);

    // READ
    IvaTasaResponse getById(UUID id);
    IvaTasaResponse getByEmpresaIdAndNombre(UUID empresaId, String nombre);
    IvaTasaResponse getByEmpresaIdAndPorcentaje(UUID empresaId, BigDecimal porcentaje);
    List<IvaTasaResponse> getAllByEmpresaId(UUID empresaId);

    // UPDATE
    IvaTasaResponse update(UUID id, IvaTasaUpdateRequest ivaTasa);

    // DELETE
    IvaTasaResponse deleteById(UUID id);
    IvaTasaResponse deleteByEmpresaIdAndNombre(UUID empresaId, String nombre);
    IvaTasaResponse deleteByEmpresaIdAndPorcentaje(UUID empresaId, BigDecimal porcentaje);
}