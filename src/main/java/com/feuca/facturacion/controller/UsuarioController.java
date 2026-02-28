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
@RequestMapping("/api/v1/facturacion/usuario")

public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
}

    // CREATE
    @PostMapping()
    public ResponseEntity<GeneralResponse> crear(@RequestBody @Valid UsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.create(request);
        return ResponseBuilder.buildResponse("Usuario creado", HttpStatus.CREATED, usuario);
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse> getById(
            @PathVariable UUID id,
            @RequestParam UUID empresaId
    ) {
        UsuarioResponse usuario = usuarioService.getById(empresaId, id);
        return ResponseBuilder.buildResponse("Usuario encontrado.", HttpStatus.OK, usuario);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<GeneralResponse> getByEmail(
            @PathVariable String email,
            @RequestParam UUID empresaId
    ) {
        UsuarioResponse usuario = usuarioService.getByEmail(empresaId, email);
        return ResponseBuilder.buildResponse("Usuario encontrado.", HttpStatus.OK, usuario);
    }

    @GetMapping()
    public ResponseEntity<GeneralResponse> getAllByEmpresa(@RequestParam UUID empresaId) {
        List<UsuarioResponse> usuarios = usuarioService.getAllByEmpresa(empresaId);
        return ResponseBuilder.buildResponse("Usuarios encontrados.", HttpStatus.OK, usuarios);
    }

    // UPDATE
    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse> actualizar(
            @PathVariable UUID id,
            @RequestParam UUID empresaId,
            @RequestBody UsuarioUpdateRequest request
    ) {
        UsuarioResponse usuario = usuarioService.update(empresaId, id, request);
        return ResponseBuilder.buildResponse("Usuario actualizado.", HttpStatus.OK, usuario);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse> eliminar(
            @PathVariable UUID id,
            @RequestParam UUID empresaId
    ) {
        usuarioService.delete(empresaId, id);
        return ResponseBuilder.buildResponse("Usuario eliminado.", HttpStatus.OK, null);
    }
}
