package com.feuca.facturacion.mapper;

import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import com.feuca.facturacion.entity.Moneda;

import java.util.List;
import java.util.stream.Collectors;

public class MonedaMapper {

    private MonedaMapper(){}

    public static MonedaResponse toDTO(Moneda moneda){
        return MonedaResponse.builder()
                .codigo(moneda.getCodigo())
                .nombre(moneda.getNombre())
                .simbolo(moneda.getSimbolo())
                .build();
    }

    public static List<MonedaResponse> toDTOList(List<Moneda> monedas){
        return monedas.stream().map(MonedaMapper::toDTO).toList();
    }
}
