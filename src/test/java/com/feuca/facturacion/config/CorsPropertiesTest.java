package com.feuca.facturacion.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorsPropertiesTest {

    @Test
    void parsesCommaSeparatedOriginsAndTrimsSpaces() {
        CorsProperties properties = new CorsProperties(
                "http://localhost:5173, https://feuca.vercel.app, "
        );

        assertThat(properties.getAllowedOrigins())
                .containsExactly("http://localhost:5173", "https://feuca.vercel.app");
    }
}
