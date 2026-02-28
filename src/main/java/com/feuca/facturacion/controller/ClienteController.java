package com.feuca.facturacion.controller;

import com.feuca.facturacion.dto.request.Cliente.ClienteRequest;
import com.feuca.facturacion.dto.request.Cliente.ClienteUpdateRequest;
import com.feuca.facturacion.dto.response.Cliente.ClienteResponse;
import com.feuca.facturacion.dto.response.GeneralResponse;
import com.feuca.facturacion.service.ClienteService;
import com.feuca.facturacion.util.ResponseBuilder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facturacion/cliente")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // CREATE
    @PostMapping("")
    public ResponseEntity<GeneralResponse> create(
            @RequestBody @Valid ClienteRequest request
    ) {
        ClienteResponse cliente = clienteService.create(request);

        return ResponseBuilder.buildResponse(
                "Cliente creado.",
                HttpStatus.CREATED,
                cliente
        );
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse> getById(@PathVariable UUID id) {
        ClienteResponse cliente = clienteService.getById(id);

        return ResponseBuilder.buildResponse(
                "Cliente encontrado.",
                HttpStatus.OK,
                cliente
        );
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<GeneralResponse> getAllByEmpresa(@PathVariable UUID empresaId) {
        List<ClienteResponse> clientes = clienteService.getAllByEmpresaId(empresaId);

        return ResponseBuilder.buildResponse(
                "Clientes encontrados.",
                HttpStatus.OK,
                clientes
        );
    }

    @GetMapping("/empresa/{empresaId}/activos")
    public ResponseEntity<GeneralResponse> getAllActivosByEmpresa(@PathVariable UUID empresaId) {
        List<ClienteResponse> clientes = clienteService.getAllActivosByEmpresaId(empresaId);

        return ResponseBuilder.buildResponse(
                "Clientes activos encontrados.",
                HttpStatus.OK,
                clientes
        );
    }

    @GetMapping("/empresa/{empresaId}/nif/{nifCif}")
    public ResponseEntity<GeneralResponse> getByEmpresaAndNifCif(
            @PathVariable UUID empresaId,
            @PathVariable String nifCif
    ) {
        ClienteResponse cliente = clienteService.getByEmpresaIdAndNifCif(empresaId, nifCif);

        return ResponseBuilder.buildResponse(
                "Cliente encontrado.",
                HttpStatus.OK,
                cliente
        );
    }

    @GetMapping("/empresa/{empresaId}/email/{email}")
    public ResponseEntity<GeneralResponse> getByEmpresaAndEmail(
            @PathVariable UUID empresaId,
            @PathVariable String email
    ) {
        ClienteResponse cliente = clienteService.getByEmpresaIdAndEmail(empresaId, email);

        return ResponseBuilder.buildResponse(
                "Cliente encontrado.",
                HttpStatus.OK,
                cliente
        );
    }

    @GetMapping("/empresa/{empresaId}/buscar")
    public ResponseEntity<GeneralResponse> searchByNombre(
            @PathVariable UUID empresaId,
            @RequestParam(name = "nombre") String nombre
    ) {
        List<ClienteResponse> clientes = clienteService.searchByNombre(empresaId, nombre);

        return ResponseBuilder.buildResponse(
                "Clientes encontrados.",
                HttpStatus.OK,
                clientes
        );
    }

    // UPDATE
    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid ClienteUpdateRequest request
    ) {
        ClienteResponse cliente = clienteService.update(id, request);

        return ResponseBuilder.buildResponse(
                "Cliente actualizado.",
                HttpStatus.OK,
                cliente
        );
    }

    // DELETE (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse> deleteById(@PathVariable UUID id) {
        ClienteResponse cliente = clienteService.deleteById(id);

        return ResponseBuilder.buildResponse(
                "Cliente eliminado.",
                HttpStatus.OK,
                cliente
        );
    }
}