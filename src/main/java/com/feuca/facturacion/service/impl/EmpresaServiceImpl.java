package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.request.Moneda.AddMonedaRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.EmpresaMoneda;
import com.feuca.facturacion.entity.Moneda;
import com.feuca.facturacion.exception.Empresa.*;
import com.feuca.facturacion.exception.Moneda.MonedaNotFoundException;
import com.feuca.facturacion.mapper.EmpresaMapper;
import com.feuca.facturacion.mapper.MonedaMapper;
import com.feuca.facturacion.repository.EmpresaRepository;
import com.feuca.facturacion.repository.EmpresaMonedaRepository;
import com.feuca.facturacion.repository.MonedaRepository;
import com.feuca.facturacion.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMonedaRepository empresaMonedaRepository;
    private final MonedaRepository monedaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmpresaServiceImpl(
        EmpresaRepository empresaRepository,
        EmpresaMonedaRepository empresaMonedaRepository,
        MonedaRepository monedaRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.empresaRepository = empresaRepository;
        this.empresaMonedaRepository = empresaMonedaRepository;
        this.monedaRepository = monedaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //CREATE
    @Override
    @Transactional
    public EmpresaResponse create(EmpresaRequest empresa) {

        String nombreEmpresaNormalizado = empresa.getNombreLegal().toLowerCase().trim();
        String nifCifNormalizado = empresa.getNit().trim();
        String correoNormalizado = empresa.getEmail() != null ? empresa.getEmail().trim() : null;
        String telefonoNormalizado = empresa.getTelefono() != null ? empresa.getTelefono().trim() : null;

        boolean existeNombreLegal = empresaRepository.existsByNombreLegal(nombreEmpresaNormalizado);
        boolean existeNifCif = empresaRepository.existsByNit(nifCifNormalizado);

        if (existeNombreLegal) throw new EmpresaAlredyExistsException("El nombre \"" + empresa.getNombreLegal() + "\" ya ha sido asignado a una empresa.");
        if (existeNifCif) throw new EmpresaNifCifAlredyExistsException("El NIT \"" + empresa.getNit() + "\" ya ha sido asignado a una empresa.");

        if (correoNormalizado != null) {
            boolean existeEmail = empresaRepository.existsByEmail(correoNormalizado);
            if (existeEmail) throw new EmpresaEmailAlredyExistsException("El email \"" + empresa.getEmail() + "\" ya ha sido asignado a una empresa.");
        }

        if (telefonoNormalizado != null) {
            boolean existeTelefono = empresaRepository.existsByTelefono(telefonoNormalizado);
            if (existeTelefono) throw new EmpresaTelefonoAlredyExistsException("El numero telefonico \"" + empresa.getTelefono() + "\" ya ha sido asignado a una empresa.");
        }

        Empresa nuevaEmpresa = EmpresaMapper.toEntityCreate(empresa, passwordEncoder);

        List<MonedaResponse> monedas = List.of();

        return EmpresaMapper.toDTO(
                empresaRepository.save(nuevaEmpresa),
                monedas
        );
    }

    //READ
    @Override
    public EmpresaResponse getById(UUID id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(id)
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByNombreLegal(String nombreLegal) {

        String nombreEmpresaNormalizado = nombreLegal.toLowerCase().trim();

        Empresa empresa = empresaRepository.findByNombreLegal(nombreEmpresaNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByNit(String nit) {

        String nitNormalizado = nit.trim();

        Empresa empresa = empresaRepository.findByNit(nitNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByEmail(String email) {

        String emailNormalizado = email.trim();

        Empresa empresa = empresaRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByTelefono(String telefono) {

        String telefonoNormalizado = telefono.trim();

        Empresa empresa = empresaRepository.findByTelefono(telefonoNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByNombreComercialAndDireccion(String nombreComercial, String direccion) {

        String nombreEmpresaNormalizado = nombreComercial.toLowerCase().trim();
        String direccionNormalizada = direccion.toLowerCase().trim();

        Empresa empresa = empresaRepository.findByNombreComercialAndDireccion(
                nombreEmpresaNormalizado,
                direccionNormalizada
        ).orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public List<EmpresaResponse> getAllByNombreComercial(String nombreComercial) {
        String nombreEmpresaNormalizado = nombreComercial.toLowerCase().trim();

        List<Empresa> empresas = empresaRepository.findAllByNombreComercial(nombreEmpresaNormalizado);

        if (empresas.isEmpty()) {
            return List.of();
        }

        List<UUID> empresaIds = empresas.stream()
                .map(Empresa::getId)
                .toList();

        List<EmpresaMoneda> relaciones = empresaMonedaRepository.findAllByEmpresa_idIn(empresaIds);

        Map<UUID, List<MonedaResponse>> monedasXEmpresa = relaciones.stream()
                .collect(Collectors.groupingBy(
                        EmpresaMoneda::getEmpresa_id,
                        Collectors.mapping(
                                rel -> MonedaMapper.toDTO(rel.getMoneda()),
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    @Override
    public List<EmpresaResponse> getAllByCiudad(String ciudad) {

        String ciudadNormalizado = ciudad.toLowerCase().trim();

        List<Empresa> empresas = empresaRepository.findAllByCiudad(ciudadNormalizado);

        if (empresas.isEmpty()) {
            return List.of();
        }

        List<UUID> empresaIds = empresas.stream()
                .map(Empresa::getId)
                .toList();

        List<EmpresaMoneda> relaciones = empresaMonedaRepository.findAllByEmpresa_idIn(empresaIds);

        Map<UUID, List<MonedaResponse>> monedasXEmpresa = relaciones.stream()
                .collect(Collectors.groupingBy(
                        EmpresaMoneda::getEmpresa_id,
                        Collectors.mapping(
                                rel -> MonedaMapper.toDTO(rel.getMoneda()),
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    @Override
    public List<EmpresaResponse> getAllByCodigoPostal(String codigoPostal) {

        String codigoPostalNormalizado = codigoPostal.toLowerCase().trim();

        List<Empresa> empresas = empresaRepository.findAllByCodigoPostal(codigoPostalNormalizado);

        if (empresas.isEmpty()) {
            return List.of();
        }

        List<UUID> empresaIds = empresas.stream()
                .map(Empresa::getId)
                .toList();

        List<EmpresaMoneda> relaciones = empresaMonedaRepository.findAllByEmpresa_idIn(empresaIds);

        Map<UUID, List<MonedaResponse>> monedasXEmpresa = relaciones.stream()
                .collect(Collectors.groupingBy(
                        EmpresaMoneda::getEmpresa_id,
                        Collectors.mapping(
                                rel -> MonedaMapper.toDTO(rel.getMoneda()),
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    @Override
    public List<EmpresaResponse> getAllByPais(String pais) {

        String paisNormalizado = pais.toLowerCase().trim();

        List<Empresa> empresas = empresaRepository.findAllByPais(paisNormalizado);

        if (empresas.isEmpty()) {
            return List.of();
        }

        List<UUID> empresaIds = empresas.stream()
                .map(Empresa::getId)
                .toList();

        List<EmpresaMoneda> relaciones = empresaMonedaRepository.findAllByEmpresa_idIn(empresaIds);

        Map<UUID, List<MonedaResponse>> monedasXEmpresa = relaciones.stream()
                .collect(Collectors.groupingBy(
                        EmpresaMoneda::getEmpresa_id,
                        Collectors.mapping(
                                rel -> MonedaMapper.toDTO(rel.getMoneda()),
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    @Override
    public List<EmpresaResponse> getAll() {

        List<Empresa> empresas = empresaRepository.findAll();

        if (empresas.isEmpty()) {
            return List.of();
        }

        List<UUID> empresaIds = empresas.stream()
                .map(Empresa::getId)
                .toList();

        List<EmpresaMoneda> relaciones = empresaMonedaRepository.findAllByEmpresa_idIn(empresaIds);

        Map<UUID, List<MonedaResponse>> monedasXEmpresa = relaciones.stream()
                .collect(Collectors.groupingBy(
                        EmpresaMoneda::getEmpresa_id,
                        Collectors.mapping(
                                rel -> MonedaMapper.toDTO(rel.getMoneda()),
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    //UPDATE

    @Override
    @Transactional
    public EmpresaResponse update(UUID idEmpresa, EmpresaUpdateRequest empresaRequest) {

        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        EmpresaMapper.applyUpdate(empresa, empresaRequest, passwordEncoder);

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        return EmpresaMapper.toDTO(empresaRepository.save(empresa), monedas);
    }

    @Override
    @Transactional
    public EmpresaResponse updateMonedas(UUID idEmpresa, AddMonedaRequest monedasRequest) {

        if (monedasRequest.getCodigos() == null || monedasRequest.getCodigos().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos una moneda");
        }

        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        Set<String> codigos = new HashSet<>(monedasRequest.getCodigos());

        List<Moneda> monedas = monedaRepository.findAllById(codigos);

        if (monedas.size() != monedasRequest.getCodigos().size())
            throw new MonedaNotFoundException("Una o mas monedas no existen.");

        List<EmpresaMoneda> relExistentes = empresaMonedaRepository.findAllByEmpresa_id(idEmpresa);

        Set<String> codigosExistentes = relExistentes.stream()
                .map(EmpresaMoneda::getMoneda_codigo)
                .collect(Collectors.toSet());

        List<EmpresaMoneda> relNuevas = monedas.stream()
                .filter(moneda -> !codigosExistentes.contains(moneda.getCodigo()))
                .map(moneda -> EmpresaMoneda.builder()
                        .empresa_id(idEmpresa)
                        .moneda_codigo(moneda.getCodigo())
                        .build())
                .toList();

        if (!relNuevas.isEmpty()) {
            empresaMonedaRepository.saveAll(relNuevas);
        }

        List<MonedaResponse> monedasEmpresa = empresaMonedaRepository
                .findAllByEmpresa_id(idEmpresa)
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        return EmpresaMapper.toDTO(empresa, monedasEmpresa);
    }

    //DELETE

    @Override
    public EmpresaResponse deleteById(UUID id) {

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse deleteByNombreLegal(String nombreLegal) {

        String nombreNormalizado = nombreLegal.toLowerCase().trim();

        Empresa empresa = empresaRepository.findByNombreLegal(nombreNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse deleteByNit(String nit) {

        String nitNormalizado = nit.trim();

        Empresa empresa = empresaRepository.findByNit(nitNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse deleteByEmail(String email) {

        String emailNormalizado = email.trim();

        Empresa empresa = empresaRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse deleteByTelefono(String telefono) {

        String telefonoNormalizado = telefono.trim();

        Empresa empresa = empresaRepository.findByTelefono(telefonoNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }
}

