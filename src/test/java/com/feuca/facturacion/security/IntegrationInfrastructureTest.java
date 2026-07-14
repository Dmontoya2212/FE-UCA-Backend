package com.feuca.facturacion.security;

import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.mapper.DteBuilder;
import com.feuca.facturacion.service.DteJsonValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class IntegrationInfrastructureTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DteJsonValidationService dteJsonValidationService;

    @Test
    void flywaySchemaHistoryExistsAndCriticalTablesAreCreated() {
        assertTableExists("flyway_schema_history");
        assertTableExists("empresas");
        assertTableExists("usuarios");
        assertTableExists("usuario_empresas");
        assertTableExists("facturas");
        assertTableExists("factura_lineas");
        assertTableExists("dte_secuencias");
        assertTableExists("intentos_emision");
    }

    @Test
    void dteSerializationWorksWithSpringContextConfiguration() {
        String codigoGeneracion = UUID.randomUUID().toString().toUpperCase();
        DteFacturaElectronica dte = validDte(codigoGeneracion);

        String json = dteJsonValidationService.validarYSerializar(dte);

        assertTrue(json.contains("\"identificacion\""));
        assertTrue(json.contains("\"codigoGeneracion\":\"" + codigoGeneracion + "\""));
        assertTrue(json.contains("\"numeroControl\":\"DTE-01-M001P001-000000000000001\""));
    }

    private void assertTableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name = ?
                        """,
                Integer.class,
                tableName
        );
        assertEquals(1, count);
    }

    private DteFacturaElectronica validDte(String codigoGeneracion) {
        UUID itemId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .fechaEmision(LocalDate.now())
                .monedaCodigo("USD")
                .tipoDte("01")
                .numeroControl("DTE-01-M001P001-000000000000001")
                .codigoGeneracion(codigoGeneracion)
                .subtotalSinIva(new BigDecimal("10.00000000"))
                .totalIva(new BigDecimal("1.30000000"))
                .totalConIva(new BigDecimal("11.30000000"))
                .emisorNit("06142802901013")
                .emisorNrc("87654321")
                .emisorNombre("Emisor")
                .emisorCodActividad("620100")
                .emisorDescActividad("Servicios")
                .emisorDireccion("Direccion emisor")
                .emisorDepartamento("06")
                .emisorMunicipio("14")
                .emisorDistrito("0002")
                .emisorTelefono("22224444")
                .emisorEmail("emisor@example.com")
                .emisorCodEstablecimiento("M001")
                .emisorCodPuntoVenta("P001")
                .condicionOperacion(1)
                .build();
        FacturaLinea linea = FacturaLinea.builder()
                .itemId(itemId)
                .descripcion("Servicio")
                .cantidad(BigDecimal.ONE)
                .precioSinIva(new BigDecimal("10.00000000"))
                .subtotalSinIva(new BigDecimal("10.00000000"))
                .totalIva(new BigDecimal("1.30000000"))
                .totalConIva(new BigDecimal("11.30000000"))
                .itemCodigoInterno("SERV-001")
                .itemUnidadMedida(59)
                .itemTipo(2)
                .build();
        Item item = Item.builder()
                .id(itemId)
                .codigoInterno("SERV-001")
                .unidadMedida(59)
                .categoria(ItemCategoria.SERVICIO)
                .build();
        Empresa empresa = Empresa.builder()
                .nit("06142802901013")
                .registro("87654321")
                .razonSocial("Emisor")
                .codActividad("620100")
                .actividadEconomica("Servicios")
                .direccion("Direccion emisor")
                .departamento("06")
                .municipio("14")
                .distrito("0002")
                .telefono("22224444")
                .email("emisor@example.com")
                .codEstablecimiento("M001")
                .codPuntoVenta("P001")
                .build();

        return DteBuilder.build(
                factura,
                List.of(linea),
                List.of(item),
                empresa,
                null,
                factura.getNumeroControl(),
                factura.getCodigoGeneracion(),
                "00"
        );
    }
}
