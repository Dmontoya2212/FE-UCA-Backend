package com.feuca.facturacion.security;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.expiration-ms=900000",
        "jwt.issuer=feuca-test"
})
@ActiveProfiles("test")
class FreshDatabaseMigrationIntegrationTest {

    private static final List<String> BUSINESS_TABLES = List.of(
            "empresas", "monedas", "empresa_monedas", "clientes", "iva_tasas", "items",
            "facturas", "factura_lineas", "dte_secuencias", "usuario_empresas",
            "intentos_emision", "audit_logs"
    );

    @Autowired
    private DataSource dataSource;

    @Test
    void freshMigrationCreatesOnlyInitialSuperadmin() {
        String schema = "fresh_" + UUID.randomUUID().toString().replace("-", "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE SCHEMA " + schema);

        try {
            Flyway.configure()
                    .dataSource(dataSource)
                    .defaultSchema(schema)
                    .schemas(schema)
                    .locations("classpath:db/migration")
                    .cleanDisabled(true)
                    .load()
                    .migrate();

            for (String table : BUSINESS_TABLES) {
                Long count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + schema + "." + table,
                        Long.class
                );
                assertEquals(0L, count, () -> table + " debe iniciar vacía");
            }

            Long userCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + schema + ".usuarios",
                    Long.class
            );
            assertEquals(1L, userCount);

            var superadmin = jdbcTemplate.queryForMap(
                    "SELECT email, password_hash, es_admin, rol, activo FROM " + schema + ".usuarios"
            );
            assertEquals("superadmin@facturacion.local", superadmin.get("email"));
            assertEquals(Boolean.TRUE, superadmin.get("es_admin"));
            assertEquals("SUPERADMIN", superadmin.get("rol"));
            assertEquals(Boolean.TRUE, superadmin.get("activo"));
            assertTrue(
                    ((String) superadmin.get("password_hash"))
                            .matches("\\$2[aby]\\$12\\$[./A-Za-z0-9]{53}"),
                    "La contraseña inicial debe persistirse únicamente como BCrypt con coste 12"
            );
        } finally {
            jdbcTemplate.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }
}
