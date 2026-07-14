package com.feuca.facturacion.controller;

import com.feuca.facturacion.dto.request.Usuario.UsuarioRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.dto.response.GeneralResponse;
import com.feuca.facturacion.dto.response.Usuario.UsuarioResponse;
import com.feuca.facturacion.service.UsuarioService;
import com.feuca.facturacion.util.ResponseBuilder;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")

public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
}

    // CREATE
    @PostMapping("/facturacion/usuario")
    public ResponseEntity<GeneralResponse> crear(@RequestBody @Valid UsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.create(request);
        return ResponseBuilder.buildResponse("Usuario creado", HttpStatus.CREATED, usuario);
    }

    // READ
    @GetMapping("/empresas/{empresaId}/usuarios/{id}")
    public ResponseEntity<GeneralResponse> getById(
            @PathVariable UUID empresaId,
            @PathVariable UUID id
    ) {
        UsuarioResponse usuario = usuarioService.getById(empresaId, id);
        return ResponseBuilder.buildResponse("Usuario encontrado.", HttpStatus.OK, usuario);
    }

    @GetMapping("/empresas/{empresaId}/usuarios/email/{email}")
    public ResponseEntity<GeneralResponse> getByEmail(
            @PathVariable UUID empresaId,
            @PathVariable String email
    ) {
        UsuarioResponse usuario = usuarioService.getByEmail(empresaId, email);
        return ResponseBuilder.buildResponse("Usuario encontrado.", HttpStatus.OK, usuario);
    }

    @GetMapping("/empresas/{empresaId}/usuarios")
    public ResponseEntity<GeneralResponse> getAllByEmpresa(@PathVariable UUID empresaId) {
        List<UsuarioResponse> usuarios = usuarioService.getAllByEmpresa(empresaId);
        return ResponseBuilder.buildResponse("Usuarios encontrados.", HttpStatus.OK, usuarios);
    }

    // UPDATE
    @PatchMapping("/empresas/{empresaId}/usuarios/{id}")
    public ResponseEntity<GeneralResponse> actualizar(
            @PathVariable UUID empresaId,
            @PathVariable UUID id,
            @RequestBody UsuarioUpdateRequest request
    ) {
        UsuarioResponse usuario = usuarioService.update(empresaId, id, request);
        return ResponseBuilder.buildResponse("Usuario actualizado.", HttpStatus.OK, usuario);
    }

    // DELETE
    @DeleteMapping("/empresas/{empresaId}/usuarios/{id}")
    public ResponseEntity<GeneralResponse> eliminar(
            @PathVariable UUID empresaId,
            @PathVariable UUID id
    ) {
        usuarioService.delete(empresaId, id);
        return ResponseBuilder.buildResponse("Usuario eliminado.", HttpStatus.OK, null);
    }
}
