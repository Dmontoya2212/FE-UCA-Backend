package com.feuca.facturacion.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.expiration-ms=900000",
        "jwt.issuer=feuca-test"
})
@ActiveProfiles("test")
class DataIntegrityConstraintsIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void databaseRejectsDuplicateClienteNifWithinSameEmpresa() {
        UUID empresaId = jdbcTemplate.queryForObject("SELECT id FROM empresas LIMIT 1", UUID.class);
        String nif = shortValue("DUP");

        insertCliente(empresaId, "Cliente A", nif, "cliente-a-" + UUID.randomUUID() + "@example.com");

        assertThrows(DataIntegrityViolationException.class, () ->
                insertCliente(empresaId, "Cliente B", nif, "cliente-b-" + UUID.randomUUID() + "@example.com")
        );
    }

    @Test
    @Transactional
    void databaseRejectsDuplicateClienteEmailWithinSameEmpresa() {
        UUID empresaId = jdbcTemplate.queryForObject("SELECT id FROM empresas LIMIT 1", UUID.class);
        String email = "cliente-" + UUID.randomUUID() + "@example.com";

        insertCliente(empresaId, "Cliente A", shortValue("A"), email);

        assertThrows(DataIntegrityViolationException.class, () ->
                insertCliente(empresaId, "Cliente B", shortValue("B"), email.toUpperCase())
        );
    }

    @Test
    @Transactional
    void databaseRejectsDuplicateUsuarioEmpresaRelation() {
        UUID empresaId = jdbcTemplate.queryForObject("SELECT id FROM empresas LIMIT 1", UUID.class);
        UUID usuarioId = UUID.randomUUID();

        jdbcTemplate.update("""
                        INSERT INTO usuarios (id, nombre, email, password_hash, es_admin, rol, activo, created_at, updated_at)
                        VALUES (?, ?, ?, ?, false, 'USUARIO', true, NOW(), NOW())
                        """,
                usuarioId,
                "Usuario Duplicado",
                "usuario-" + usuarioId + "@example.com",
                "hash"
        );
        jdbcTemplate.update("INSERT INTO usuario_empresas (usuario_id, empresa_id) VALUES (?, ?)", usuarioId, empresaId);

        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update("INSERT INTO usuario_empresas (usuario_id, empresa_id) VALUES (?, ?)", usuarioId, empresaId)
        );
    }

    private void insertCliente(UUID empresaId, String nombre, String nif, String email) {
        jdbcTemplate.update("""
                        INSERT INTO clientes (
                            id, empresa_id, nombre_razon_social, nif_cif, email, activo, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, true, NOW(), NOW())
                        """,
                UUID.randomUUID(),
                empresaId,
                nombre,
                nif,
                email
        );
    }

    private String shortValue(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 20);
    }
}
