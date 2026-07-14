package com.feuca.facturacion.dto.response;

import com.feuca.facturacion.dto.response.Cliente.ClienteResponse;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;
import com.feuca.facturacion.dto.response.FacturaLinea.FacturaLineaResponse;
import com.feuca.facturacion.dto.response.Item.ItemResponse;
import com.feuca.facturacion.dto.response.IvaTasa.IvaTasaResponse;
import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import com.feuca.facturacion.dto.response.Usuario.UsuarioResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PublicResponseDtoSecurityTest {

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "passwordHash",
            "clavePrimaria",
            "token",
            "secret",
            "secreto",
            "esAdmin"
    );

    @Test
    void normalResponseDtosDoNotExposeSecretsOrLegacySecurityFields() {
        List<Class<?>> responseTypes = List.of(
                ClienteResponse.class,
                EmpresaResponse.class,
                FacturaResponse.class,
                FacturaLineaResponse.class,
                ItemResponse.class,
                IvaTasaResponse.class,
                MonedaResponse.class,
                UsuarioResponse.class
        );

        for (Class<?> responseType : responseTypes) {
            for (Field field : responseType.getDeclaredFields()) {
                assertFalse(
                        SENSITIVE_FIELDS.contains(field.getName()),
                        responseType.getSimpleName() + " exposes sensitive field " + field.getName()
                );
            }
        }
    }
}
