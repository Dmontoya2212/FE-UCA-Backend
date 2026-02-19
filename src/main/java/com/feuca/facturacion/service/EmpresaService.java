package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;

import java.util.List;
import java.util.UUID;

public interface EmpresaService {
    //CREATE
    EmpresaResponse create(EmpresaRequest empresa);

    //READ
    EmpresaResponse getById(UUID id);
    EmpresaResponse getByNombreLegal(String nombreLegal);
    EmpresaResponse getByNifCif(String nifCif);
    EmpresaResponse getByEmail(String email);
    EmpresaResponse getByTelefono(String telefono);
    EmpresaResponse getByNombreComercialAndDireccion(String nombreComercial, String direccion);
    List<EmpresaResponse> getAllByNombreComercial(String nombreComercial);
    List<EmpresaResponse> getAllByCiudad(String ciudad);
    List<EmpresaResponse> getAllByCodigoPostal(String codigoPostal);
    List<EmpresaResponse> getAllByPais(String pais);
    List<EmpresaResponse> getAll();


    //UPDATE
    EmpresaResponse update(UUID idEmpresa, EmpresaUpdateRequest empresa);

    //DELETE
    EmpresaResponse deleteById(UUID id);
    EmpresaResponse deleteByNombreLegal(String nombreLegal);
    EmpresaResponse deleteByNifCif(String nifCif);
    EmpresaResponse deleteByEmail(String email);
    EmpresaResponse deleteByTelefono(String telefono);
}
