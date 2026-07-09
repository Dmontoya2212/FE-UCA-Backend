package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Auth.LoginRequest;
import com.feuca.facturacion.dto.response.Auth.LoginResponse;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.exception.Usuario.InvalidCredentialsException;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String emailNormalizado = request.getEmail().toLowerCase().trim();

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new InvalidCredentialsException("Correo electrónico o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword_hash())) {
            throw new InvalidCredentialsException("Correo electrónico o contraseña incorrectos.");
        }

        if (usuario.getActivo() == null || !usuario.getActivo()) {
            throw new InvalidCredentialsException("El usuario se encuentra inactivo.");
        }

        // Generar un token de sesión ficticio/simple
        String sessionToken = "session-" + UUID.randomUUID().toString();

        return LoginResponse.builder()
                .id(usuario.getId())
                .empresaId(usuario.getEmpresaId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .esAdmin(usuario.getEs_admin())
                .token(sessionToken)
                .build();
    }
}
