package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.response.Moneda.MonedaResponse;
import com.feuca.facturacion.entity.Moneda;
import com.feuca.facturacion.exception.Moneda.MonedaNotFoundException;
import com.feuca.facturacion.mapper.MonedaMapper;
import com.feuca.facturacion.repository.MonedaRepository;
import com.feuca.facturacion.service.MonedaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonedaServiceImpl implements MonedaService {

    private final MonedaRepository monedaRepository;

    public MonedaServiceImpl(
            MonedaRepository monedaRepository
    ) {
        this.monedaRepository = monedaRepository;
    }

    @Override
    public MonedaResponse getByCodigo(String codigo) {

        String codigoNormalizado = codigo.toLowerCase().trim();

        Moneda moneda = monedaRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new MonedaNotFoundException("Moneda no encontrada"));

        return MonedaMapper.toDTO(moneda);
    }

    @Override
    public MonedaResponse getByNombre(String nombre) {

        String nombreNormalizado = nombre.toLowerCase().trim();

        Moneda moneda = monedaRepository.findByNombre(nombreNormalizado)
                .orElseThrow(() -> new MonedaNotFoundException("Moneda no encontrada"));

        return MonedaMapper.toDTO(moneda);
    }

    @Override
    public MonedaResponse getBySimbolo(String simbolo) {

        String simboloNormalizado = simbolo.toLowerCase().trim();
        Moneda moneda = monedaRepository.findBySimbolo(simboloNormalizado)
                .orElseThrow(() -> new MonedaNotFoundException("Moneda no encontrada"));

        return MonedaMapper.toDTO(moneda);
    }

    @Override
    public List<MonedaResponse> getAllMonedas() {
        
        List<Moneda> monedas = monedaRepository.findAll();

        return MonedaMapper.toDTOList(monedas);
    }
}
