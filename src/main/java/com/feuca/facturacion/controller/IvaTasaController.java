package com.feuca.facturacion.controller;

import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaRequest;
import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaUpdateRequest;
import com.feuca.facturacion.dto.response.GeneralResponse;
import com.feuca.facturacion.dto.response.IvaTasa.IvaTasaResponse;
import com.feuca.facturacion.service.IvaTasaService;
import com.feuca.facturacion.util.ResponseBuilder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facturacion/iva")
public class IvaTasaController {

    private final IvaTasaService ivaTasaService;

    public IvaTasaController(IvaTasaService ivaTasaService) {
        this.ivaTasaService = ivaTasaService;
    }

    // CREATE
    @PostMapping("")
    public ResponseEntity<GeneralResponse> create(
            @RequestBody @Valid IvaTasaRequest request
    ) {
        IvaTasaResponse iva = ivaTasaService.create(request);

        return ResponseBuilder.buildResponse(
                "IVA creado.",
                HttpStatus.CREATED,
                iva
        );
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse> getById(@PathVariable UUID id) {
        IvaTasaResponse iva = ivaTasaService.getById(id);

        return ResponseBuilder.buildResponse(
                "IVA encontrado.",
                HttpStatus.OK,
                iva
        );
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<GeneralResponse> getAllByEmpresa(@PathVariable UUID empresaId) {
        List<IvaTasaResponse> ivas = ivaTasaService.getAllByEmpresaId(empresaId);

        return ResponseBuilder.buildResponse(
                "IVAs encontrados.",
                HttpStatus.OK,
                ivas
        );
    }

    @GetMapping("/empresa/{empresaId}/porcentaje/{porcentaje}")
    public ResponseEntity<GeneralResponse> getByEmpresaAndPorcentaje(
            @PathVariable UUID empresaId,
            @PathVariable BigDecimal porcentaje
    ) {
        IvaTasaResponse iva = ivaTasaService.getByEmpresaIdAndPorcentaje(empresaId, porcentaje);

        return ResponseBuilder.buildResponse(
                "IVA encontrado.",
                HttpStatus.OK,
                iva
        );
    }

    @GetMapping("/empresa/{empresaId}/nombre/{nombre}")
    public ResponseEntity<GeneralResponse> getByEmpresaAndNombre(
            @PathVariable UUID empresaId,
            @PathVariable String nombre
    ) {
        IvaTasaResponse iva = ivaTasaService.getByEmpresaIdAndNombre(empresaId, nombre);

        return ResponseBuilder.buildResponse(
                "IVA encontrado.",
                HttpStatus.OK,
                iva
        );
    }

    // UPDATE
    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid IvaTasaUpdateRequest request
    ) {
        IvaTasaResponse iva = ivaTasaService.update(id, request);

        return ResponseBuilder.buildResponse(
                "IVA actualizado.",
                HttpStatus.OK,
                iva
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse> deleteById(@PathVariable UUID id) {
        IvaTasaResponse iva = ivaTasaService.deleteById(id);

        return ResponseBuilder.buildResponse(
                "IVA eliminado",
                HttpStatus.OK,
                iva
        );
    }
    @DeleteMapping("/empresa/{empresaId}/nombre/{nombre}")
    public ResponseEntity<GeneralResponse> deleteByEmpresaAndNombre(
            @PathVariable UUID empresaId,
            @PathVariable String nombre
    ) {
        IvaTasaResponse iva = ivaTasaService.deleteByEmpresaIdAndNombre(empresaId, nombre);

        return ResponseBuilder.buildResponse(
                "IVA eliminado",
                HttpStatus.OK,
                iva
        );
    }

    @DeleteMapping("/empresa/{empresaId}/porcentaje/{porcentaje}")
    public ResponseEntity<GeneralResponse> deleteByEmpresaAndPorcentaje(
            @PathVariable UUID empresaId,
            @PathVariable BigDecimal porcentaje
    ) {
        IvaTasaResponse iva = ivaTasaService.deleteByEmpresaIdAndPorcentaje(empresaId, porcentaje);

        return ResponseBuilder.buildResponse(
                "IVA eliminado",
                HttpStatus.OK,
                iva
        );
    }
}