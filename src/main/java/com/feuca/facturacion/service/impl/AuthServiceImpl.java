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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String INVALID_CREDENTIALS = "Correo electrónico o contraseña incorrectos.";
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoO5Yq4fH5ZQj6tQeKzYd8Y8Pc7gO5xE6W";

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

        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail(emailNormalizado);
        String storedHash = usuarioEncontrado
                .map(Usuario::getPasswordHash)
                .filter(hash -> !hash.isBlank())
                .orElse(DUMMY_PASSWORD_HASH);

        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), storedHash);
        if (usuarioEncontrado.isEmpty() || !passwordMatches) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }
        Usuario usuario = usuarioEncontrado.get();

        if (usuario.getActivo() == null || !usuario.getActivo()) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        String rol = usuario.getRol();
        if (rol == null || rol.isBlank()) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }
        if (!List.of(AccessControlService.SUPERADMIN, AccessControlService.ADMINISTRADOR, AccessControlService.USUARIO).contains(rol)) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
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
