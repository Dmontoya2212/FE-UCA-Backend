package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Factura {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "cliente_id")
    private UUID clienteId;

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "estado", nullable = false)
    @ColumnTransformer(write = "?::invoice_status")
    private String estado;

    @Column(name = "moneda_codigo")
    private String monedaCodigo;

    @Column(name = "subtotal_sin_iva")
    private BigDecimal subtotalSinIva;

    @Column(name = "total_iva")
    private BigDecimal totalIva;

    @Column(name = "total_con_iva")
    private BigDecimal totalConIva;

    @Column(name = "cliente_nombre_razon_social")
    private String clienteNombreRazonSocial;

    @Column(name = "cliente_nif_cif")
    private String clienteNifCif;

    @Column(name = "cliente_direccion")
    private String clienteDireccion;

    @Column(name = "cliente_tipo_documento", length = 2)
    private String clienteTipoDocumento;

    @Column(name = "cliente_nrc", length = 8)
    private String clienteNrc;

    @Column(name = "cliente_cod_actividad", length = 6)
    private String clienteCodActividad;

    @Column(name = "cliente_desc_actividad", length = 150)
    private String clienteDescActividad;

    @Column(name = "cliente_departamento", length = 2)
    private String clienteDepartamento;

    @Column(name = "cliente_municipio", length = 2)
    private String clienteMunicipio;

    @Column(name = "cliente_distrito", length = 4)
    private String clienteDistrito;

    @Column(name = "cliente_telefono")
    private String clienteTelefono;

    @Column(name = "cliente_email")
    private String clienteEmail;

    @Column(name = "emisor_nit")
    private String emisorNit;

    @Column(name = "emisor_nrc")
    private String emisorNrc;

    @Column(name = "emisor_nombre")
    private String emisorNombre;

    @Column(name = "emisor_cod_actividad", length = 6)
    private String emisorCodActividad;

    @Column(name = "emisor_desc_actividad")
    private String emisorDescActividad;

    @Column(name = "emisor_nombre_comercial")
    private String emisorNombreComercial;

    @Column(name = "emisor_direccion")
    private String emisorDireccion;

    @Column(name = "emisor_departamento", length = 2)
    private String emisorDepartamento;

    @Column(name = "emisor_municipio", length = 2)
    private String emisorMunicipio;

    @Column(name = "emisor_distrito", length = 4)
    private String emisorDistrito;

    @Column(name = "emisor_telefono")
    private String emisorTelefono;

    @Column(name = "emisor_email")
    private String emisorEmail;

    @Column(name = "emisor_cod_establecimiento")
    private String emisorCodEstablecimiento;

    @Column(name = "emisor_cod_punto_venta")
    private String emisorCodPuntoVenta;

    @Column(name = "numero_control", length = 31)
    private String numeroControl;

    @Column(name = "codigo_generacion", length = 36)
    private String codigoGeneracion;

    @Column(name = "condicion_operacion")
    private Integer condicionOperacion;

    @Column(name = "sello_recibido")
    private String selloRecibido;

    @Column(name = "fecha_recepcion")
    private OffsetDateTime fechaRecepcion;

    @Column(name = "hacienda_codigo_respuesta")
    private String haciendaCodigoRespuesta;

    @Column(name = "hacienda_mensaje_respuesta", length = 1000)
    private String haciendaMensajeRespuesta;

    @Column(name = "hacienda_errores", length = 2000)
    private String haciendaErrores;

    @Column(name = "hacienda_response_json", columnDefinition = "TEXT")
    private String haciendaResponseJson;

    @Column(name = "tipo_dte", length = 2)
    private String tipoDte;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
