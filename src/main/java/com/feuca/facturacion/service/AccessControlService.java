package com.feuca.facturacion.service;

import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.exception.Usuario.UsuarioNotFoundException;
import com.feuca.facturacion.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AccessControlService {

    public static final String SUPERADMIN = "SUPERADMIN";
    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String USUARIO = "USUARIO";

    private static final Set<String> ROLES_VALIDOS = Set.of(SUPERADMIN, ADMINISTRADOR, USUARIO);

    private final UsuarioRepository usuarioRepository;

    public AccessControlService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public boolean isSuperAdmin() {
        return hasRole(SUPERADMIN);
    }

    public boolean esSuperadmin() {
        return isSuperAdmin();
    }

    public boolean isAdministradorOrSuperAdmin() {
        return hasAnyRole(SUPERADMIN, ADMINISTRADOR);
    }

    public Usuario obtenerUsuarioActual() {
        return getCurrentUsuario();
    }

    public UUID obtenerIdUsuarioActual() {
        return getCurrentUserId();
    }

    public String obtenerRolActual() {
        Authentication auth = getAuthentication();
        return auth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(ROLES_VALIDOS::contains)
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Rol no permitido para el usuario autenticado."));
    }

    public List<UUID> obtenerEmpresasAutorizadas() {
        return getCurrentUserEmpresaIds();
    }

    public void requireSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new AccessDeniedException("Solo SUPERADMIN puede realizar esta operacion.");
        }
    }

    public void requireAdministradorOrSuperAdmin() {
        if (!isAdministradorOrSuperAdmin()) {
            throw new AccessDeniedException("Solo ADMINISTRADOR o SUPERADMIN puede realizar esta operacion.");
        }
    }

    public void validarAccesoAEmpresa(UUID empresaId) {
        requireEmpresaAccess(empresaId);
    }

    public void validarAdministradorDeEmpresa(UUID empresaId) {
        requireAdministradorOrSuperAdmin();
        requireEmpresaAccess(empresaId);
    }

    public void requireEmpresaAccess(UUID empresaId) {
        if (empresaId == null) {
            throw new AccessDeniedException("Debe indicar una empresa.");
        }

        if (isSuperAdmin()) {
            return;
        }

        Usuario usuario = getCurrentUsuario();
        boolean assigned = usuario.getEmpresas() != null && usuario.getEmpresas().stream()
                .map(Empresa::getId)
                .anyMatch(empresaId::equals);

        if (!assigned) {
            throw new AccessDeniedException("No tiene acceso a la empresa indicada.");
        }
    }

    public List<UUID> getCurrentUserEmpresaIds() {
        Usuario usuario = getCurrentUsuario();
        if (usuario.getEmpresas() == null) {
            return List.of();
        }
        return usuario.getEmpresas().stream()
                .map(Empresa::getId)
                .toList();
    }

    public UUID getCurrentUserId() {
        Authentication auth = getAuthentication();
        return UUID.fromString(auth.getName());
    }

    public void validateRole(String role) {
        if (role == null || role.isBlank()) {
            return;
        }
        if (!ROLES_VALIDOS.contains(role)) {
            throw new AccessDeniedException("Rol no permitido: " + role);
        }
    }

    public void requireValidRole(String role) {
        if (role == null || role.isBlank()) {
            throw new AccessDeniedException("El rol es obligatorio.");
        }
        validateRole(role);
    }

    private boolean hasRole(String role) {
        return hasAnyRole(role);
    }

    private boolean hasAnyRole(String... roles) {
        Set<String> expected = Set.of(roles);
        Authentication auth = getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> expected.contains(a.getAuthority()));
    }

    private Usuario getCurrentUsuario() {
        UUID userId = getCurrentUserId();
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario autenticado no encontrado."));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new AccessDeniedException("Usuario autenticado inactivo.");
        }
        return usuario;
    }

    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Usuario no autenticado.");
        }
        return auth;
    }
}
