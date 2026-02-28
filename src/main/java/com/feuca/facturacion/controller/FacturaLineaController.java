package com.feuca.facturacion.controller;

import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaRequest;
import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.dto.response.GeneralResponse;
import com.feuca.facturacion.dto.response.FacturaLinea.FacturaLineaResponse;
import com.feuca.facturacion.service.FacturaLineaService;
import com.feuca.facturacion.util.ResponseBuilder;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facturacion/factura-linea")
public class FacturaLineaController {
    private final FacturaLineaService facturaLineaService;

    public FacturaLineaController(FacturaLineaService facturaLineaService) {
        this.facturaLineaService = facturaLineaService;
    }

    @PostMapping()
    public ResponseEntity<GeneralResponse> crear(
            @RequestParam UUID empresaId,
            @RequestBody @Valid FacturaLineaRequest request
    ) {
        FacturaLineaResponse linea = facturaLineaService.create(empresaId, request);
        return ResponseBuilder.buildResponse("Detalle creado.", HttpStatus.CREATED, linea);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse> getById(
            @PathVariable UUID id,
            @RequestParam UUID empresaId,
            @RequestParam UUID facturaId
    ) {
        FacturaLineaResponse linea = facturaLineaService.getById(empresaId, facturaId, id);
        return ResponseBuilder.buildResponse("Detalle encontrado.", HttpStatus.OK, linea);
    }

    @GetMapping()
    public ResponseEntity<GeneralResponse> getAll(
            @RequestParam UUID empresaId,
            @RequestParam UUID facturaId
    ) {
        List<FacturaLineaResponse> lineas = facturaLineaService.getAllByFactura(empresaId, facturaId);
        return ResponseBuilder.buildResponse("Detalle encontrado.", HttpStatus.OK, lineas);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse> actualizar(
            @PathVariable UUID id,
            @RequestParam UUID empresaId,
            @RequestParam UUID facturaId,
            @RequestBody FacturaLineaUpdateRequest request
    ) {
        FacturaLineaResponse linea = facturaLineaService.update(empresaId, facturaId, id, request);
        return ResponseBuilder.buildResponse("Detalle actualizado.", HttpStatus.OK, linea);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse> eliminar(
            @PathVariable UUID id,
            @RequestParam UUID empresaId,
            @RequestParam UUID facturaId
    ) {
        facturaLineaService.delete(empresaId, facturaId, id);
        return ResponseBuilder.buildResponse("Detalle eliminado.", HttpStatus.OK, null);
    }
}
