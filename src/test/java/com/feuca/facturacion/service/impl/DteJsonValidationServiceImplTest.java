package com.feuca.facturacion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.mapper.DteBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DteJsonValidationServiceImplTest {

    private final DteJsonValidationServiceImpl validationService = new DteJsonValidationServiceImpl(new ObjectMapper());

    @Test
    void serializesAndValidatesValidDteJson() {
        String json = validationService.validarYSerializar(validDte());

        assertTrue(json.contains("\"identificacion\""));
        assertTrue(json.contains("\"numeroControl\":\"DTE-01-M001P001-000000000000001\""));
    }

    @Test
    void rejectsInvalidGeneratedJsonFormats() {
        DteFacturaElectronica dte = validDte();
        dte.getIdentificacion().setCodigoGeneracion("codigo-minusculo");
        dte.getIdentificacion().setFecEmi("2026-99-99");

        FacturaValidationException exception = assertThrows(FacturaValidationException.class,
                () -> validationService.validarYSerializar(dte));

        assertTrue(exception.getErrors().stream().anyMatch(error -> error.startsWith("identificacion.codigoGeneracion:")));
        assertTrue(exception.getErrors().stream().anyMatch(error -> error.startsWith("identificacion.fecEmi:")));
    }

    @Test
    void rejectsDecimalPrecisionGreaterThanAllowed() {
        DteFacturaElectronica dte = validDte();
        dte.getCuerpoDocumento().getFirst().setPrecioUni(new BigDecimal("10.123456789"));

        FacturaValidationException exception = assertThrows(FacturaValidationException.class,
                () -> validationService.validarYSerializar(dte));

        assertTrue(exception.getErrors().stream().anyMatch(error -> error.contains("excede 8 decimales")));
    }

    private DteFacturaElectronica validDte() {
        UUID itemId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .fechaEmision(LocalDate.now())
                .monedaCodigo("USD")
                .tipoDte("01")
                .numeroControl("DTE-01-M001P001-000000000000001")
                .codigoGeneracion(UUID.randomUUID().toString().toUpperCase())
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
