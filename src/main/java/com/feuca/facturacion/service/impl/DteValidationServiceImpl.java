package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.repository.EmpresaMonedaRepository;
import com.feuca.facturacion.service.DteValidationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DteValidationServiceImpl implements DteValidationService {

    private static final BigDecimal MONEY_TOLERANCE = new BigDecimal("0.00000001");

    private final EmpresaMonedaRepository empresaMonedaRepository;

    public DteValidationServiceImpl(EmpresaMonedaRepository empresaMonedaRepository) {
        this.empresaMonedaRepository = empresaMonedaRepository;
    }

    @Override
    public void validarPreEmision(
            Empresa empresa,
            Cliente cliente,
            Factura factura,
            List<FacturaLinea> lineas,
            List<Item> items,
            String ambiente
    ) {
        java.util.ArrayList<String> errors = new java.util.ArrayList<>();

        validarEmpresa(empresa, errors);
        validarCliente(cliente, factura, errors);
        validarFactura(empresa, factura, lineas, errors, ambiente);
        validarItems(lineas, items, errors);

        if (!errors.isEmpty()) {
            throw new FacturaValidationException(errors);
        }
    }

    private void validarEmpresa(Empresa empresa, List<String> errors) {
        if (empresa == null) {
            errors.add("empresa: Empresa no encontrada para generar el DTE.");
            return;
        }
        require("empresa.nit", empresa.getNit(), errors);
        require("empresa.registro", empresa.getRegistro(), errors);
        require("empresa.razonSocial", empresa.getRazonSocial(), errors);
        require("empresa.actividadEconomica", empresa.getActividadEconomica(), errors);
        require("empresa.codActividad", empresa.getCodActividad(), errors);
        require("empresa.codEstablecimiento", empresa.getCodEstablecimiento(), errors);
        require("empresa.codPuntoVenta", empresa.getCodPuntoVenta(), errors);
        require("empresa.direccion", empresa.getDireccion(), errors);
        require("empresa.departamento", empresa.getDepartamento(), errors);
        require("empresa.municipio", empresa.getMunicipio(), errors);
        require("empresa.distrito", empresa.getDistrito(), errors);
        require("empresa.telefono", empresa.getTelefono(), errors);
        require("empresa.email", empresa.getEmail(), errors);
        require("empresa.usuario", empresa.getUsuario(), errors);
        require("empresa.passwordHash", empresa.getPasswordHash(), errors);
        require("empresa.clavePrimaria", empresa.getClavePrimaria(), errors);
    }

    private void validarCliente(Cliente cliente, Factura factura, List<String> errors) {
        if (factura == null || factura.getClienteId() == null) {
            return;
        }
        if (cliente == null) {
            errors.add("cliente: Cliente no encontrado para generar el DTE.");
            return;
        }

        String tipoDocumento = firstText(factura.getClienteTipoDocumento(), cliente.getTipoDocumento());
        String nrc = firstText(factura.getClienteNrc(), cliente.getNrc());
        require("cliente.tipoDocumento", tipoDocumento, errors);
        require("cliente.numDocumento", firstText(factura.getClienteNifCif(), cliente.getNifCif()), errors);
        require("cliente.nombre", firstText(factura.getClienteNombreRazonSocial(), cliente.getNombreRazonSocial()), errors);
        require("cliente.direccion", firstText(factura.getClienteDireccion(), cliente.getDireccion()), errors);
        require("cliente.departamento", firstText(factura.getClienteDepartamento(), cliente.getDepartamento()), errors);
        require("cliente.municipio", firstText(factura.getClienteMunicipio(), cliente.getMunicipio()), errors);
        require("cliente.distrito", firstText(factura.getClienteDistrito(), cliente.getDistrito()), errors);

        if ("36".equals(tipoDocumento) || hasText(nrc)) {
            require("cliente.nrc", nrc, errors);
            require("cliente.codActividad", firstText(factura.getClienteCodActividad(), cliente.getCodActividad()), errors);
            require("cliente.descActividad", firstText(factura.getClienteDescActividad(), cliente.getDescActividad()), errors);
        }
    }

    private void validarFactura(Empresa empresa, Factura factura, List<FacturaLinea> lineas, List<String> errors, String ambiente) {
        if (factura == null) {
            errors.add("factura: Factura no encontrada para generar el DTE.");
            return;
        }
        requireAllowed("dte.ambiente", ambiente, List.of("00", "01"), errors);
        requireAllowed("factura.tipoDte", factura.getTipoDte(), List.of("01"), errors);
        require("factura.fechaEmision", factura.getFechaEmision(), errors);
        requireAllowed("factura.monedaCodigo", factura.getMonedaCodigo(), List.of("USD"), errors);
        if (empresa != null && empresa.getId() != null && hasText(factura.getMonedaCodigo())
                && !empresaMonedaRepository.existsByEmpresa_idAndMoneda_codigo(empresa.getId(), factura.getMonedaCodigo())) {
            errors.add("factura.monedaCodigo: La moneda no esta asignada a la empresa.");
        }
        requireAllowed("factura.condicionOperacion", factura.getCondicionOperacion(), List.of(1, 2, 3), errors);
        require("factura.subtotalSinIva", factura.getSubtotalSinIva(), errors);
        require("factura.totalIva", factura.getTotalIva(), errors);
        require("factura.totalConIva", factura.getTotalConIva(), errors);

        if (lineas == null || lineas.isEmpty()) {
            errors.add("factura.lineas: La factura debe tener al menos una linea antes de emitirse.");
            return;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal iva = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (FacturaLinea linea : lineas) {
            subtotal = subtotal.add(valueOrZero(linea.getSubtotalSinIva()));
            iva = iva.add(valueOrZero(linea.getTotalIva()));
            total = total.add(valueOrZero(linea.getTotalConIva()));
        }
        requireSameAmount("factura.subtotalSinIva", factura.getSubtotalSinIva(), subtotal, errors);
        requireSameAmount("factura.totalIva", factura.getTotalIva(), iva, errors);
        requireSameAmount("factura.totalConIva", factura.getTotalConIva(), total, errors);

        if (hasText(factura.getNumeroControl()) && !factura.getNumeroControl().startsWith("DTE-" + factura.getTipoDte() + "-")) {
            errors.add("factura.numeroControl: El numero de control no corresponde al tipo DTE.");
        }
        if (hasText(factura.getNumeroControl()) != hasText(factura.getCodigoGeneracion())) {
            errors.add("factura.correlativos: Numero de control y codigo de generacion deben existir juntos.");
        }
    }

    private void validarItems(List<FacturaLinea> lineas, List<Item> items, List<String> errors) {
        if (lineas == null || lineas.isEmpty()) {
            return;
        }

        Map<UUID, Item> itemsById = new HashMap<>();
        if (items != null) {
            items.forEach(item -> itemsById.put(item.getId(), item));
        }

        for (int i = 0; i < lineas.size(); i++) {
            int lineNumber = i + 1;
            FacturaLinea linea = lineas.get(i);
            Item item = linea.getItemId() == null ? null : itemsById.get(linea.getItemId());

            require("lineas[" + lineNumber + "].descripcion", linea.getDescripcion(), errors);
            require("lineas[" + lineNumber + "].itemId", linea.getItemId(), errors);
            if (linea.getItemId() != null && item == null) {
                errors.add("lineas[" + lineNumber + "].itemId: Item no encontrado para generar el DTE.");
            }
            require("lineas[" + lineNumber + "].codigo", firstText(linea.getItemCodigoInterno(), item != null ? item.getCodigoInterno() : null), errors);
            require("lineas[" + lineNumber + "].unidadMedida", linea.getItemUnidadMedida() != null ? linea.getItemUnidadMedida() : item != null ? item.getUnidadMedida() : null, errors);
            require("lineas[" + lineNumber + "].tipoItem", linea.getItemTipo() != null ? linea.getItemTipo() : item != null ? item.getCategoria() : null, errors);
            requirePositive("lineas[" + lineNumber + "].cantidad", linea.getCantidad(), errors);
            requirePositiveOrZero("lineas[" + lineNumber + "].precioSinIva", linea.getPrecioSinIva(), errors);
            requirePositiveOrZero("lineas[" + lineNumber + "].ivaPorcentaje", linea.getIvaPorcentaje(), errors);
            require("lineas[" + lineNumber + "].subtotalSinIva", linea.getSubtotalSinIva(), errors);
            require("lineas[" + lineNumber + "].totalIva", linea.getTotalIva(), errors);
            require("lineas[" + lineNumber + "].totalConIva", linea.getTotalConIva(), errors);
            if (item != null && Boolean.FALSE.equals(item.getActivo())) {
                errors.add("lineas[" + lineNumber + "].itemId: El item esta inactivo.");
            }
        }
    }

    private void require(String field, Object value, List<String> errors) {
        if (value == null || (value instanceof String text && text.isBlank())) {
            errors.add(field + ": Campo obligatorio para emitir DTE.");
        }
    }

    private void requireAllowed(String field, String value, List<String> allowedValues, List<String> errors) {
        require(field, value, errors);
        if (hasText(value) && !allowedValues.contains(value)) {
            errors.add(field + ": Valor no permitido para DTE: " + value);
        }
    }

    private void requireAllowed(String field, Integer value, List<Integer> allowedValues, List<String> errors) {
        require(field, value, errors);
        if (value != null && !allowedValues.contains(value)) {
            errors.add(field + ": Valor no permitido para DTE: " + value);
        }
    }

    private void requirePositive(String field, BigDecimal value, List<String> errors) {
        require(field, value, errors);
        if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(field + ": Debe ser mayor que cero.");
        }
    }

    private void requirePositiveOrZero(String field, BigDecimal value, List<String> errors) {
        require(field, value, errors);
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            errors.add(field + ": No puede ser negativo.");
        }
    }

    private void requireSameAmount(String field, BigDecimal actual, BigDecimal expected, List<String> errors) {
        if (actual == null) {
            return;
        }
        if (actual.subtract(expected).abs().compareTo(MONEY_TOLERANCE) > 0) {
            errors.add(field + ": No coincide con la suma de lineas. Esperado " + expected + ", recibido " + actual + ".");
        }
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String firstText(String first, String second) {
        if (hasText(first)) {
            return first;
        }
        return hasText(second) ? second : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
