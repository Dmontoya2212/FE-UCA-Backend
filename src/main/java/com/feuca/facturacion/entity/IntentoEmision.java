package com.feuca.facturacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "intentos_emision")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentoEmision {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "factura_id", columnDefinition = "uuid", nullable = false)
    private UUID facturaId;

    @Column(name = "empresa_id", columnDefinition = "uuid", nullable = false)
    private UUID empresaId;

    @Column(name = "codigo_generacion", length = 36)
    private String codigoGeneracion;

    @Column(name = "numero_control", length = 31)
    private String numeroControl;

    @Column(name = "ambiente", length = 2)
    private String ambiente;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "estado_intento", nullable = false)
    private String estadoIntento;

    @Column(name = "codigo_http")
    private Integer codigoHttp;

    @Column(name = "codigo_hacienda")
    private String codigoHacienda;

    @Column(name = "descripcion_respuesta", length = 1000)
    private String descripcionRespuesta;

    @Column(name = "sello_recibido")
    private String selloRecibido;

    @Column(name = "request_json", columnDefinition = "TEXT")
    private String requestJson;

    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;

    @Column(name = "mensaje", length = 1000)
    private String mensaje;

    @Column(name = "numero_intento", nullable = false)
    private Integer numeroIntento;

    @Column(name = "error_tecnico", length = 2000)
    private String errorTecnico;

    @Column(name = "fecha_intento")
    private OffsetDateTime fechaIntento;

    @Column(name = "fecha_respuesta")
    private OffsetDateTime fechaRespuesta;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
