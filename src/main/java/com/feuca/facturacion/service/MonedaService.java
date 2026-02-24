package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;

import java.util.List;

public interface MonedaService {
    //READ
    MonedaResponse getByCodigo(String codigo);
    MonedaResponse getByNombre(String nombre);
    MonedaResponse getBySimbolo(String simbolo);
    List<MonedaResponse> getAllMonedas();
}
