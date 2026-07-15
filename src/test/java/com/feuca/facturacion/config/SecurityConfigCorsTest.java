package com.feuca.facturacion.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigCorsTest {

    @Test
    void corsConfigurationAllowsExpectedOriginsMethodsAndHeaders() {
        CorsProperties corsProperties = new CorsProperties(
                "http://localhost:5173,https://feuca.vercel.app"
        );
        SecurityConfig securityConfig = new SecurityConfig(
                null,
                null,
                corsProperties,
                "GET,POST,PUT,PATCH,DELETE,OPTIONS",
                false
        );

        CorsConfiguration configuration = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/v1/auth/login"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:5173", "https://feuca.vercel.app")
                .doesNotContain("*");
        assertThat(configuration.getAllowedMethods()).contains("OPTIONS");
        assertThat(configuration.getAllowedHeaders()).contains("Authorization", "Content-Type");
        assertThat(configuration.getExposedHeaders()).contains("Authorization");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }
}
