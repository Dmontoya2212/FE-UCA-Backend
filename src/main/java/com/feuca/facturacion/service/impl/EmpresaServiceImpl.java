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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMonedaRepository empresaMonedaRepository;
    private final MonedaRepository monedaRepository;

    @Autowired
    public EmpresaServiceImpl (
        EmpresaRepository empresaRepository,
        EmpresaMonedaRepository empresaMonedaRepository,
        MonedaRepository monedaRepository
    ){
        this.empresaRepository = empresaRepository;
        this.empresaMonedaRepository = empresaMonedaRepository;
        this.monedaRepository = monedaRepository;
    }

    //CREATE
    @Override
    @Transactional
    public EmpresaResponse create(EmpresaRequest empresa) {

        String nombreEmpresaNormalizado = empresa.getNombreLegal().toLowerCase().trim();
        String nifCifNormalizado = empresa.getNifCif().toLowerCase().trim();
        String correoNormalizado = empresa.getEmail().trim();
        String telefonoNormalizado = empresa.getTelefono().trim();

        boolean existeNombreLegal = empresaRepository.existsByNombreLegal(nombreEmpresaNormalizado);
        boolean existeNifCif = empresaRepository.existsByNifCif(nifCifNormalizado);
        boolean existeEmail = empresaRepository.existsByEmail(correoNormalizado);
        boolean existeTelefono = empresaRepository.existsByTelefono(telefonoNormalizado);

        if(existeNombreLegal) throw new EmpresaAlredyExistsException("El nombre \"" + empresa.getNombreLegal() + "\" ya ha sido asignado a una empresa.");
        if(existeNifCif) throw new EmpresaNifCifAlredyExistsException("El NIT \"" + empresa.getNifCif() + "\" ya ha sido asignado a una empresa.");
        if(existeEmail) throw  new EmpresaEmailAlredyExistsException("El email \"" + empresa.getEmail() + "\" ya ha sido asignado a una empresa.");
        if(existeTelefono) throw new EmpresaTelefonoAlredyExistsException("El numero telefonico \"" + empresa.getTelefono() + "\" ya ha sido asignado a una empresa.");

        EmpresaRequest empresaNormalizada = EmpresaRequest.builder()
                .nombreLegal(nombreEmpresaNormalizado)
                .nombreComercial(empresa.getNombreComercial().toLowerCase().trim())
                .nifCif(nifCifNormalizado)
                .email(correoNormalizado)
                .telefono(telefonoNormalizado)
                .direccion(empresa.getDireccion().toLowerCase().trim())
                .ciudad(empresa.getCiudad().toLowerCase().trim())
                .codigoPostal(empresa.getCodigoPostal().toLowerCase().trim())
                .build();

        List<MonedaResponse> monedas = List.of();

        return EmpresaMapper.toDTO(
                empresaRepository.save(EmpresaMapper.toEntityCreate(empresaNormalizada)),
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
    public EmpresaResponse getByNifCif(String nifCif) {

        String nifCifNormalizado = nifCif.toLowerCase().trim();

        Empresa empresa = empresaRepository.findByNifCif(nifCifNormalizado)
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

        String nombreLegalNormalizado  = null,
                nombreComercialNormalizado  = null,
                nifCifNormalizado  = null,
                emailNormalizado  = null,
                telefonoNormalizado  = null,
                direccionNormalizada  = null,
                ciudadNormalizada  = null,
                codigoPostalNormalizado = null;

        if(empresaRequest.getNombreLegal() != null) nombreLegalNormalizado = empresaRequest.getNombreLegal().toLowerCase().trim();
        if(empresaRequest.getNombreComercial() != null) nombreComercialNormalizado = empresaRequest.getNombreComercial().toLowerCase().trim();
        if(empresaRequest.getNifCif() != null) nifCifNormalizado = empresaRequest.getNifCif().toLowerCase().trim();
        if(empresaRequest.getEmail() != null) emailNormalizado = empresaRequest.getEmail().trim();
        if(empresaRequest.getTelefono() != null) telefonoNormalizado = empresaRequest.getTelefono().trim();
        if(empresaRequest.getDireccion() != null) direccionNormalizada = empresaRequest.getDireccion().toLowerCase().trim();
        if(empresaRequest.getCiudad() != null) ciudadNormalizada = empresaRequest.getCiudad().toLowerCase().trim();
        if(empresaRequest.getCodigoPostal() != null) codigoPostalNormalizado = empresaRequest.getCodigoPostal().toLowerCase().trim();


        EmpresaUpdateRequest empresaUpdateNormalizada = EmpresaUpdateRequest.builder()
                .nombreLegal(nombreLegalNormalizado)
                .nombreComercial(nombreComercialNormalizado)
                .nifCif(nifCifNormalizado)
                .email(emailNormalizado)
                .telefono(telefonoNormalizado)
                .direccion(direccionNormalizada)
                .ciudad(ciudadNormalizada)
                .codigoPostal(codigoPostalNormalizado)
                .build();

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(rel -> MonedaMapper.toDTO(rel.getMoneda()))
                .toList();

        EmpresaMapper.toEntityUpdate(empresa, empresaUpdateNormalizada);

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

        List<Moneda> monedas =  monedaRepository.findAllById(codigos);

        if(monedas.size() != monedasRequest.getCodigos().size())
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
    public EmpresaResponse deleteByNifCif(String nifCif) {

        String nifCifNormalizado = nifCif.toLowerCase().trim();

        Empresa empresa = empresaRepository.findByNifCif(nifCifNormalizado)
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

        String telefonoNormalizado = telefono.toLowerCase().trim();

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
