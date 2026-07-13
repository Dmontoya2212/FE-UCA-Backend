package com.feuca.facturacion.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumeroALetras {
    private static final String[] UNIDADES = {"", "UN ", "DOS ", "TRES ", "CUATRO ", "CINCO ", "SEIS ", "SIETE ", "OCHO ", "NUEVE "};
    private static final String[] DECENAS = {"DIEZ ", "ONCE ", "DOCE ", "TRECE ", "CATORCE ", "QUINCE ", "DIECISEIS ", "DIECISIETE ", "DIECIOCHO ", "DIECINUEVE ", "VEINTE ", "TREINTA ", "CUARENTA ", "CINCUENTA ", "SESENTA ", "SETENTA ", "OCHENTA ", "NOVENTA "};
    private static final String[] CENTENAS = {"", "CIENTO ", "DOSCIENTOS ", "TRESCIENTOS ", "CUATROCIENTOS ", "QUINIENTOS ", "SEISCIENTOS ", "SETECIENTOS ", "OCHOCIENTOS ", "NOVECIENTOS "};

    public static String convertir(BigDecimal numero, String moneda) {
        String num = numero.setScale(2, RoundingMode.HALF_UP).toString();
        String[] partes = num.split("\\.");
        long entero = Long.parseLong(partes[0]);
        String decimales = partes.length > 1 ? partes[1] : "00";
        if (decimales.length() == 1) decimales += "0";

        if (entero == 0) {
            return "CERO " + decimales + "/100 " + moneda;
        }
        
        String resultado = convertirMillones(entero);
        return resultado.trim() + " " + decimales + "/100 " + moneda;
    }

    private static String convertirMillones(long numero) {
        if (numero >= 1000000) {
            long millones = numero / 1000000;
            long resto = numero % 1000000;
            String strMillones = (millones == 1) ? "UN MILLON " : convertirMiles(millones) + "MILLONES ";
            return strMillones + convertirMiles(resto);
        } else {
            return convertirMiles(numero);
        }
    }

    private static String convertirMiles(long numero) {
        if (numero >= 1000) {
            long miles = numero / 1000;
            long resto = numero % 1000;
            String strMiles = (miles == 1) ? "MIL " : convertirCentenas(miles) + "MIL ";
            return strMiles + convertirCentenas(resto);
        } else {
            return convertirCentenas(numero);
        }
    }

    private static String convertirCentenas(long numero) {
        if (numero == 100) return "CIEN ";
        if (numero > 100) {
            return CENTENAS[(int) (numero / 100)] + convertirDecenas(numero % 100);
        }
        return convertirDecenas(numero);
    }

    private static String convertirDecenas(long numero) {
        if (numero < 10) return UNIDADES[(int) numero];
        if (numero < 20) return DECENAS[(int) (numero - 10)];
        if (numero < 30) return (numero == 20) ? "VEINTE " : "VEINTI" + UNIDADES[(int) (numero - 20)];
        int decena = (int) (numero / 10);
        int unidad = (int) (numero % 10);
        if (unidad == 0) return DECENAS[decena + 8];
        return DECENAS[decena + 8] + "Y " + UNIDADES[unidad];
    }
}
