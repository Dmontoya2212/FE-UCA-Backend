package com.feuca.facturacion.controller;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.response.GeneralResponse;
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
@RequestMapping("/api/v1/facturacion/factura")
public class FacturaController {
    private final FacturaService facturaService;
    private final DteService dteService;

    public FacturaController(FacturaService facturaService, DteService dteService) {
        this.facturaService = facturaService;
        this.dteService = dteService;
    }

    @PostMapping()
    public ResponseEntity<GeneralResponse> crear(@RequestBody @Valid FacturaRequest request) {
        FacturaResponse factura = facturaService.create(request);
        return ResponseBuilder.buildResponse("Factura creada.", HttpStatus.CREATED, factura);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse> getById(@PathVariable UUID id, @RequestParam UUID empresaId) {
        FacturaResponse factura = facturaService.getById(empresaId, id);
        return ResponseBuilder.buildResponse("Factura encontrada.", HttpStatus.OK, factura);
    }

    @GetMapping()
    public ResponseEntity<GeneralResponse> getAll(@RequestParam UUID empresaId) {
        List<FacturaResponse> facturas = facturaService.getAllByEmpresa(empresaId);
        return ResponseBuilder.buildResponse("Facturas encontradas.", HttpStatus.OK, facturas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse> actualizar(
            @PathVariable UUID id,
            @RequestParam UUID empresaId,
            @RequestBody FacturaUpdateRequest request
    ) {
        FacturaResponse factura = facturaService.update(empresaId, id, request);
        return ResponseBuilder.buildResponse("Factura actualizada.", HttpStatus.OK, factura);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse> eliminar(@PathVariable UUID id, @RequestParam UUID empresaId) {
        facturaService.delete(empresaId, id);
        return ResponseBuilder.buildResponse("Factura eliminada.", HttpStatus.OK, null);
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<GeneralResponse> enviar(@PathVariable UUID id, @RequestParam UUID empresaId) {
        FacturaResponse factura = facturaService.enviarAHacienda(empresaId, id);
        return ResponseBuilder.buildResponse("Factura enviada a Hacienda.", HttpStatus.OK, factura);
    }

    @GetMapping("/{id}/dte")
    public ResponseEntity<GeneralResponse> getDte(@PathVariable UUID id, @RequestParam UUID empresaId) {
        DteFacturaElectronica dte = dteService.generarDte(empresaId, id);
        return ResponseBuilder.buildResponse("DTE generado correctamente.", HttpStatus.OK, dte);
    }
}
