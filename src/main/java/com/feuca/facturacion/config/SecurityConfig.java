package com.feuca.facturacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final String allowedOrigins;
    private final String allowedMethods;
    private final boolean requireHttps;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitingFilter rateLimitingFilter,
            @Value("${app.cors.allowed-origins:}") String allowedOrigins,
            @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}") String allowedMethods,
            @Value("${security.require-https:false}") boolean requireHttps
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.requireHttps = requireHttps;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (requireHttps) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "No autenticado.")))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Login is public
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Health probes may be used by the platform; other actuator endpoints are restricted.
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasAuthority("SUPERADMIN")
                        // OPTIONS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Empresa management: only SUPERADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/facturacion/empresa/**").hasAuthority("SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/facturacion/empresa/**").hasAuthority("SUPERADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/facturacion/empresa/**").hasAuthority("SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/facturacion/empresa/**").hasAuthority("SUPERADMIN")
                        // User management: only SUPERADMIN
                        .requestMatchers("/api/v1/facturacion/usuario").hasAuthority("SUPERADMIN")
                        .requestMatchers("/api/v1/facturacion/usuario/**").hasAuthority("SUPERADMIN")
                        .requestMatchers("/api/v1/empresas/*/usuarios/**").hasAuthority("SUPERADMIN")
                        // IVA rates are administrative configuration
                        .requestMatchers(HttpMethod.POST, "/api/v1/facturacion/iva/**").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/facturacion/iva/**").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/facturacion/iva/**").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/empresas/*/iva/**").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/empresas/*/iva/**").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/empresas/*/iva/**").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        // Sending to Hacienda is restricted by default
                        .requestMatchers(HttpMethod.POST, "/api/v1/facturacion/factura/*/enviar").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/empresas/*/facturas/*/enviar").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        // Delete factura: ADMINISTRADOR or SUPERADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/facturacion/factura/**").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/empresas/*/facturas/**").hasAnyAuthority("SUPERADMIN", "ADMINISTRADOR")
                        // All other API endpoints require authentication
                        .requestMatchers("/api/v1/**").authenticated()
                        // Everything else is closed by default
                        .anyRequest().denyAll()
                )
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(splitCsv(allowedOrigins));
        config.setAllowedMethods(splitCsv(allowedMethods));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .toList();
    }
}
