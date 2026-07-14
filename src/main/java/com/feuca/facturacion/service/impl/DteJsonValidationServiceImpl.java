package com.feuca.facturacion.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.service.DteJsonValidationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class DteJsonValidationServiceImpl implements DteJsonValidationService {

    private static final Pattern NUMERO_CONTROL_PATTERN = Pattern.compile("^DTE-01-[MBSP][0-9]{3}P[0-9]{3}-[0-9]{15}$");
    private static final Pattern CODIGO_GENERACION_PATTERN = Pattern.compile("^[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{12}$");
    private static final Pattern HORA_PATTERN = Pattern.compile("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$");
    private static final BigDecimal MONEY_TOLERANCE = new BigDecimal("0.00000001");
    private static final int MAX_DECIMAL_SCALE = 8;

    private final ObjectMapper objectMapper;

    public DteJsonValidationServiceImpl() {
        this(new ObjectMapper());
    }

    public DteJsonValidationServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
    }

    @Override
    public String validarYSerializar(DteFacturaElectronica dte) {
        String json = serialize(dte);
        JsonNode root = parse(json);
        List<String> errors = new ArrayList<>();

        validateRequiredStructure(root, errors);
        validateIdentificacion(root.path("identificacion"), errors);
        validateCuerpoDocumento(root.path("cuerpoDocumento"), errors);
        validateResumen(root.path("resumen"), root.path("cuerpoDocumento"), errors);
        validateDecimals(root, "$", errors);

        if (!errors.isEmpty()) {
            throw new FacturaValidationException(errors);
        }
        return json;
    }

    private String serialize(DteFacturaElectronica dte) {
        if (dte == null) {
            throw new FacturaValidationException("dte: El DTE es obligatorio para validar JSON.");
        }
        try {
            return objectMapper.writeValueAsString(dte);
        } catch (JsonProcessingException ex) {
            throw new FacturaValidationException("dte: No se pudo serializar el DTE a JSON.");
        }
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new FacturaValidationException("dte: El JSON generado no es valido.");
        }
    }

    private void validateRequiredStructure(JsonNode root, List<String> errors) {
        requireObject(root, "dte", errors);
        requireObject(root.path("identificacion"), "identificacion", errors);
        requireObject(root.path("emisor"), "emisor", errors);
        requireArray(root.path("cuerpoDocumento"), "cuerpoDocumento", errors);
        requireObject(root.path("resumen"), "resumen", errors);
        if (root.path("cuerpoDocumento").isArray() && root.path("cuerpoDocumento").isEmpty()) {
            errors.add("cuerpoDocumento: Debe contener al menos una linea.");
        }
    }

    private void validateIdentificacion(JsonNode identificacion, List<String> errors) {
        requireIntValue(identificacion.path("version"), "identificacion.version", 2, errors);
        requireTextValue(identificacion.path("ambiente"), "identificacion.ambiente", List.of("00", "01"), errors);
        requireTextValue(identificacion.path("tipoDte"), "identificacion.tipoDte", List.of("01"), errors);
        requirePattern(identificacion.path("numeroControl"), "identificacion.numeroControl", NUMERO_CONTROL_PATTERN, errors);
        requirePattern(identificacion.path("codigoGeneracion"), "identificacion.codigoGeneracion", CODIGO_GENERACION_PATTERN, errors);
        requireIntValue(identificacion.path("tipoModelo"), "identificacion.tipoModelo", List.of(1, 2), errors);
        requireIntValue(identificacion.path("tipoOperacion"), "identificacion.tipoOperacion", List.of(1, 2), errors);
        requireDate(identificacion.path("fecEmi"), "identificacion.fecEmi", errors);
        requirePattern(identificacion.path("horEmi"), "identificacion.horEmi", HORA_PATTERN, errors);
        requireTextValue(identificacion.path("tipoMoneda"), "identificacion.tipoMoneda", List.of("USD"), errors);
    }

    private void validateCuerpoDocumento(JsonNode cuerpo, List<String> errors) {
        if (!cuerpo.isArray()) {
            return;
        }
        int expectedNumItem = 1;
        for (JsonNode linea : cuerpo) {
            String prefix = "cuerpoDocumento[" + expectedNumItem + "]";
            requireIntValue(linea.path("numItem"), prefix + ".numItem", expectedNumItem, errors);
            requireIntValue(linea.path("tipoItem"), prefix + ".tipoItem", List.of(1, 2, 3, 4), errors);
            requirePositive(linea.path("cantidad"), prefix + ".cantidad", errors);
            requireText(linea.path("descripcion"), prefix + ".descripcion", errors);
            requireText(linea.path("codigo"), prefix + ".codigo", errors);
            requireTextValue(linea.path("codTributo"), prefix + ".codTributo", List.of("20"), errors);
            requireInt(linea.path("uniMedida"), prefix + ".uniMedida", errors);
            requireNonNegative(linea.path("precioUni"), prefix + ".precioUni", errors);
            requireNonNegative(linea.path("ivaItem"), prefix + ".ivaItem", errors);
            expectedNumItem++;
        }
    }

    private void validateResumen(JsonNode resumen, JsonNode cuerpo, List<String> errors) {
        requireIntValue(resumen.path("condicionOperacion"), "resumen.condicionOperacion", List.of(1, 2, 3), errors);
        requireText(resumen.path("totalLetras"), "resumen.totalLetras", errors);
        requireNonNegative(resumen.path("totalPagar"), "resumen.totalPagar", errors);

        if (!cuerpo.isArray()) {
            return;
        }

        BigDecimal totalGravada = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        for (JsonNode linea : cuerpo) {
            totalGravada = totalGravada.add(decimal(linea.path("ventaGravada")));
            totalIva = totalIva.add(decimal(linea.path("ivaItem")));
        }
        requireSameAmount("resumen.totalGravada", decimal(resumen.path("totalGravada")), totalGravada, errors);
        requireSameAmount("resumen.totalIva", decimal(resumen.path("totalIva")), totalIva, errors);
        requireSameAmount("resumen.totalPagar", decimal(resumen.path("totalPagar")), totalGravada.add(totalIva), errors);
    }

    private void validateDecimals(JsonNode node, String path, List<String> errors) {
        if (node.isNumber()) {
            BigDecimal value = node.decimalValue();
            if (value.scale() > MAX_DECIMAL_SCALE) {
                errors.add(path + ": El numero excede " + MAX_DECIMAL_SCALE + " decimales.");
            }
        }
        if (node.isObject()) {
            Iterator<String> fields = node.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                validateDecimals(node.get(field), path + "." + field, errors);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                validateDecimals(node.get(i), path + "[" + i + "]", errors);
            }
        }
    }

    private void requireObject(JsonNode node, String field, List<String> errors) {
        if (!node.isObject()) {
            errors.add(field + ": Debe ser un objeto JSON.");
        }
    }

    private void requireArray(JsonNode node, String field, List<String> errors) {
        if (!node.isArray()) {
            errors.add(field + ": Debe ser un arreglo JSON.");
        }
    }

    private void requireText(JsonNode node, String field, List<String> errors) {
        if (!node.isTextual() || node.asText().isBlank()) {
            errors.add(field + ": Debe ser texto obligatorio.");
        }
    }

    private void requireTextValue(JsonNode node, String field, List<String> allowed, List<String> errors) {
        requireText(node, field, errors);
        if (node.isTextual() && !allowed.contains(node.asText())) {
            errors.add(field + ": Valor no permitido: " + node.asText());
        }
    }

    private void requirePattern(JsonNode node, String field, Pattern pattern, List<String> errors) {
        requireText(node, field, errors);
        if (node.isTextual() && !pattern.matcher(node.asText()).matches()) {
            errors.add(field + ": Formato invalido.");
        }
    }

    private void requireDate(JsonNode node, String field, List<String> errors) {
        requireText(node, field, errors);
        if (node.isTextual()) {
            try {
                LocalDate.parse(node.asText());
            } catch (RuntimeException ex) {
                errors.add(field + ": Fecha invalida.");
            }
        }
    }

    private void requireInt(JsonNode node, String field, List<String> errors) {
        if (!node.canConvertToInt()) {
            errors.add(field + ": Debe ser entero.");
        }
    }

    private void requireIntValue(JsonNode node, String field, int expected, List<String> errors) {
        requireInt(node, field, errors);
        if (node.canConvertToInt() && node.asInt() != expected) {
            errors.add(field + ": Valor esperado " + expected + ".");
        }
    }

    private void requireIntValue(JsonNode node, String field, List<Integer> allowed, List<String> errors) {
        requireInt(node, field, errors);
        if (node.canConvertToInt() && !allowed.contains(node.asInt())) {
            errors.add(field + ": Valor no permitido: " + node.asInt());
        }
    }

    private void requirePositive(JsonNode node, String field, List<String> errors) {
        requireNumber(node, field, errors);
        if (node.isNumber() && decimal(node).compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(field + ": Debe ser mayor que cero.");
        }
    }

    private void requireNonNegative(JsonNode node, String field, List<String> errors) {
        requireNumber(node, field, errors);
        if (node.isNumber() && decimal(node).compareTo(BigDecimal.ZERO) < 0) {
            errors.add(field + ": No puede ser negativo.");
        }
    }

    private void requireNumber(JsonNode node, String field, List<String> errors) {
        if (!node.isNumber()) {
            errors.add(field + ": Debe ser numerico.");
        }
    }

    private void requireSameAmount(String field, BigDecimal actual, BigDecimal expected, List<String> errors) {
        if (actual.subtract(expected).abs().compareTo(MONEY_TOLERANCE) > 0) {
            errors.add(field + ": No coincide con el detalle. Esperado " + expected + ", recibido " + actual + ".");
        }
    }

    private BigDecimal decimal(JsonNode node) {
        return node.isNumber() ? node.decimalValue() : BigDecimal.ZERO;
    }
}
