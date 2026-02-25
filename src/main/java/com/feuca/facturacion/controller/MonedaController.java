package com.feuca.facturacion.controller;

import com.feuca.facturacion.dto.response.GeneralResponse;
import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import com.feuca.facturacion.service.MonedaService;
import com.feuca.facturacion.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/facturacion/moneda")
public class MonedaController {

    private final MonedaService monedaService;

    public MonedaController(MonedaService monedaService) {
        this.monedaService = monedaService;
    }

    //READ
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<GeneralResponse> getByCodigo(@PathVariable String codigo){
        MonedaResponse moneda = monedaService.getByCodigo(codigo);

        return ResponseBuilder.buildResponse(
                "Moneda encontrada.",
                HttpStatus.OK,
                moneda
        );
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<GeneralResponse> getByNombre(@PathVariable String nombre){
        MonedaResponse moneda = monedaService.getByNombre(nombre);

        return ResponseBuilder.buildResponse(
                "Moneda encontrada.",
                HttpStatus.OK,
                moneda
        );
    }

    @GetMapping("/simbolo/{simbolo}")
    public ResponseEntity<GeneralResponse> getBySimbolo(@PathVariable String simbolo){
        MonedaResponse moneda = monedaService.getBySimbolo(simbolo);

        return ResponseBuilder.buildResponse(
                "Moneda encontrada.",
                HttpStatus.OK,
                moneda
        );
    }

    @GetMapping("")
    public ResponseEntity<GeneralResponse> getAll(){
        List<MonedaResponse> monedas = monedaService.getAllMonedas();

        return ResponseBuilder.buildResponse(
                "Monedas encontradas.",
                HttpStatus.OK,
                monedas
        );
    }
}
