package com.feuca.facturacion.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feuca.facturacion.dto.request.Cliente.ClienteUpdateRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.dto.request.Item.ItemUpdateRequest;
import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaUpdateRequest;
import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.entity.IvaTasa;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UpdateMapperOwnershipTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Set<String> FORBIDDEN_BUSINESS_UPDATE_FIELDS = Set.of(
            "id",
            "empresaId",
            "empresaIds",
            "facturaId",
            "propietario",
            "estado",
            "subtotalSinIva",
            "totalIva",
            "totalConIva",
            "codigoGeneracion",
            "numeroControl",
            "selloRecibido",
            "createdAt",
            "password",
            "passwordHash",
            "clavePrimaria",
            "token"
    );

    @Test
    void businessUpdateDtosDoNotDeclareOwnershipCalculatedStateOrCredentialFields() {
        List<Class<?>> updateTypes = List.of(
                ClienteUpdateRequest.class,
                EmpresaUpdateRequest.class,
                FacturaUpdateRequest.class,
                FacturaLineaUpdateRequest.class,
                ItemUpdateRequest.class,
                IvaTasaUpdateRequest.class
        );

        for (Class<?> updateType : updateTypes) {
            for (Field field : updateType.getDeclaredFields()) {
                assertFalse(
                        FORBIDDEN_BUSINESS_UPDATE_FIELDS.contains(field.getName()),
                        updateType.getSimpleName() + " declares forbidden field " + field.getName()
                );
            }
        }
    }

    @Test
    void clienteUpdateDoesNotChangeEmpresaOwner() {
        UUID empresaOriginal = UUID.randomUUID();
        Cliente cliente = Cliente.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaOriginal)
                .nombreRazonSocial("Cliente original")
                .build();

        ClienteMapper.update_entity(cliente, ClienteUpdateRequest.builder()
                .nombreRazonSocial("Cliente actualizado")
                .build());

        assertEquals(empresaOriginal, cliente.getEmpresaId());
    }

    @Test
    void itemUpdateDoesNotChangeEmpresaOwner() {
        UUID empresaOriginal = UUID.randomUUID();
        Item item = Item.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaOriginal)
                .nombre("Item original")
                .build();

        ItemMapper.update_entity(item, ItemUpdateRequest.builder()
                .nombre("Item actualizado")
                .categoria(ItemCategoria.SERVICIO)
                .ivaId(UUID.randomUUID())
                .precioSinIva(BigDecimal.TEN)
                .build());

        assertEquals(empresaOriginal, item.getEmpresaId());
    }

    @Test
    void ivaUpdateDoesNotChangeEmpresaOwner() {
        UUID empresaOriginal = UUID.randomUUID();
        IvaTasa ivaTasa = IvaTasa.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaOriginal)
                .nombre("IVA")
                .porcentaje(BigDecimal.valueOf(13))
                .build();

        IvaTasaMapper.update_entity(ivaTasa, IvaTasaUpdateRequest.builder()
                .nombre("IVA actualizado")
                .porcentaje(BigDecimal.valueOf(10))
                .build());

        assertEquals(empresaOriginal, ivaTasa.getEmpresaId());
    }

    @Test
    void facturaUpdateDoesNotChangeEmpresaOwner() {
        UUID empresaOriginal = UUID.randomUUID();
        Factura factura = Factura.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaOriginal)
                .numero("F-001")
                .fechaEmision(LocalDate.now())
                .estado("BORRADOR")
                .build();

        FacturaMapper.applyUpdate(factura, FacturaUpdateRequest.builder()
                .fechaEmision(LocalDate.now().plusDays(1))
                .monedaCodigo("USD")
                .build());

        assertEquals(empresaOriginal, factura.getEmpresaId());
    }

    @Test
    void facturaLineaUpdateDoesNotChangeFacturaOwner() {
        UUID facturaOriginal = UUID.randomUUID();
        FacturaLinea linea = FacturaLinea.builder()
                .id(UUID.randomUUID())
                .facturaId(facturaOriginal)
                .descripcion("Linea original")
                .cantidad(BigDecimal.ONE)
                .precioSinIva(BigDecimal.TEN)
                .ivaPorcentaje(BigDecimal.valueOf(13))
                .build();

        FacturaLineaMapper.applyUpdate(linea, FacturaLineaUpdateRequest.builder()
                .descripcion("Linea actualizada")
                .cantidad(BigDecimal.valueOf(2))
                .build());

        assertEquals(facturaOriginal, linea.getFacturaId());
    }

    @Test
    void maliciousEmpresaIdInJsonDoesNotChangeResourceOwners() throws Exception {
        UUID empresaOriginal = UUID.randomUUID();
        UUID empresaMaliciosa = UUID.randomUUID();
        UUID facturaOriginal = UUID.randomUUID();
        UUID facturaMaliciosa = UUID.randomUUID();

        Cliente cliente = Cliente.builder().id(UUID.randomUUID()).empresaId(empresaOriginal).build();
        Item item = Item.builder().id(UUID.randomUUID()).empresaId(empresaOriginal).build();
        IvaTasa ivaTasa = IvaTasa.builder().id(UUID.randomUUID()).empresaId(empresaOriginal).build();
        Factura factura = Factura.builder().id(UUID.randomUUID()).empresaId(empresaOriginal).build();
        FacturaLinea linea = FacturaLinea.builder().id(UUID.randomUUID()).facturaId(facturaOriginal).build();

        ClienteUpdateRequest clienteRequest = objectMapper.readValue("""
                {"empresa_id":"%s","nombre_razon_social":"Cliente malicioso"}
                """.formatted(empresaMaliciosa), ClienteUpdateRequest.class);
        ItemUpdateRequest itemRequest = objectMapper.readValue("""
                {"empresa_id":"%s","nombre":"Item malicioso"}
                """.formatted(empresaMaliciosa), ItemUpdateRequest.class);
        IvaTasaUpdateRequest ivaRequest = objectMapper.readValue("""
                {"empresa_id":"%s","nombre":"IVA malicioso"}
                """.formatted(empresaMaliciosa), IvaTasaUpdateRequest.class);
        FacturaUpdateRequest facturaRequest = objectMapper.readValue("""
                {"empresa_id":"%s","monedaCodigo":"USD"}
                """.formatted(empresaMaliciosa), FacturaUpdateRequest.class);
        FacturaLineaUpdateRequest lineaRequest = objectMapper.readValue("""
                {"empresa_id":"%s","factura_id":"%s","descripcion":"Linea maliciosa"}
                """.formatted(empresaMaliciosa, facturaMaliciosa), FacturaLineaUpdateRequest.class);

        ClienteMapper.update_entity(cliente, clienteRequest);
        ItemMapper.update_entity(item, itemRequest);
        IvaTasaMapper.update_entity(ivaTasa, ivaRequest);
        FacturaMapper.applyUpdate(factura, facturaRequest);
        FacturaLineaMapper.applyUpdate(linea, lineaRequest);

        assertEquals(empresaOriginal, cliente.getEmpresaId());
        assertEquals(empresaOriginal, item.getEmpresaId());
        assertEquals(empresaOriginal, ivaTasa.getEmpresaId());
        assertEquals(empresaOriginal, factura.getEmpresaId());
        assertEquals(facturaOriginal, linea.getFacturaId());
    }
}
