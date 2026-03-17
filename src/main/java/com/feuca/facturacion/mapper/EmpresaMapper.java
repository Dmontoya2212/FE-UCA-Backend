package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import com.feuca.facturacion.entity.Empresa;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmpresaMapper {

    private EmpresaMapper() {}

    public static Empresa toEntityCreate(EmpresaRequest nuevaEmpresa) {
        return Empresa.builder()
                .id(UUID.randomUUID())
                .nombreLegal(nuevaEmpresa.getNombreLegal())
                .nombreComercial(nuevaEmpresa.getNombreComercial())
                .nifCif(nuevaEmpresa.getNifCif())
                .email(nuevaEmpresa.getEmail())
                .telefono(nuevaEmpresa.getTelefono())
                .direccion(nuevaEmpresa.getDireccion())
                .ciudad(nuevaEmpresa.getCiudad())
                .codigoPostal(nuevaEmpresa.getCodigoPostal())
                .pais(null)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .deletedAt(null)
                .build();
    }

    public static void toEntityUpdate(
            Empresa empresa,
            EmpresaUpdateRequest empresaUpdate
    ) {
        if (empresaUpdate.getNombreLegal() != null) empresa.setNombreLegal(empresaUpdate.getNombreLegal());
        if (empresaUpdate.getNombreComercial() != null) empresa.setNombreComercial(empresaUpdate.getNombreComercial());
        if (empresaUpdate.getNifCif() != null) empresa.setNifCif(empresaUpdate.getNifCif());
        if (empresaUpdate.getEmail() != null) empresa.setEmail(empresaUpdate.getEmail());
        if (empresaUpdate.getTelefono() != null) empresa.setTelefono(empresaUpdate.getTelefono());
        if (empresaUpdate.getDireccion() != null) empresa.setDireccion(empresaUpdate.getDireccion());
        if (empresaUpdate.getCiudad() != null) empresa.setCiudad(empresaUpdate.getCiudad());
        if (empresaUpdate.getCodigoPostal() != null) empresa.setCodigoPostal(empresaUpdate.getCodigoPostal());

        empresa.setUpdatedAt(OffsetDateTime.now());
    }

    public static EmpresaResponse toDTO(
            Empresa empresa,
            List<MonedaResponse> monedas
    ) {
        return EmpresaResponse.builder()
                .id(empresa.getId())
                .nombreLegal(empresa.getNombreLegal())
                .nombreComercial(empresa.getNombreComercial())
                .nifCif(empresa.getNifCif())
                .email(empresa.getEmail())
                .telefono(empresa.getTelefono())
                .direccion(empresa.getDireccion())
                .ciudad(empresa.getCiudad())
                .codigoPostal(empresa.getCodigoPostal())
                .pais(empresa.getPais())
                .createdAt(empresa.getCreatedAt())
                .monedas(monedas != null ? monedas : List.<MonedaResponse>of())
                .build();
    }

    public static List<EmpresaResponse> toDTOList(
            List<Empresa> empresas,
            Map<UUID, List<MonedaResponse>> monedasPorEmpresa
    ) {
        return empresas.stream()
                .map(empresa -> toDTO(
                        empresa,
                        monedasPorEmpresa.getOrDefault(empresa.getId(), List.<MonedaResponse>of())
                ))
                .toList();
    }
}