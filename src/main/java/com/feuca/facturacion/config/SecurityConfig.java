package com.feuca.facturacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Login is public
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // OPTIONS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Empresa management: only SUPERADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/facturacion/empresa/**").hasAuthority("SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/facturacion/empresa/**").hasAuthority("SUPERADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/facturacion/empresa/**").hasAuthority("SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/facturacion/empresa/**").hasAuthority("SUPERADMIN")
                        // User management: only SUPERADMIN
                        .requestMatchers("/api/v1/facturacion/usuario/**").hasAuthority("SUPERADMIN")
                        // Delete factura: ADMINISTRADOR or SUPERADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/facturacion/factura/**").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        // All other API endpoints require authentication
                        .requestMatchers("/api/v1/**").authenticated()
                        // Everything else
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
