package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaIntegrationUpdateRequest;
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
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.EmpresaService;
import com.feuca.facturacion.service.SecretEncryptionService;
import com.feuca.facturacion.util.DataNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final SecretEncryptionService secretEncryptionService;
    private final AccessControlService accessControlService;

    @Autowired
    public EmpresaServiceImpl(
        EmpresaRepository empresaRepository,
        EmpresaMonedaRepository empresaMonedaRepository,
        MonedaRepository monedaRepository,
        SecretEncryptionService secretEncryptionService,
        AccessControlService accessControlService
    ) {
        this.empresaRepository = empresaRepository;
        this.empresaMonedaRepository = empresaMonedaRepository;
        this.monedaRepository = monedaRepository;
        this.secretEncryptionService = secretEncryptionService;
        this.accessControlService = accessControlService;
    }

    //CREATE
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public EmpresaResponse create(EmpresaRequest empresa) {
        accessControlService.requireSuperAdmin();

        String nombreEmpresaNormalizado = DataNormalizer.displayText(empresa.getNombreLegal());
        String nifCifNormalizado = DataNormalizer.identifier(empresa.getNit());
        String correoNormalizado = DataNormalizer.email(empresa.getEmail());
        String telefonoNormalizado = DataNormalizer.phone(empresa.getTelefono());

        boolean existeNombreLegal = empresaRepository.existsByNombreLegalIgnoreCase(nombreEmpresaNormalizado);
        boolean existeNifCif = empresaRepository.existsByNit(nifCifNormalizado);

        if (existeNombreLegal) throw new EmpresaAlredyExistsException("El nombre \"" + empresa.getNombreLegal() + "\" ya ha sido asignado a una empresa.");
        if (existeNifCif) throw new EmpresaNifCifAlredyExistsException("El NIT \"" + empresa.getNit() + "\" ya ha sido asignado a una empresa.");

        if (correoNormalizado != null) {
            boolean existeEmail = empresaRepository.existsByEmailIgnoreCase(correoNormalizado);
            if (existeEmail) throw new EmpresaEmailAlredyExistsException("El email \"" + empresa.getEmail() + "\" ya ha sido asignado a una empresa.");
        }

        if (telefonoNormalizado != null) {
            boolean existeTelefono = empresaRepository.existsByTelefono(telefonoNormalizado);
            if (existeTelefono) throw new EmpresaTelefonoAlredyExistsException("El numero telefonico \"" + empresa.getTelefono() + "\" ya ha sido asignado a una empresa.");
        }

        Empresa nuevaEmpresa = EmpresaMapper.toEntityCreate(empresa, secretEncryptionService);

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
        accessControlService.requireEmpresaAccess(empresa.getId());

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(id)
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByNombreLegal(String nombreLegal) {

        String nombreEmpresaNormalizado = DataNormalizer.displayText(nombreLegal);

        Empresa empresa = empresaRepository.findByNombreLegalIgnoreCase(nombreEmpresaNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));
        accessControlService.requireEmpresaAccess(empresa.getId());

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByNit(String nit) {

        String nitNormalizado = DataNormalizer.identifier(nit);

        Empresa empresa = empresaRepository.findByNit(nitNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));
        accessControlService.requireEmpresaAccess(empresa.getId());

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByEmail(String email) {

        String emailNormalizado = DataNormalizer.email(email);

        Empresa empresa = empresaRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));
        accessControlService.requireEmpresaAccess(empresa.getId());

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByTelefono(String telefono) {

        String telefonoNormalizado = DataNormalizer.phone(telefono);

        Empresa empresa = empresaRepository.findByTelefono(telefonoNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));
        accessControlService.requireEmpresaAccess(empresa.getId());

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public EmpresaResponse getByNombreComercialAndDireccion(String nombreComercial, String direccion) {

        String nombreEmpresaNormalizado = DataNormalizer.displayText(nombreComercial);
        String direccionNormalizada = DataNormalizer.displayText(direccion);

        Empresa empresa = empresaRepository.findByNombreComercialAndDireccion(
                nombreEmpresaNormalizado,
                direccionNormalizada
        ).orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));
        accessControlService.requireEmpresaAccess(empresa.getId());

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    public List<EmpresaResponse> getAllByNombreComercial(String nombreComercial) {
        String nombreEmpresaNormalizado = DataNormalizer.displayText(nombreComercial);

        List<Empresa> empresas = empresaRepository.findAllByNombreComercial(nombreEmpresaNormalizado);
        empresas = filterNotDeleted(empresas);
        empresas = filterByCurrentUserAccess(empresas);

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
                                MonedaMapper::toDTO,
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    @Override
    public List<EmpresaResponse> getAllByCiudad(String ciudad) {

        String ciudadNormalizado = DataNormalizer.displayText(ciudad);

        List<Empresa> empresas = empresaRepository.findAllByCiudad(ciudadNormalizado);
        empresas = filterNotDeleted(empresas);
        empresas = filterByCurrentUserAccess(empresas);

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
                                MonedaMapper::toDTO,
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    @Override
    public List<EmpresaResponse> getAllByCodigoPostal(String codigoPostal) {

        String codigoPostalNormalizado = DataNormalizer.identifier(codigoPostal);

        List<Empresa> empresas = empresaRepository.findAllByCodigoPostal(codigoPostalNormalizado);
        empresas = filterNotDeleted(empresas);
        empresas = filterByCurrentUserAccess(empresas);

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
                                MonedaMapper::toDTO,
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    @Override
    public List<EmpresaResponse> getAllByPais(String pais) {

        String paisNormalizado = DataNormalizer.displayText(pais);

        List<Empresa> empresas = empresaRepository.findAllByPais(paisNormalizado);
        empresas = filterNotDeleted(empresas);
        empresas = filterByCurrentUserAccess(empresas);

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
                                MonedaMapper::toDTO,
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    @Override
    public List<EmpresaResponse> getAll() {

        List<Empresa> empresas = accessControlService.isSuperAdmin()
                ? empresaRepository.findAllByDeletedAtIsNull()
                : empresaRepository.findAllByIdInAndDeletedAtIsNull(accessControlService.getCurrentUserEmpresaIds());

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
                                MonedaMapper::toDTO,
                                Collectors.toList()
                        )
                ));

        return EmpresaMapper.toDTOList(empresas, monedasXEmpresa);
    }

    //UPDATE

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public EmpresaResponse update(UUID idEmpresa, EmpresaUpdateRequest empresaRequest) {
        accessControlService.requireSuperAdmin();

        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        EmpresaMapper.applyBusinessUpdate(empresa, empresaRequest);

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        return EmpresaMapper.toDTO(empresaRepository.save(empresa), monedas);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public EmpresaResponse updateIntegration(UUID idEmpresa, EmpresaIntegrationUpdateRequest integrationRequest) {
        accessControlService.requireSuperAdmin();

        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        EmpresaMapper.applyIntegrationCredentialsUpdate(empresa, integrationRequest, secretEncryptionService);

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        return EmpresaMapper.toDTO(empresaRepository.save(empresa), monedas);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public EmpresaResponse updateMonedas(UUID idEmpresa, AddMonedaRequest monedasRequest) {
        accessControlService.requireSuperAdmin();

        if (monedasRequest.getCodigos() == null || monedasRequest.getCodigos().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos una moneda");
        }

        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        Set<String> codigos = monedasRequest.getCodigos().stream()
                .map(DataNormalizer::identifier)
                .filter(codigo -> codigo != null && !codigo.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (codigos.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos una moneda valida");
        }

        List<Moneda> monedas = monedaRepository.findAllById(codigos);

        if (monedas.size() != codigos.size())
            throw new MonedaNotFoundException("Una o mas monedas no existen.");

        List<EmpresaMoneda> relExistentes = empresaMonedaRepository.findAllByEmpresa_id(idEmpresa);

        Set<String> codigosExistentes = relExistentes.stream()
                .map(EmpresaMoneda::getMoneda_codigo)
                .collect(Collectors.toSet());

        Set<String> codigosFinales = new HashSet<>(codigosExistentes);
        codigosFinales.addAll(codigos);

        String monedaPrincipal = DataNormalizer.identifier(monedasRequest.getMonedaPrincipal());
        if (monedaPrincipal != null && !codigosFinales.contains(monedaPrincipal)) {
            throw new IllegalArgumentException("La moneda principal debe estar asignada a la empresa");
        }

        boolean existePrincipal = relExistentes.stream()
                .anyMatch(rel -> Boolean.TRUE.equals(rel.getPrincipal()));
        String principalEfectiva = monedaPrincipal;
        if (principalEfectiva == null && !existePrincipal) {
            principalEfectiva = relExistentes.stream()
                    .map(EmpresaMoneda::getMoneda_codigo)
                    .findFirst()
                    .orElseGet(() -> codigos.iterator().next());
        }

        if (principalEfectiva != null) {
            for (EmpresaMoneda rel : relExistentes) {
                rel.setPrincipal(principalEfectiva.equals(rel.getMoneda_codigo()));
            }
        }
        final String principalSeleccionada = principalEfectiva;

        List<EmpresaMoneda> relNuevas = monedas.stream()
                .filter(moneda -> !codigosExistentes.contains(moneda.getCodigo()))
                .map(moneda -> EmpresaMoneda.builder()
                        .empresa_id(idEmpresa)
                        .moneda_codigo(moneda.getCodigo())
                        .principal(moneda.getCodigo().equals(principalSeleccionada))
                        .build())
                .toList();

        List<EmpresaMoneda> relacionesAGuardar = new ArrayList<>();
        relacionesAGuardar.addAll(relNuevas);
        if (principalEfectiva != null) {
            relacionesAGuardar.addAll(relExistentes);
        }

        if (!relacionesAGuardar.isEmpty()) {
            empresaMonedaRepository.saveAll(relacionesAGuardar);
        }

        List<MonedaResponse> monedasEmpresa = empresaMonedaRepository
                .findAllByEmpresa_id(idEmpresa)
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        return EmpresaMapper.toDTO(empresa, monedasEmpresa);
    }

    //DELETE

    @Override
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public EmpresaResponse deleteById(UUID id) {
        accessControlService.requireSuperAdmin();

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public EmpresaResponse deleteByNombreLegal(String nombreLegal) {
        accessControlService.requireSuperAdmin();

        String nombreNormalizado = DataNormalizer.displayText(nombreLegal);

        Empresa empresa = empresaRepository.findByNombreLegalIgnoreCase(nombreNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public EmpresaResponse deleteByNit(String nit) {
        accessControlService.requireSuperAdmin();

        String nitNormalizado = DataNormalizer.identifier(nit);

        Empresa empresa = empresaRepository.findByNit(nitNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public EmpresaResponse deleteByEmail(String email) {
        accessControlService.requireSuperAdmin();

        String emailNormalizado = DataNormalizer.email(email);

        Empresa empresa = empresaRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    @Override
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public EmpresaResponse deleteByTelefono(String telefono) {
        accessControlService.requireSuperAdmin();

        String telefonoNormalizado = DataNormalizer.phone(telefono);

        Empresa empresa = empresaRepository.findByTelefono(telefonoNormalizado)
                .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));

        List<MonedaResponse> monedas = empresaMonedaRepository
                .findAllByEmpresa_id(empresa.getId())
                .stream()
                .map(MonedaMapper::toDTO)
                .toList();

        empresaRepository.delete(empresa);

        return EmpresaMapper.toDTO(empresa, monedas);
    }

    private List<Empresa> filterByCurrentUserAccess(List<Empresa> empresas) {
        if (accessControlService.isSuperAdmin()) {
            return empresas;
        }
        Set<UUID> allowedIds = new HashSet<>(accessControlService.getCurrentUserEmpresaIds());
        return empresas.stream()
                .filter(empresa -> allowedIds.contains(empresa.getId()))
                .toList();
    }

    private List<Empresa> filterNotDeleted(List<Empresa> empresas) {
        return empresas.stream()
                .filter(empresa -> empresa.getDeletedAt() == null)
                .toList();
    }
}
