package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.dto.response.FacturaLinea.FacturaLineaResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.exception.Factura.FacturaNoEditableException;
import com.feuca.facturacion.exception.Factura.FacturaNotFoundException;
import com.feuca.facturacion.exception.FacturaLinea.FacturaLineaNotFoundException;
import com.feuca.facturacion.mapper.FacturaLineaMapper;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.service.FacturaLineaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FacturaLineaServiceImpl implements FacturaLineaService {

    private final FacturaRepository facturaRepository;
    private final FacturaLineaRepository facturaLineaRepository;

    public FacturaLineaServiceImpl(FacturaRepository facturaRepository,
                                   FacturaLineaRepository facturaLineaRepository) {
        this.facturaRepository = facturaRepository;
        this.facturaLineaRepository = facturaLineaRepository;
    }

    private Factura getFacturaOrThrow(UUID empresaId, UUID facturaId) {
        return facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));
    }

    private void validarEditable(Factura f) {
        if (!"BORRADOR".equalsIgnoreCase(f.getEstado())) {
            throw new FacturaNoEditableException("La factura ya fue enviada y no se puede actualizar.");
        }
    }

    @Override
    public FacturaLineaResponse getById(UUID empresaId, UUID facturaId, UUID lineaId) {
        getFacturaOrThrow(empresaId, facturaId);

        FacturaLinea linea = facturaLineaRepository.findByIdAndFacturaId(lineaId, facturaId)
                .orElseThrow(() -> new FacturaLineaNotFoundException("Detalle no encontrado."));

        return FacturaLineaMapper.toResponse(linea);
    }

    @Override
    public List<FacturaLineaResponse> getAllByFactura(UUID empresaId, UUID facturaId) {
        getFacturaOrThrow(empresaId, facturaId);

        return facturaLineaRepository.findAllByFacturaId(facturaId)
                .stream()
                .map(FacturaLineaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FacturaLineaResponse update(UUID empresaId, UUID facturaId, UUID lineaId,
                                       FacturaLineaUpdateRequest request) {
        Factura factura = getFacturaOrThrow(empresaId, facturaId);
        validarEditable(factura);

        FacturaLinea linea = facturaLineaRepository.findByIdAndFacturaId(lineaId, facturaId)
                .orElseThrow(() -> new FacturaLineaNotFoundException("Detalle no encontrado."));

        FacturaLineaMapper.applyUpdate(linea, request);
        FacturaLinea saved = facturaLineaRepository.save(linea);

        return FacturaLineaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID empresaId, UUID facturaId, UUID lineaId) {
        Factura factura = getFacturaOrThrow(empresaId, facturaId);
        validarEditable(factura);

        FacturaLinea linea = facturaLineaRepository.findByIdAndFacturaId(lineaId, facturaId)
                .orElseThrow(() -> new FacturaLineaNotFoundException("Detalle no encontrado."));

        facturaLineaRepository.delete(linea);
    }
}