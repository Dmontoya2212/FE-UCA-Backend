package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.repository.EmpresaRepository;
import com.feuca.facturacion.repository.EmpresaMonedaRepository;
import com.feuca.facturacion.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMonedaRepository empresaMonedaRepository;

    @Autowired
    public EmpresaServiceImpl (
        EmpresaRepository empresaRepository,
        EmpresaMonedaRepository empresaMonedaRepository
    ){
        this.empresaRepository = empresaRepository;
        this.empresaMonedaRepository = empresaMonedaRepository;
    }

    //CREATE
    @Override
    @Transactional
    public EmpresaResponse create(EmpresaRequest empresa) {
        return null;
    }

    //READ
    @Override
    public EmpresaResponse getById(UUID id) {
        return null;
    }

    @Override
    public EmpresaResponse getByNombreLegal(String nombreLegal) {
        return null;
    }

    @Override
    public EmpresaResponse getByNifCif(String nifCif) {
        return null;
    }

    @Override
    public EmpresaResponse getByEmail(String email) {
        return null;
    }

    @Override
    public EmpresaResponse getByTelefono(String telefono) {
        return null;
    }

    @Override
    public EmpresaResponse getByNombreComercialAndDireccion(String nombreComercial, String direccion) {
        return null;
    }

    @Override
    public List<EmpresaResponse> getAllByNombreComercial(String nombreComercial) {
        return List.of();
    }

    @Override
    public List<EmpresaResponse> getAllByCiudad(String ciudad) {
        return List.of();
    }

    @Override
    public List<EmpresaResponse> getAllByCodigoPostal(String codigoPostal) {
        return List.of();
    }

    @Override
    public List<EmpresaResponse> getAllByPais(String pais) {
        return List.of();
    }

    @Override
    public List<EmpresaResponse> getAll() {
        return List.of();
    }

    //UPDATE

    @Override
    public EmpresaResponse update(UUID idEmpresa, EmpresaUpdateRequest empresa) {
        return null;
    }

    //DELETE

    @Override
    public EmpresaResponse deleteById(UUID id) {
        return null;
    }

    @Override
    public EmpresaResponse deleteByNombreLegal(String nombreLegal) {
        return null;
    }

    @Override
    public EmpresaResponse deleteByNifCif(String nifCif) {
        return null;
    }

    @Override
    public EmpresaResponse deleteByEmail(String email) {
        return null;
    }

    @Override
    public EmpresaResponse deleteByTelefono(String telefono) {
        return null;
    }
}
