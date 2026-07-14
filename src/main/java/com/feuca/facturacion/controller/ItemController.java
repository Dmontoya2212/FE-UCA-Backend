package com.feuca.facturacion.controller;

import com.feuca.facturacion.dto.request.Item.ItemRequest;
import com.feuca.facturacion.dto.request.Item.ItemUpdateRequest;
import com.feuca.facturacion.dto.response.GeneralResponse;
import com.feuca.facturacion.dto.response.Item.ItemResponse;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.service.ItemService;
import com.feuca.facturacion.util.ResponseBuilder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // CREATE
    @PostMapping("")
    public ResponseEntity<GeneralResponse> create(
            @PathVariable UUID empresaId,
            @RequestBody @Valid ItemRequest request
    ) {
        request.setEmpresaId(empresaId);
        ItemResponse item = itemService.create(request);

        return ResponseBuilder.buildResponse(
                "Item creado.",
                HttpStatus.CREATED,
                item
        );
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse> getById(
            @PathVariable UUID empresaId,
            @PathVariable UUID id
    ) {
        ItemResponse item = itemService.getById(id);

        return ResponseBuilder.buildResponse(
                "Item encontrado.",
                HttpStatus.OK,
                item
        );
    }

    @GetMapping("")
    public ResponseEntity<GeneralResponse> getAllByEmpresa(@PathVariable UUID empresaId) {
        List<ItemResponse> items = itemService.getAllByEmpresaId(empresaId);

        return ResponseBuilder.buildResponse(
                "Items encontrados.",
                HttpStatus.OK,
                items
        );
    }

    @GetMapping("/activos")
    public ResponseEntity<GeneralResponse> getAllActivosByEmpresa(@PathVariable UUID empresaId) {
        List<ItemResponse> items = itemService.getAllActivosByEmpresaId(empresaId);

        return ResponseBuilder.buildResponse(
                "Items activos encontrados.",
                HttpStatus.OK,
                items
        );
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<GeneralResponse> getByEmpresaAndNombre(
            @PathVariable UUID empresaId,
            @PathVariable String nombre
    ) {
        ItemResponse item = itemService.getByEmpresaIdAndNombre(empresaId, nombre);

        return ResponseBuilder.buildResponse(
                "Item encontrado.",
                HttpStatus.OK,
                item
        );
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<GeneralResponse> getAllByEmpresaAndCategoria(
            @PathVariable UUID empresaId,
            @PathVariable ItemCategoria categoria
    ) {
        List<ItemResponse> items = itemService.getAllByEmpresaIdAndCategoria(empresaId, categoria);

        return ResponseBuilder.buildResponse(
                "Items encontrados.",
                HttpStatus.OK,
                items
        );
    }

    @GetMapping("/iva/{ivaId}")
    public ResponseEntity<GeneralResponse> getAllByEmpresaAndIva(
            @PathVariable UUID empresaId,
            @PathVariable UUID ivaId
    ) {
        List<ItemResponse> items = itemService.getAllByEmpresaIdAndIvaId(empresaId, ivaId);

        return ResponseBuilder.buildResponse(
                "Items encontrados.",
                HttpStatus.OK,
                items
        );
    }

    @GetMapping("/buscar")
    public ResponseEntity<GeneralResponse> searchByNombre(
            @PathVariable UUID empresaId,
            @RequestParam(name = "nombre") String nombre
    ) {
        List<ItemResponse> items = itemService.searchByNombre(empresaId, nombre);

        return ResponseBuilder.buildResponse(
                "Items encontrados.",
                HttpStatus.OK,
                items
        );
    }

    // UPDATE
    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse> update(
            @PathVariable UUID empresaId,
            @PathVariable UUID id,
            @RequestBody @Valid ItemUpdateRequest request
    ) {
        ItemResponse item = itemService.update(id, request);

        return ResponseBuilder.buildResponse(
                "Item actualizado.",
                HttpStatus.OK,
                item
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse> deleteById(
            @PathVariable UUID empresaId,
            @PathVariable UUID id
    ) {
        ItemResponse item = itemService.deleteById(id);

        return ResponseBuilder.buildResponse(
                "Item eliminado.",
                HttpStatus.OK,
                item
        );
    }
}
