package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.repository.EmpresaMonedaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DteValidationServiceImplTest {

    private final EmpresaMonedaRepository empresaMonedaRepository = mock(EmpresaMonedaRepository.class);
    private final DteValidationServiceImpl validationService = new DteValidationServiceImpl(empresaMonedaRepository);

    @Test
    void rejectsPreEmissionWithClearFieldErrorsAndDoesNotStopAtFirstProblem() {
        UUID empresaId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .empresaId(empresaId)
                .clienteId(UUID.randomUUID())
                .monedaCodigo("USD")
                .totalConIva(BigDecimal.TEN)
                .build();

        FacturaValidationException exception = assertThrows(FacturaValidationException.class, () ->
                validationService.validarPreEmision(
                        Empresa.builder().id(empresaId).build(),
                        Cliente.builder().tipoDocumento("36").build(),
                        factura,
                        List.of(),
                        List.of(),
                        ""
                ));

        assertTrue(exception.getErrors().stream().anyMatch(error -> error.startsWith("empresa.nit:")));
        assertTrue(exception.getErrors().stream().anyMatch(error -> error.startsWith("cliente.nrc:")));
        assertTrue(exception.getErrors().stream().anyMatch(error -> error.startsWith("factura.lineas:")));
        assertTrue(exception.getErrors().stream().anyMatch(error -> error.startsWith("dte.ambiente:")));
    }

    @Test
    void acceptsCompletePreEmissionData() {
        UUID empresaId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(empresaMonedaRepository.existsByEmpresa_idAndMoneda_codigo(empresaId, "USD")).thenReturn(true);

        assertDoesNotThrow(() -> validationService.validarPreEmision(
                validEmpresa(empresaId),
                validCliente(empresaId),
                validFactura(empresaId),
                List.of(validLinea(itemId)),
                List.of(validItem(empresaId, itemId)),
                "00"
        ));
    }

    private Empresa validEmpresa(UUID empresaId) {
        return Empresa.builder()
                .id(empresaId)
                .nit("0614-280290-101-3")
                .registro("12345678")
                .razonSocial("Emisor")
                .actividadEconomica("Servicios")
                .codActividad("620100")
                .codEstablecimiento("M001")
                .codPuntoVenta("P001")
                .direccion("Direccion")
                .departamento("06")
                .municipio("14")
                .distrito("0001")
                .telefono("22223333")
                .email("emisor@example.com")
                .usuario("usuario-hacienda")
                .passwordHash("enc:v1:password")
                .clavePrimaria("enc:v1:clave")
                .build();
    }

    private Cliente validCliente(UUID empresaId) {
        return Cliente.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .tipoDocumento("36")
                .nifCif("0614-280290-101-4")
                .nombreRazonSocial("Cliente")
                .nrc("87654321")
                .codActividad("620100")
                .descActividad("Servicios")
                .direccion("Direccion cliente")
                .departamento("06")
                .municipio("14")
                .distrito("0002")
                .build();
    }

    private Factura validFactura(UUID empresaId) {
        return Factura.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .clienteId(UUID.randomUUID())
                .fechaEmision(LocalDate.now())
                .tipoDte("01")
                .monedaCodigo("USD")
                .condicionOperacion(1)
                .subtotalSinIva(new BigDecimal("10.00000000"))
                .totalIva(new BigDecimal("1.30000000"))
                .totalConIva(new BigDecimal("11.30000000"))
                .build();
    }

    private FacturaLinea validLinea(UUID itemId) {
        return FacturaLinea.builder()
                .itemId(itemId)
                .descripcion("Servicio")
                .itemCodigoInterno("SERV-001")
                .itemUnidadMedida(59)
                .itemTipo(2)
                .cantidad(BigDecimal.ONE)
                .precioSinIva(new BigDecimal("10.00000000"))
                .ivaPorcentaje(new BigDecimal("13.00"))
                .subtotalSinIva(new BigDecimal("10.00000000"))
                .totalIva(new BigDecimal("1.30000000"))
                .totalConIva(new BigDecimal("11.30000000"))
                .build();
    }

    private Item validItem(UUID empresaId, UUID itemId) {
        return Item.builder()
                .id(itemId)
                .empresaId(empresaId)
                .codigoInterno("SERV-001")
                .unidadMedida(59)
                .categoria(ItemCategoria.SERVICIO)
                .activo(true)
                .build();
    }
}
