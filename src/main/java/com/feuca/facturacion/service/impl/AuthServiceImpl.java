package com.feuca.facturacion.service.impl;
import com.feuca.facturacion.dto.request.Auth.LoginRequest;
import com.feuca.facturacion.dto.response.Auth.LoginResponse;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.exception.Usuario.InvalidCredentialsException;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.AuditService;
import com.feuca.facturacion.service.AuthService;
import com.feuca.facturacion.service.JwtService;
import com.feuca.facturacion.service.OperationalMetricsService;
import com.feuca.facturacion.util.DataNormalizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final OperationalMetricsService operationalMetricsService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuditService auditService,
                           OperationalMetricsService operationalMetricsService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.operationalMetricsService = operationalMetricsService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            LoginResponse response = doLogin(request);
            auditService.recordLogin(request.getEmail(), true, "reason=credentials_valid");
            return response;
        } catch (RuntimeException exception) {
            auditService.recordLogin(request.getEmail(), false, "reason=" + exception.getClass().getSimpleName());
            operationalMetricsService.recordAuthenticationFailure(exception.getClass().getSimpleName());
            throw exception;
        }
    }

    private LoginResponse doLogin(LoginRequest request) {
        String emailNormalizado = DataNormalizer.email(request.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new InvalidCredentialsException("Correo electrónico o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new InvalidCredentialsException("Correo electrónico o contraseña incorrectos.");
        }

        if (usuario.getActivo() == null || !usuario.getActivo()) {
            throw new InvalidCredentialsException("El usuario se encuentra inactivo.");
        }

        String rol = usuario.getRol();
        if (rol == null || rol.isBlank()) {
            throw new InvalidCredentialsException("El usuario no tiene un rol asignado.");
        }
        if (!List.of(AccessControlService.SUPERADMIN, AccessControlService.ADMINISTRADOR, AccessControlService.USUARIO).contains(rol)) {
            throw new InvalidCredentialsException("El usuario tiene un rol no permitido.");
        }

        List<java.util.UUID> empresaIds = usuario.getEmpresas() == null
                ? List.of()
                : usuario.getEmpresas().stream()
                        .map(Empresa::getId)
                        .toList();

        String token = jwtService.generateToken(
                usuario.getId(),
                usuario.getEmail(),
                rol
        );

        return LoginResponse.builder()
                .id(usuario.getId())
                .empresaIds(empresaIds)
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(rol)
                .token(token)
                .build();
    }
}
