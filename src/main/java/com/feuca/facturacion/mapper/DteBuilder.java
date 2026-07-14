package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.dte.*;
import com.feuca.facturacion.entity.*;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.util.NumeroALetras;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DteBuilder {
    private DteBuilder() {}

    public static DteFacturaElectronica build(
            Factura factura,
            List<FacturaLinea> lineas,
            List<Item> items,
            Empresa empresa,
            Cliente cliente,
            String numeroControl,
            String codigoGeneracion,
            String ambiente
    ) {
        requireAllowed("ambiente DTE", ambiente, List.of("00", "01"));
        requireNotBlank("numero de control DTE", numeroControl);
        requireNotBlank("codigo de generacion DTE", codigoGeneracion);
        requireNotNull("fecha de emision", factura.getFechaEmision());
        requireAllowed("tipo DTE", factura.getTipoDte(), List.of("01"));
        requireAllowed("moneda de la factura", factura.getMonedaCodigo(), List.of("USD"));
        requireNotNull("subtotal de factura", factura.getSubtotalSinIva());
        requireNotNull("total IVA de factura", factura.getTotalIva());
        requireNotNull("total con IVA de factura", factura.getTotalConIva());

        String fecha = factura.getFechaEmision().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String hora = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // 1. Identificacion
        DteIdentificacion identificacion = DteIdentificacion.builder()
                .version(2)
                .ambiente(ambiente)
                .tipoDte(factura.getTipoDte())
                .numeroControl(numeroControl)
                .codigoGeneracion(codigoGeneracion)
                .tipoModelo(1)
                .tipoOperacion(1)
                .tipoContingencia(null)
                .motivoContin(null)
                .fecEmi(fecha)
                .horEmi(hora)
                .tipoMoneda(factura.getMonedaCodigo())
                .build();

        // 2. Emisor
        DteDireccion dirEmisor = DteDireccion.builder()
                .departamento(required("departamento del emisor", coalesce(factura.getEmisorDepartamento(), empresa.getDepartamento())))
                .municipio(required("municipio del emisor", coalesce(factura.getEmisorMunicipio(), empresa.getMunicipio())))
                .distrito(required("distrito del emisor", coalesce(factura.getEmisorDistrito(), empresa.getDistrito())))
                .complemento(required("direccion del emisor", coalesce(factura.getEmisorDireccion(), empresa.getDireccion())))
                .build();

        DteEmisor emisor = DteEmisor.builder()
                .nit(required("NIT del emisor", coalesce(factura.getEmisorNit(), empresa.getNit())))
                .nrc(required("NRC del emisor", coalesce(factura.getEmisorNrc(), empresa.getRegistro())))
                .nombre(required("nombre del emisor", coalesce(factura.getEmisorNombre(), empresa.getRazonSocial())))
                .codActividad(required("codigo de actividad del emisor", coalesce(factura.getEmisorCodActividad(), empresa.getCodActividad())))
                .descActividad(required("actividad economica del emisor", coalesce(factura.getEmisorDescActividad(), empresa.getActividadEconomica())))
                .nombreComercial(coalesce(factura.getEmisorNombreComercial(), empresa.getNombreComercial(), null))
                .direccion(dirEmisor)
                .telefono(required("telefono del emisor", coalesce(factura.getEmisorTelefono(), empresa.getTelefono())))
                .correo(required("correo del emisor", coalesce(factura.getEmisorEmail(), empresa.getEmail())))
                .codEstable(required("codigo de establecimiento del emisor", coalesce(factura.getEmisorCodEstablecimiento(), empresa.getCodEstablecimiento())))
                .codPuntoVenta(required("codigo de punto de venta del emisor", coalesce(factura.getEmisorCodPuntoVenta(), empresa.getCodPuntoVenta())))
                .build();

        // 3. Receptor
        DteReceptor receptor = null;
        if (cliente != null) {
            DteDireccion dirReceptor = DteDireccion.builder()
                    .departamento(required("departamento del receptor", coalesce(factura.getClienteDepartamento(), cliente.getDepartamento())))
                    .municipio(required("municipio del receptor", coalesce(factura.getClienteMunicipio(), cliente.getMunicipio())))
                    .distrito(required("distrito del receptor", coalesce(factura.getClienteDistrito(), cliente.getDistrito())))
                    .complemento(required("direccion del receptor", coalesce(factura.getClienteDireccion(), cliente.getDireccion())))
                    .build();

            receptor = DteReceptor.builder()
                    .tipoDocumento(required("tipo de documento del receptor", coalesce(factura.getClienteTipoDocumento(), cliente.getTipoDocumento())))
                    .numDocumento(required("numero de documento del receptor", coalesce(factura.getClienteNifCif(), cliente.getNifCif())))
                    .nrc(coalesce(factura.getClienteNrc(), cliente.getNrc(), null))
                    .nombre(required("nombre del receptor", coalesce(factura.getClienteNombreRazonSocial(), cliente.getNombreRazonSocial())))
                    .codActividad(required("codigo de actividad del receptor", coalesce(factura.getClienteCodActividad(), cliente.getCodActividad())))
                    .descActividad(required("actividad economica del receptor", coalesce(factura.getClienteDescActividad(), cliente.getDescActividad())))
                    .direccion(dirReceptor)
                    .telefono(coalesce(factura.getClienteTelefono(), cliente.getTelefono(), null))
                    .correo(coalesce(factura.getClienteEmail(), cliente.getEmail(), null))
                    .build();
        }

        // 4. CuerpoDocumento
        List<DteCuerpoDocumento> cuerpo = new ArrayList<>();
        int i = 1;
        for (FacturaLinea linea : lineas) {
            Item it = items.stream().filter(it2 -> it2.getId().equals(linea.getItemId())).findFirst().orElse(null);
            
            Integer tipoItem = linea.getItemTipo();
            String codInt = linea.getItemCodigoInterno();
            Integer uniMedida = linea.getItemUnidadMedida();
            if (it != null) {
                if (linea.getItemTipo() == null) {
                    tipoItem = (it.getCategoria() != null && it.getCategoria() == ItemCategoria.PRODUCTO) ? 1 : 2;
                }
                if (codInt == null) {
                    codInt = it.getCodigoInterno();
                }
                if (linea.getItemUnidadMedida() == null) {
                    uniMedida = it.getUnidadMedida();
                }
            }
            requireNotNull("tipo de item de la linea " + i, tipoItem);
            requireNotNull("unidad de medida de la linea " + i, uniMedida);
            requireNotBlank("descripcion de la linea " + i, linea.getDescripcion());
            requireNotNull("cantidad de la linea " + i, linea.getCantidad());
            requireNotNull("precio sin IVA de la linea " + i, linea.getPrecioSinIva());
            requireNotNull("subtotal sin IVA de la linea " + i, linea.getSubtotalSinIva());
            requireNotNull("total IVA de la linea " + i, linea.getTotalIva());

            DteCuerpoDocumento cd = DteCuerpoDocumento.builder()
                    .numItem(i++)
                    .tipoItem(tipoItem)
                    .numeroDocumento(null)
                    .cantidad(linea.getCantidad())
                    .codigo(codInt)
                    .codTributo("20") // 20 = IVA
                    .uniMedida(uniMedida)
                    .descripcion(linea.getDescripcion())
                    .precioUni(linea.getPrecioSinIva())
                    .montoDescu(BigDecimal.ZERO)
                    .ventaNoSuj(BigDecimal.ZERO)
                    .ventaExenta(BigDecimal.ZERO)
                    .ventaGravada(linea.getSubtotalSinIva())
                    .tributos(List.of("20"))
                    .psv(BigDecimal.ZERO)
                    .noGravado(BigDecimal.ZERO)
                    .ivaItem(linea.getTotalIva())
                    .build();
            cuerpo.add(cd);
        }

        // 5. Resumen
        BigDecimal totalNoSuj = BigDecimal.ZERO;
        BigDecimal totalExenta = BigDecimal.ZERO;
        BigDecimal totalGravada = factura.getSubtotalSinIva();
        BigDecimal subTotalVentas = totalGravada; // totalNoSuj + totalExenta + totalGravada
        
        BigDecimal totalIva = factura.getTotalIva();
        List<DteTributoResumen> tributos = List.of(
                DteTributoResumen.builder()
                        .codigo("20")
                        .descripcion("IVA 13%")
                        .valor(totalIva)
                        .build()
        );

        BigDecimal subTotal = subTotalVentas.add(totalIva);
        BigDecimal ivaRete = BigDecimal.ZERO;
        BigDecimal montoTotalOperacion = subTotal.subtract(ivaRete);
        BigDecimal totalPagar = factura.getTotalConIva(); // should be equal to montoTotalOperacion + totalNoGravado
        
        String totalLetras = NumeroALetras.convertir(totalPagar, factura.getMonedaCodigo());

        List<DtePago> pagos = List.of(
                DtePago.builder()
                        .codigo("01") // 01 = Billetes y monedas
                        .montoPago(totalPagar)
                        .referencia(null)
                        .plazo(null)
                        .periodo(null)
                        .build()
        );

        DteResumen resumen = DteResumen.builder()
                .totalNoSuj(totalNoSuj)
                .totalExenta(totalExenta)
                .totalGravada(totalGravada)
                .subTotalVentas(subTotalVentas)
                .descuNoSuj(BigDecimal.ZERO)
                .descuExenta(BigDecimal.ZERO)
                .descuGravada(BigDecimal.ZERO)
                .porcentajeDescuento(BigDecimal.ZERO)
                .totalDescu(BigDecimal.ZERO)
                .tributos(tributos)
                .subTotal(subTotal)
                .ivaRete(ivaRete)
                .montoTotalOperacion(montoTotalOperacion)
                .totalNoGravado(BigDecimal.ZERO)
                .totalPagar(totalPagar)
                .totalLetras(totalLetras)
                .totalIva(totalIva)
                .saldoFavor(BigDecimal.ZERO)
                .condicionOperacion(factura.getCondicionOperacion() != null ? factura.getCondicionOperacion() : 1) // 1 = Contado
                .pagos(pagos)
                .numPagoElectronico(null)
                .observaciones(null)
                .build();

        return DteFacturaElectronica.builder()
                .identificacion(identificacion)
                .documentoRelacionado(null)
                .emisor(emisor)
                .receptor(receptor)
                .otrosDocumentos(null)
                .ventaTercero(null)
                .cuerpoDocumento(cuerpo)
                .resumen(resumen)
                .apendice(null)
                .build();
    }

    private static String coalesce(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private static String coalesce(String first, String second, String fallback) {
        String value = coalesce(first, second);
        return value != null ? value : fallback;
    }

    private static String required(String field, String value) {
        requireNotBlank(field, value);
        return value;
    }

    private static void requireNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new FacturaValidationException("Falta " + field + " para generar el DTE.");
        }
    }

    private static void requireNotNull(String field, Object value) {
        if (value == null) {
            throw new FacturaValidationException("Falta " + field + " para generar el DTE.");
        }
    }

    private static void requireAllowed(String field, String value, List<String> allowedValues) {
        requireNotBlank(field, value);
        if (!allowedValues.contains(value)) {
            throw new FacturaValidationException("Valor no permitido para " + field + ": " + value);
        }
    }
}
