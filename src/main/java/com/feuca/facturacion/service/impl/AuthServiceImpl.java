package com.feuca.facturacion.service.impl;
import java.util.UUID;
import com.feuca.facturacion.dto.request.Auth.LoginRequest;
import com.feuca.facturacion.dto.response.Auth.LoginResponse;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.exception.Usuario.InvalidCredentialsException;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AuthService;
import com.feuca.facturacion.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String emailNormalizado = request.getEmail().toLowerCase().trim();

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new InvalidCredentialsException("Correo electrónico o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new InvalidCredentialsException("Correo electrónico o contraseña incorrectos.");
        }

        if (usuario.getActivo() == null || !usuario.getActivo()) {
            throw new InvalidCredentialsException("El usuario se encuentra inactivo.");
        }

        // Determine rol: use the rol field, fall back to esAdmin for backward compat
        String rol = usuario.getRol();
        if (rol == null || rol.isBlank()) {
            rol = (usuario.getEsAdmin() != null && usuario.getEsAdmin()) ? "ADMINISTRADOR" : "USUARIO";
        }

        java.util.UUID firstEmpresaId = null;
        if (usuario.getEmpresas() != null && !usuario.getEmpresas().isEmpty()) {
            firstEmpresaId = usuario.getEmpresas().get(0).getId();
        }

        // Generate real JWT token
        String token = jwtService.generateToken(
                usuario.getId(),
                firstEmpresaId != null ? firstEmpresaId : UUID.randomUUID(), // fallback if none assigned
                usuario.getEmail(),
                rol
        );

        return LoginResponse.builder()
                .id(usuario.getId())
                .empresaId(firstEmpresaId)
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .esAdmin(usuario.getEsAdmin())
                .rol(rol)
                .token(token)
                .build();
    }
}
