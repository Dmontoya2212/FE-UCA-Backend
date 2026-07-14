package com.feuca.facturacion.controller;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.response.GeneralResponse;
import com.feuca.facturacion.dto.response.Factura.FacturaEmissionResponse;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;
import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.service.DteService;
import com.feuca.facturacion.service.FacturaService;
import com.feuca.facturacion.util.ResponseBuilder;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/facturas")
public class FacturaController {
    private final FacturaService facturaService;
    private final DteService dteService;

    public FacturaController(FacturaService facturaService, DteService dteService) {
        this.facturaService = facturaService;
        this.dteService = dteService;
    }

    @PostMapping()
    public ResponseEntity<GeneralResponse> crear(
            @PathVariable UUID empresaId,
            @RequestBody @Valid FacturaRequest request
    ) {
        request.setEmpresaId(empresaId);
        FacturaResponse factura = facturaService.create(request);
        return ResponseBuilder.buildResponse("Factura creada.", HttpStatus.CREATED, factura);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse> getById(
            @PathVariable UUID empresaId,
            @PathVariable UUID id
    ) {
        FacturaResponse factura = facturaService.getById(empresaId, id);
        return ResponseBuilder.buildResponse("Factura encontrada.", HttpStatus.OK, factura);
    }

    @GetMapping()
    public ResponseEntity<GeneralResponse> getAll(@PathVariable UUID empresaId) {
        List<FacturaResponse> facturas = facturaService.getAllByEmpresa(empresaId);
        return ResponseBuilder.buildResponse("Facturas encontradas.", HttpStatus.OK, facturas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse> actualizar(
            @PathVariable UUID empresaId,
            @PathVariable UUID id,
            @RequestBody @Valid FacturaUpdateRequest request
    ) {
        FacturaResponse factura = facturaService.update(empresaId, id, request);
        return ResponseBuilder.buildResponse("Factura actualizada.", HttpStatus.OK, factura);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse> eliminar(
            @PathVariable UUID empresaId,
            @PathVariable UUID id
    ) {
        facturaService.delete(empresaId, id);
        return ResponseBuilder.buildResponse("Factura eliminada.", HttpStatus.OK, null);
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<GeneralResponse> enviar(
            @PathVariable UUID empresaId,
            @PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        FacturaEmissionResponse factura = facturaService.enviarAHacienda(empresaId, id, idempotencyKey);
        return ResponseBuilder.buildResponse("Factura enviada y respuesta de Hacienda procesada.", HttpStatus.OK, factura);
    }

    @PostMapping("/{id}/preparar-envio")
    public ResponseEntity<GeneralResponse> prepararEnvio(
            @PathVariable UUID empresaId,
            @PathVariable UUID id
    ) {
        FacturaEmissionResponse factura = facturaService.prepararParaEnvio(empresaId, id);
        return ResponseBuilder.buildResponse("DTE generado y listo para envio.", HttpStatus.OK, factura);
    }

    @GetMapping("/{id}/dte")
    public ResponseEntity<GeneralResponse> getDte(
            @PathVariable UUID empresaId,
            @PathVariable UUID id
    ) {
        DteFacturaElectronica dte = dteService.generarDte(empresaId, id);
        return ResponseBuilder.buildResponse("DTE generado correctamente.", HttpStatus.OK, dte);
    }
}
