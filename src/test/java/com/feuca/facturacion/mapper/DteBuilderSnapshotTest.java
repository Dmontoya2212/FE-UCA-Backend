package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DteBuilderSnapshotTest {

    @Test
    void dteUsesHistoricalSnapshotsInsteadOfCurrentClienteItemAndEmpresaValues() {
        UUID itemId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .id(UUID.randomUUID())
                .fechaEmision(LocalDate.now())
                .monedaCodigo("USD")
                .tipoDte("01")
                .numeroControl("DTE-01-M001P001-000000000000001")
                .codigoGeneracion(UUID.randomUUID().toString().toUpperCase())
                .subtotalSinIva(new BigDecimal("10.00000000"))
                .totalIva(new BigDecimal("1.30000000"))
                .totalConIva(new BigDecimal("11.30000000"))
                .clienteNombreRazonSocial("Cliente Historico")
                .clienteNifCif("06142802901012")
                .clienteNrc("12345678")
                .clienteTipoDocumento("36")
                .clienteCodActividad("620100")
                .clienteDescActividad("Actividad historica")
                .clienteDireccion("Direccion historica")
                .clienteDepartamento("05")
                .clienteMunicipio("10")
                .clienteDistrito("0001")
                .clienteTelefono("22223333")
                .clienteEmail("historico@example.com")
                .emisorNit("06142802901013")
                .emisorNrc("87654321")
                .emisorNombre("Emisor Historico")
                .emisorCodActividad("620100")
                .emisorDescActividad("Servicios historicos")
                .emisorNombreComercial("Comercial Historico")
                .emisorDireccion("Direccion emisor historica")
                .emisorDepartamento("06")
                .emisorMunicipio("14")
                .emisorDistrito("0002")
                .emisorTelefono("22224444")
                .emisorEmail("emisor-historico@example.com")
                .emisorCodEstablecimiento("M999")
                .emisorCodPuntoVenta("P999")
                .build();
        FacturaLinea linea = FacturaLinea.builder()
                .itemId(itemId)
                .descripcion("Linea historica")
                .cantidad(BigDecimal.ONE)
                .precioSinIva(new BigDecimal("10.00000000"))
                .subtotalSinIva(new BigDecimal("10.00000000"))
                .totalIva(new BigDecimal("1.30000000"))
                .totalConIva(new BigDecimal("11.30000000"))
                .itemCodigoInterno("HIST-001")
                .itemUnidadMedida(59)
                .itemTipo(2)
                .build();
        Cliente clienteActual = Cliente.builder()
                .nombreRazonSocial("Cliente Actual Cambiado")
                .nifCif("00000000000000")
                .direccion("Direccion actual")
                .build();
        Item itemActual = Item.builder()
                .id(itemId)
                .codigoInterno("ACT-001")
                .unidadMedida(99)
                .categoria(ItemCategoria.PRODUCTO)
                .build();
        Empresa empresaActual = Empresa.builder()
                .nit("00000000000000")
                .registro("00000000")
                .razonSocial("Empresa Actual Cambiada")
                .direccion("Direccion empresa actual")
                .build();

        DteFacturaElectronica dte = DteBuilder.build(
                factura,
                List.of(linea),
                List.of(itemActual),
                empresaActual,
                clienteActual,
                factura.getNumeroControl(),
                factura.getCodigoGeneracion(),
                "00"
        );

        assertEquals("Cliente Historico", dte.getReceptor().getNombre());
        assertEquals("Direccion historica", dte.getReceptor().getDireccion().getComplemento());
        assertEquals("Emisor Historico", dte.getEmisor().getNombre());
        assertEquals("Direccion emisor historica", dte.getEmisor().getDireccion().getComplemento());
        assertEquals("HIST-001", dte.getCuerpoDocumento().getFirst().getCodigo());
        assertEquals(59, dte.getCuerpoDocumento().getFirst().getUniMedida());
        assertEquals(2, dte.getCuerpoDocumento().getFirst().getTipoItem());
    }

    @Test
    void dteRejectsMissingCurrencyInsteadOfDefaultingUsd() {
        Factura factura = validFactura();
        factura.setMonedaCodigo(null);

        assertThrows(FacturaValidationException.class, () -> DteBuilder.build(
                factura,
                List.of(validLinea()),
                List.of(validItem()),
                validEmpresa(),
                null,
                factura.getNumeroControl(),
                factura.getCodigoGeneracion(),
                "00"
        ));
    }

    @Test
    void dteRejectsMissingEmisorLocationInsteadOfDefaultingCatalogValues() {
        Empresa empresa = validEmpresa();
        empresa.setDepartamento(null);
        Factura factura = validFactura();
        factura.setEmisorDepartamento(null);

        assertThrows(FacturaValidationException.class, () -> DteBuilder.build(
                factura,
                List.of(validLinea()),
                List.of(validItem()),
                empresa,
                null,
                factura.getNumeroControl(),
                factura.getCodigoGeneracion(),
                "00"
        ));
    }

    private Factura validFactura() {
        UUID itemId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        return Factura.builder()
                .id(UUID.randomUUID())
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
                .build();
    }

    private FacturaLinea validLinea() {
        return FacturaLinea.builder()
                .itemId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .descripcion("Linea")
                .cantidad(BigDecimal.ONE)
                .precioSinIva(new BigDecimal("10.00000000"))
                .subtotalSinIva(new BigDecimal("10.00000000"))
                .totalIva(new BigDecimal("1.30000000"))
                .totalConIva(new BigDecimal("11.30000000"))
                .itemCodigoInterno("ITEM-001")
                .itemUnidadMedida(59)
                .itemTipo(2)
                .build();
    }

    private Item validItem() {
        return Item.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .codigoInterno("ITEM-001")
                .unidadMedida(59)
                .categoria(ItemCategoria.SERVICIO)
                .build();
    }

    private Empresa validEmpresa() {
        return Empresa.builder()
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
    }
}
