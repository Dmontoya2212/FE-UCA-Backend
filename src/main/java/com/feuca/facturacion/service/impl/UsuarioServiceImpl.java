package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Usuario.UsuarioRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.dto.response.Usuario.UsuarioResponse;
import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.exception.Usuario.UsuarioAlreadyExistsException;
import com.feuca.facturacion.exception.Usuario.UsuarioNotFoundException;
import com.feuca.facturacion.mapper.UsuarioMapper;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponse create(UsuarioRequest req) {

        String emailNormalizado = req.getEmail().toLowerCase().trim();

        boolean exists = usuarioRepository.existsByEmpresaIdAndEmail(req.getEmpresaId(), emailNormalizado);
        if (exists) throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email en la empresa.");

        String hash = passwordEncoder.encode(req.getPassword());

        Usuario entity = UsuarioMapper.toEntityCreate(
                UsuarioRequest.builder()
                        .empresaId(req.getEmpresaId())
                        .nombre(req.getNombre())
                        .email(emailNormalizado)
                        .password(req.getPassword())
                        .esAdmin(req.getEsAdmin())
                        .build(),
                hash
        );

        Usuario saved = usuarioRepository.save(entity);
        return UsuarioMapper.toResponse(saved);
    }

    @Override
    public UsuarioResponse getById(UUID empresaId, UUID usuarioId) {
        Usuario u = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado."));
        return UsuarioMapper.toResponse(u);
    }

    @Override
    public UsuarioResponse getByEmail(UUID empresaId, String email) {
        String emailNormalizado = email.toLowerCase().trim();
        Usuario u = usuarioRepository.findByEmpresaIdAndEmail(empresaId, emailNormalizado)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado."));
        return UsuarioMapper.toResponse(u);
    }

    @Override
    public List<UsuarioResponse> getAllByEmpresa(UUID empresaId) {
        return usuarioRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(UsuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioResponse update(UUID empresaId, UUID usuarioId, UsuarioUpdateRequest req) {

        Usuario u = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado."));

        // si cambia email, validar duplicado por empresa
        if (req.getEmail() != null) {
            String emailNormalizado = req.getEmail().toLowerCase().trim();

            boolean exists = usuarioRepository.existsByEmpresaIdAndEmail(empresaId, emailNormalizado);
            if (exists && !emailNormalizado.equalsIgnoreCase(u.getEmail())) {
                throw new UsuarioAlreadyExistsException("Ya existe un usuario con ese email en la empresa.");
            }
            req.setEmail(emailNormalizado);
        }

        String newHash = null;
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            newHash = passwordEncoder.encode(req.getPassword());
        }

        UsuarioMapper.applyUpdate(u, req, newHash);
        Usuario saved = usuarioRepository.save(u);

        return UsuarioMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID empresaId, UUID usuarioId) {
        Usuario u = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado."));
        usuarioRepository.delete(u);
    }
}