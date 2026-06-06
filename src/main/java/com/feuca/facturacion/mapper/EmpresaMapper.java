package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import com.feuca.facturacion.entity.Empresa;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmpresaMapper {
    private EmpresaMapper() {}

    public static Empresa toEntityCreate(EmpresaRequest req, PasswordEncoder encoder) {
        return Empresa.builder()
                .id(UUID.randomUUID())
                .razonSocial(req.getRazonSocial() != null ? req.getRazonSocial().toLowerCase().trim() : null)
                .nombreLegal(req.getNombreLegal() != null ? req.getNombreLegal().toLowerCase().trim() : null)
                .nombreComercial(req.getNombreComercial() != null ? req.getNombreComercial().toLowerCase().trim() : null)
                .nit(req.getNit() != null ? req.getNit().trim() : null)
                .registro(req.getRegistro() != null ? req.getRegistro().trim() : null)
                .actividadEconomica(req.getActividadEconomica() != null ? req.getActividadEconomica().toLowerCase().trim() : null)
                .sectorEmpresa(req.getSectorEmpresa() != null ? req.getSectorEmpresa().toLowerCase().trim() : null)
                .email(req.getEmail() != null ? req.getEmail().toLowerCase().trim() : null)
                .telefono(req.getTelefono() != null ? req.getTelefono().trim() : null)
                .direccion(req.getDireccion() != null ? req.getDireccion().toLowerCase().trim() : null)
                .ciudad(req.getCiudad() != null ? req.getCiudad().toLowerCase().trim() : null)
                .codigoPostal(req.getCodigoPostal() != null ? req.getCodigoPostal().trim() : null)
                .pais(req.getPais() != null ? req.getPais().toLowerCase().trim() : null)
                .usuario(req.getUsuario() != null ? req.getUsuario().trim() : null)
                .passwordHash(req.getPassword() != null ? encoder.encode(req.getPassword()) : null)
                .clavePrimaria(req.getClavePrimaria() != null ? encoder.encode(req.getClavePrimaria()) : null)
                .token(req.getToken())
                .expireToken(req.getExpireToken())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static void applyUpdate(Empresa e, EmpresaUpdateRequest req, PasswordEncoder encoder) {
        if (req.getRazonSocial() != null) e.setRazonSocial(req.getRazonSocial().toLowerCase().trim());
        if (req.getNombreLegal() != null) e.setNombreLegal(req.getNombreLegal().toLowerCase().trim());
        if (req.getNombreComercial() != null) e.setNombreComercial(req.getNombreComercial().toLowerCase().trim());
        if (req.getNit() != null) e.setNit(req.getNit().trim());
        if (req.getRegistro() != null) e.setRegistro(req.getRegistro().trim());
        if (req.getActividadEconomica() != null) e.setActividadEconomica(req.getActividadEconomica().toLowerCase().trim());
        if (req.getSectorEmpresa() != null) e.setSectorEmpresa(req.getSectorEmpresa().toLowerCase().trim());
        if (req.getEmail() != null) e.setEmail(req.getEmail().toLowerCase().trim());
        if (req.getTelefono() != null) e.setTelefono(req.getTelefono().trim());
        if (req.getDireccion() != null) e.setDireccion(req.getDireccion().toLowerCase().trim());
        if (req.getCiudad() != null) e.setCiudad(req.getCiudad().toLowerCase().trim());
        if (req.getCodigoPostal() != null) e.setCodigoPostal(req.getCodigoPostal().trim());
        if (req.getPais() != null) e.setPais(req.getPais().toLowerCase().trim());
        if (req.getUsuario() != null) e.setUsuario(req.getUsuario().trim());
        if (req.getPassword() != null) e.setPasswordHash(encoder.encode(req.getPassword()));
        if (req.getClavePrimaria() != null) e.setClavePrimaria(encoder.encode(req.getClavePrimaria()));
        if (req.getToken() != null) e.setToken(req.getToken());
        if (req.getExpireToken() != null) e.setExpireToken(req.getExpireToken());
        e.setUpdatedAt(OffsetDateTime.now());
    }

    public static EmpresaResponse toDTO(Empresa e, List<MonedaResponse> monedas) {
        return EmpresaResponse.builder()
                .id(e.getId())
                .razonSocial(e.getRazonSocial())
                .nombreLegal(e.getNombreLegal())
                .nombreComercial(e.getNombreComercial())
                .nit(e.getNit())
                .registro(e.getRegistro())
                .actividadEconomica(e.getActividadEconomica())
                .sectorEmpresa(e.getSectorEmpresa())
                .email(e.getEmail())
                .telefono(e.getTelefono())
                .direccion(e.getDireccion())
                .ciudad(e.getCiudad())
                .codigoPostal(e.getCodigoPostal())
                .pais(e.getPais())
                .usuario(e.getUsuario())
                .token(e.getToken())
                .expireToken(e.getExpireToken())
                .monedas(monedas)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static List<EmpresaResponse> toDTOList(List<Empresa> empresas, Map<UUID, List<MonedaResponse>> monedasXEmpresa) {
        return empresas.stream()
                .map(empresa -> toDTO(
                        empresa,
                        monedasXEmpresa.getOrDefault(empresa.getId(), List.of())
                ))
                .toList();
    }
}