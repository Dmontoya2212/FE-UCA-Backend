package com.feuca.facturacion.config;

import com.feuca.facturacion.entity.Usuario;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> ROLES_VALIDOS = Set.of(
            AccessControlService.SUPERADMIN,
            AccessControlService.ADMINISTRADOR,
            AccessControlService.USUARIO
    );

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtService.parseToken(token);
                UUID userId = UUID.fromString(claims.getSubject());
                usuarioRepository.findById(userId)
                        .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                        .filter(usuario -> usuario.getRol() != null && ROLES_VALIDOS.contains(usuario.getRol()))
                        .ifPresent(usuario -> autenticar(usuario));
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(Usuario usuario) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(usuario.getRol())
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(usuario.getId().toString(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
