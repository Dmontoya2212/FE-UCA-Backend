package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.dte.*;
import com.feuca.facturacion.entity.*;
import com.feuca.facturacion.util.NumeroALetras;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            String codigoGeneracion
    ) {
        String fecha = factura.getFechaEmision().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String hora = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        String tipoDte = factura.getTipoDte() != null ? factura.getTipoDte() : "01";

        // 1. Identificacion
        DteIdentificacion identificacion = DteIdentificacion.builder()
                .version(2)
                .ambiente("00") // TODO: leer de prop
                .tipoDte(tipoDte)
                .numeroControl(numeroControl)
                .codigoGeneracion(codigoGeneracion)
                .tipoModelo(1)
                .tipoOperacion(1)
                .tipoContingencia(null)
                .motivoContin(null)
                .fecEmi(fecha)
                .horEmi(hora)
                .tipoMoneda(factura.getMonedaCodigo() != null ? factura.getMonedaCodigo() : "USD")
                .build();

        // 2. Emisor
        DteDireccion dirEmisor = DteDireccion.builder()
                .departamento(empresa.getDepartamento() != null ? empresa.getDepartamento() : "06")
                .municipio(empresa.getMunicipio() != null ? empresa.getMunicipio() : "14")
                .distrito(empresa.getDistrito() != null ? empresa.getDistrito() : "01")
                .complemento(empresa.getDireccion() != null ? empresa.getDireccion() : "No provisto")
                .build();

        DteEmisor emisor = DteEmisor.builder()
                .nit(empresa.getNit())
                .nrc(empresa.getRegistro())
                .nombre(empresa.getRazonSocial())
                .codActividad(empresa.getCodActividad() != null ? empresa.getCodActividad() : "62010")
                .descActividad(empresa.getActividadEconomica() != null ? empresa.getActividadEconomica() : "Servicios")
                .nombreComercial(empresa.getNombreComercial())
                .direccion(dirEmisor)
                .telefono(empresa.getTelefono() != null ? empresa.getTelefono() : "22222222")
                .correo(empresa.getEmail() != null ? empresa.getEmail() : "no-reply@example.com")
                .codEstable(empresa.getCodEstablecimiento() != null ? empresa.getCodEstablecimiento() : "M001")
                .codPuntoVenta(empresa.getCodPuntoVenta() != null ? empresa.getCodPuntoVenta() : "P001")
                .build();

        // 3. Receptor
        DteReceptor receptor = null;
        if (cliente != null) {
            DteDireccion dirReceptor = DteDireccion.builder()
                    .departamento(cliente.getDepartamento() != null ? cliente.getDepartamento() : "06")
                    .municipio(cliente.getMunicipio() != null ? cliente.getMunicipio() : "14")
                    .distrito(cliente.getDistrito() != null ? cliente.getDistrito() : "01")
                    .complemento(cliente.getDireccion() != null ? cliente.getDireccion() : "No provisto")
                    .build();

            receptor = DteReceptor.builder()
                    .tipoDocumento(cliente.getTipoDocumento() != null ? cliente.getTipoDocumento() : "36") // 36=NIT
                    .numDocumento(cliente.getNifCif())
                    .nrc(cliente.getNrc())
                    .nombre(cliente.getNombreRazonSocial())
                    .codActividad(cliente.getCodActividad() != null ? cliente.getCodActividad() : "10005")
                    .descActividad(cliente.getDescActividad() != null ? cliente.getDescActividad() : "Otros")
                    .direccion(dirReceptor)
                    .telefono(cliente.getTelefono())
                    .correo(cliente.getEmail())
                    .build();
        }

        // 4. CuerpoDocumento
        List<DteCuerpoDocumento> cuerpo = new ArrayList<>();
        int i = 1;
        for (FacturaLinea linea : lineas) {
            Item it = items.stream().filter(it2 -> it2.getId().equals(linea.getItemId())).findFirst().orElse(null);
            
            Integer tipoItem = 2; // Default SERVICIO
            String codInt = null;
            Integer uniMedida = 59;
            if (it != null) {
                tipoItem = (it.getCategoria() != null && it.getCategoria() == ItemCategoria.PRODUCTO) ? 1 : 2;
                codInt = it.getCodigoInterno();
                uniMedida = (it.getUnidadMedida() != null) ? it.getUnidadMedida() : 59;
            }

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
        
        String totalLetras = NumeroALetras.convertir(totalPagar, "USD");

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
}
