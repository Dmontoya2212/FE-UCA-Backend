package com.feuca.facturacion.util;

public final class DataNormalizer {

    private DataNormalizer() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String email(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    public static String identifier(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    public static String displayText(String value) {
        return trimToNull(value);
    }

    public static String phone(String value) {
        return trimToNull(value);
    }
}
