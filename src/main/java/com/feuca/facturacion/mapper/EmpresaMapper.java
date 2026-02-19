package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import com.feuca.facturacion.entity.Empresa;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmpresaMapper {

    private EmpresaMapper() {}

    public static Empresa toEntityCreate(EmpresaRequest nuevaEmpresa
    ) {
        return Empresa.builder()
                .nombre_legal(nuevaEmpresa.getNombreLegal())
                .nombre_comercial(nuevaEmpresa.getNombreComercial())
                .nif_cif(nuevaEmpresa.getNifCif())
                .email(nuevaEmpresa.getEmail())
                .telefono(nuevaEmpresa.getTelefono())
                .direccion(nuevaEmpresa.getDireccion())
                .ciudad(nuevaEmpresa.getCiudad())
                .codigo_postal(nuevaEmpresa.getCodigoPostal())
                .build();
    }

    public static void toEntityUpdate(
            Empresa empresa,
            EmpresaUpdateRequest empresaUpdate
    ){
        if(empresaUpdate.getNombreLegal() != null) empresa.setNombre_legal(empresaUpdate.getNombreLegal());
        if(empresaUpdate.getNombreLegal() != null) empresa.setNombre_comercial(empresaUpdate.getNombreComercial());
        if(empresaUpdate.getNifCif() != null) empresa.setNif_cif(empresaUpdate.getNifCif());
        if(empresaUpdate.getEmail() != null) empresa.setEmail(empresaUpdate.getEmail());
        if(empresaUpdate.getTelefono() != null) empresa.setTelefono(empresaUpdate.getTelefono());
        if(empresaUpdate.getDireccion() != null) empresa.setDireccion(empresaUpdate.getDireccion());
        if(empresaUpdate.getCiudad() != null) empresa.setCiudad(empresaUpdate.getCiudad());
        if(empresaUpdate.getCodigoPostal() != null) empresa.setCodigo_postal(empresaUpdate.getCodigoPostal());
    }

    public static EmpresaResponse toDTO(
            Empresa empresa,
            List<MonedaResponse> monedas
    ) {
            return EmpresaResponse.builder()
                    .id(empresa.getId())
                    .nombreLegal(empresa.getNombre_legal())
                    .nombreComercial(empresa.getNombre_comercial())
                    .nifCif(empresa.getNif_cif())
                    .email(empresa.getEmail())
                    .telefono(empresa.getTelefono())
                    .direccion(empresa.getDireccion())
                    .ciudad(empresa.getCiudad())
                    .codigoPostal(empresa.getCodigo_postal())
                    .pais(empresa.getPais())
                    .createdAt(empresa.getCreated_at())
                    .monedas(monedas)
                    .build();
    }

    public static List<EmpresaResponse> toDTOList(
            List<Empresa> empresas,
            Map<UUID, List<MonedaResponse>> monedasPorEmpresa
    ) {
        return empresas.stream().map(empresa -> toDTO(
                empresa,
                monedasPorEmpresa.getOrDefault(empresa.getId(),List.of())
        )).toList();
    }
}
