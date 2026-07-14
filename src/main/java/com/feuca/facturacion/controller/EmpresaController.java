package com.feuca.facturacion.controller;

import com.feuca.facturacion.dto.request.Empresa.EmpresaRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaIntegrationUpdateRequest;
import com.feuca.facturacion.dto.request.Empresa.EmpresaUpdateRequest;
import com.feuca.facturacion.dto.request.Moneda.AddMonedaRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.dto.response.GeneralResponse;
import com.feuca.facturacion.service.EmpresaService;
import com.feuca.facturacion.util.ResponseBuilder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facturacion/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    //CREATE
    @PostMapping()
    public ResponseEntity<GeneralResponse> crear(
            @RequestBody @Valid EmpresaRequest empresaRequest
    ) {
        EmpresaResponse empresa = empresaService.create(empresaRequest);

        return ResponseBuilder.buildResponse(
                "Empresa creada",
                HttpStatus.CREATED,
                empresa
        );
    }

    //READ
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse> getById(@PathVariable UUID id) {
        EmpresaResponse empresa = empresaService.getById(id);

        return ResponseBuilder.buildResponse(
                "Empresa encontrada.",
                HttpStatus.OK,
                empresa
        );
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<GeneralResponse> getByNombre(@PathVariable String nombre) {
        EmpresaResponse empresa = empresaService.getByNombreLegal(nombre);

        return ResponseBuilder.buildResponse(
                "Empresa encontrada",
                HttpStatus.OK,
                empresa
        );
    }

    @GetMapping("/nit/{nit}")
    public ResponseEntity<GeneralResponse> getByNit(@PathVariable String nit) {
        EmpresaResponse empresa = empresaService.getByNit(nit);

        return ResponseBuilder.buildResponse(
                "Empresa encontrada.",
                HttpStatus.OK,
                empresa
        );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<GeneralResponse> getByEmail(@PathVariable String email) {
        EmpresaResponse empresa = empresaService.getByEmail(email);

        return ResponseBuilder.buildResponse(
                "Empresa encontrada.",
                HttpStatus.OK,
                empresa
        );
    }

    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<GeneralResponse> getByTelefono(@PathVariable String telefono) {
        EmpresaResponse empresa = empresaService.getByTelefono(telefono);

        return ResponseBuilder.buildResponse(
                "Empresa encontrada.",
                HttpStatus.OK,
                empresa
        );
    }

    @GetMapping("/sucursal")
    public ResponseEntity<GeneralResponse> getByNombreAndDireccion(
            @RequestParam(name = "nombre") String nombreComercial,
            @RequestParam String direccion
    ) {
        EmpresaResponse empresa = empresaService.getByNombreComercialAndDireccion(
                nombreComercial,
                direccion
        );

        return ResponseBuilder.buildResponse(
                "Empresa encontrada.",
                HttpStatus.OK,
                empresa
        );
    }

    @GetMapping("/sucursales")
    public ResponseEntity<GeneralResponse> getAllByNombreComercial(
            @RequestParam(name = "nombre") String nombreComercial
    ){
        List<EmpresaResponse> empresas = empresaService.getAllByNombreComercial(nombreComercial);

        return ResponseBuilder.buildResponse(
                "Empresas encontradas.",
                HttpStatus.OK,
                empresas
        );
    }

    @GetMapping("/localizacion")
    public ResponseEntity<GeneralResponse> getAllByCiudad(
            @RequestParam(name = "ciudad") String nombreCiudad
    ){
        List<EmpresaResponse> empresas = empresaService.getAllByCiudad(nombreCiudad);

        return ResponseBuilder.buildResponse(
                "Empresas encontradas.",
                HttpStatus.OK,
                empresas
        );
    }

    @GetMapping("/codigo")
    public ResponseEntity<GeneralResponse> getAllByCodigoPostal(
            @RequestParam(name = "codigo") String codigoPostal
    ) {
        List<EmpresaResponse> empresas = empresaService.getAllByCodigoPostal(codigoPostal);

        return ResponseBuilder.buildResponse(
                "Empresas encontradas.",
                HttpStatus.OK,
                empresas
        );
    }

    @GetMapping("/ubicacion")
    public ResponseEntity<GeneralResponse> getAllByPais(
            @RequestParam(name = "pais") String nombrePais
    ) {
        List<EmpresaResponse> empresas = empresaService.getAllByPais(nombrePais);

        return ResponseBuilder.buildResponse(
                "Empresas encontradas.",
                HttpStatus.OK,
                empresas
        );
    }

    @GetMapping("")
    public ResponseEntity<GeneralResponse> getAll() {
        List<EmpresaResponse> empresas = empresaService.getAll();

        return ResponseBuilder.buildResponse(
                "Empresas encontradas.",
                HttpStatus.OK,
                empresas
        );
    }

    //UPDATE
    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid EmpresaUpdateRequest empresaRequest
    ) {
        EmpresaResponse empresa = empresaService.update(id, empresaRequest);

        return ResponseBuilder.buildResponse(
                "Empresa actualizada.",
                HttpStatus.OK,
                empresa
        );
    }

    @PatchMapping("/{id}/monedas")
    public ResponseEntity<GeneralResponse> updateMonedas(
            @PathVariable UUID id,
            @RequestBody @Valid AddMonedaRequest addMonedaRequest
    ) {
        EmpresaResponse empresa = empresaService.updateMonedas(id, addMonedaRequest);

        return ResponseBuilder.buildResponse(
                "Monedas actualizadas.",
                HttpStatus.OK,
                empresa
        );
    }

    @PatchMapping("/{id}/integracion")
    public ResponseEntity<GeneralResponse> updateIntegracion(
            @PathVariable UUID id,
            @RequestBody @Valid EmpresaIntegrationUpdateRequest integrationUpdateRequest
    ) {
        EmpresaResponse empresa = empresaService.updateIntegration(id, integrationUpdateRequest);

        return ResponseBuilder.buildResponse(
                "Configuracion de integracion actualizada.",
                HttpStatus.OK,
                empresa
        );
    }

    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse> deleteById(
            @PathVariable UUID id
    ) {
        EmpresaResponse empresa = empresaService.deleteById(id);

        return ResponseBuilder.buildResponse(
                "Empresa eliminada.",
                HttpStatus.OK,
                empresa
        );
    }

    @DeleteMapping("/nombre/{nombre}")
    public ResponseEntity<GeneralResponse> deleteByNombre(
            @PathVariable String nombre
    ) {
        EmpresaResponse empresa = empresaService.deleteByNombreLegal(nombre);

        return ResponseBuilder.buildResponse(
                "Empresa eliminada.",
                HttpStatus.OK,
                empresa
        );
    }

    @DeleteMapping("/nit/{nit}")
    public ResponseEntity<GeneralResponse> deleteByNit(
            @PathVariable String nit
    ) {
        EmpresaResponse empresa = empresaService.deleteByNit(nit);

        return ResponseBuilder.buildResponse(
                "Empresa eliminada.",
                HttpStatus.OK,
                empresa
        );
    }

    @DeleteMapping("/email/{email}")
    public ResponseEntity<GeneralResponse> deleteByEmail(
            @PathVariable String email
    ) {
        EmpresaResponse empresa = empresaService.deleteByEmail(email);

        return ResponseBuilder.buildResponse(
                "Empresa eliminada.",
                HttpStatus.OK,
                empresa
        );
    }

    @DeleteMapping("/telefono/{telefono}")
    public ResponseEntity<GeneralResponse> deleteByTelefono(
            @PathVariable String telefono
    ) {
        EmpresaResponse empresa = empresaService.deleteByTelefono(telefono);

        return ResponseBuilder.buildResponse(
                "Empresa eliminada.",
                HttpStatus.OK,
                empresa
        );
    }
}
