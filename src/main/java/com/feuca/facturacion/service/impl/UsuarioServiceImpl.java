package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Usuario.UsuarioRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.dto.response.Usuario.UsuarioResponse;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.exception.Usuario.UsuarioAlreadyExistsException;
import com.feuca.facturacion.exception.Usuario.UsuarioNotFoundException;
import com.feuca.facturacion.mapper.UsuarioMapper;
import com.feuca.facturacion.repository.EmpresaRepository;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.UsuarioService;
import com.feuca.facturacion.util.DataNormalizer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@PreAuthorize("hasAuthority('SUPERADMIN')")
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessControlService accessControlService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              EmpresaRepository empresaRepository,
                              PasswordEncoder passwordEncoder,
                              AccessControlService accessControlService) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessControlService = accessControlService;
    }

    @Override
    @Transactional
    public UsuarioResponse create(UsuarioRequest req) {
        accessControlService.requireSuperAdmin();
        accessControlService.requireValidRole(req.getRol());
        validateEmpresaAssignments(req.getRol(), req.getEmpresaIds());
        validatePasswordPolicy(req.getPassword());

        String emailNormalizado = DataNormalizer.email(req.getEmail());

        boolean exists = usuarioRepository.existsByEmailIgnoreCase(emailNormalizado);
        if (exists) throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email en la empresa.");

        String hash = passwordEncoder.encode(req.getPassword());

        Usuario entity = UsuarioMapper.toEntityCreate(
                UsuarioRequest.builder()
                        .empresaIds(req.getEmpresaIds())
                        .nombre(DataNormalizer.displayText(req.getNombre()))
                        .email(emailNormalizado)
                        .password(req.getPassword())
                        .rol(req.getRol())
                        .build(),
                hash
        );

        Usuario saved = usuarioRepository.save(entity);
        return UsuarioMapper.toResponse(saved);
    }

    @Override
    public UsuarioResponse getById(UUID empresaId, UUID usuarioId) {
        accessControlService.requireSuperAdmin();
        Usuario u = usuarioRepository.findByIdAndEmpresasId(usuarioId, empresaId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado."));
        return UsuarioMapper.toResponse(u);
    }

    @Override
    public UsuarioResponse getByEmail(UUID empresaId, String email) {
        accessControlService.requireSuperAdmin();
        String emailNormalizado = DataNormalizer.email(email);
        Usuario u = usuarioRepository.findByEmpresasIdAndEmail(empresaId, emailNormalizado)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado."));
        return UsuarioMapper.toResponse(u);
    }

    @Override
    public List<UsuarioResponse> getAllByEmpresa(UUID empresaId) {
        accessControlService.requireSuperAdmin();
        return usuarioRepository.findAllByEmpresasId(empresaId)
                .stream()
                .map(UsuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioResponse update(UUID empresaId, UUID usuarioId, UsuarioUpdateRequest req) {
        accessControlService.requireSuperAdmin();
        accessControlService.validateRole(req.getRol());

        Usuario u = usuarioRepository.findByIdAndEmpresasId(usuarioId, empresaId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado."));

        validateLastActiveSuperAdminIsPreserved(u, req);

        // si cambia email, validar duplicado por empresa
        if (req.getEmail() != null) {
            String emailNormalizado = DataNormalizer.email(req.getEmail());

            boolean exists = usuarioRepository.existsByEmailIgnoreCase(emailNormalizado);
            if (exists && !emailNormalizado.equalsIgnoreCase(u.getEmail())) {
                throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email en la empresa.");
            }
            req.setEmail(emailNormalizado);
        }

        String targetRole = req.getRol() != null ? req.getRol() : u.getRol();
        if (req.getEmpresaIds() != null) {
            validateEmpresaAssignments(targetRole, req.getEmpresaIds());
        }

        String newHash = null;
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            validatePasswordPolicy(req.getPassword());
            newHash = passwordEncoder.encode(req.getPassword());
        }

        UsuarioMapper.applyProfileUpdate(u, req, newHash);
        UsuarioMapper.applyAdminSecurityUpdate(u, req);
        Usuario saved = usuarioRepository.save(u);

        return UsuarioMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID empresaId, UUID usuarioId) {
        accessControlService.requireSuperAdmin();
        Usuario u = usuarioRepository.findByIdAndEmpresasId(usuarioId, empresaId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado."));
        validateLastActiveSuperAdminDelete(u);
        usuarioRepository.delete(u);
    }

    private void validateLastActiveSuperAdminIsPreserved(Usuario usuario, UsuarioUpdateRequest req) {
        boolean targetIsActiveSuperAdmin = AccessControlService.SUPERADMIN.equals(usuario.getRol())
                && Boolean.TRUE.equals(usuario.getActivo());
        boolean removesSuperAdminRole = req.getRol() != null
                && !AccessControlService.SUPERADMIN.equals(req.getRol());
        boolean deactivatesUser = Boolean.FALSE.equals(req.getActivo());
        boolean removesAllEmpresaAccess = req.getEmpresaIds() != null && req.getEmpresaIds().isEmpty();

        if (targetIsActiveSuperAdmin && isLastActiveSuperAdmin()
                && (removesSuperAdminRole || deactivatesUser || removesAllEmpresaAccess)) {
            throw new AccessDeniedException("No puede modificar el ultimo SUPERADMIN activo de forma que pierda rol, estado activo o acceso.");
        }
    }

    private void validateLastActiveSuperAdminDelete(Usuario usuario) {
        boolean targetIsActiveSuperAdmin = AccessControlService.SUPERADMIN.equals(usuario.getRol())
                && Boolean.TRUE.equals(usuario.getActivo());

        if (targetIsActiveSuperAdmin && isLastActiveSuperAdmin()) {
            throw new AccessDeniedException("No puede eliminar el ultimo SUPERADMIN activo.");
        }
    }

    private boolean isLastActiveSuperAdmin() {
        return usuarioRepository.countByRolAndActivoTrue(AccessControlService.SUPERADMIN) <= 1;
    }

    private void validateEmpresaAssignments(String rol, List<UUID> empresaIds) {
        if (!AccessControlService.SUPERADMIN.equals(rol) && (empresaIds == null || empresaIds.isEmpty())) {
            throw new AccessDeniedException("Debe asignar al menos una empresa al usuario.");
        }
        if (empresaIds == null || empresaIds.isEmpty()) {
            return;
        }
        List<UUID> uniqueIds = empresaIds.stream().distinct().toList();
        List<Empresa> empresas = empresaRepository.findAllByIdInAndDeletedAtIsNull(uniqueIds);
        if (empresas.size() != uniqueIds.size()) {
            throw new AccessDeniedException("Todas las empresas asignadas deben existir y estar activas.");
        }
    }

    private void validatePasswordPolicy(String password) {
        if (password == null || password.length() < 8 || password.length() > 100
                || password.chars().anyMatch(Character::isWhitespace)
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)
                || password.chars().noneMatch(ch -> !Character.isLetterOrDigit(ch))) {
            throw new AccessDeniedException("La contrasena debe tener 8 a 100 caracteres, mayuscula, minuscula, numero, simbolo y no contener espacios.");
        }
    }
}
