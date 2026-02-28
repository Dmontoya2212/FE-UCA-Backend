package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.exception.Factura.FacturaAlreadyExistsException;
import com.feuca.facturacion.exception.Factura.FacturaNoEditableException;
import com.feuca.facturacion.exception.Factura.FacturaNotFoundException;
import com.feuca.facturacion.mapper.FacturaMapper;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.service.FacturaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;

    public FacturaServiceImpl(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    @Override
    @Transactional
    public FacturaResponse create(FacturaRequest request) {

        String numero = request.getNumero().trim();

        boolean exists = facturaRepository.existsByEmpresaIdAndNumero(request.getEmpresaId(), numero);
        if (exists) throw new FacturaAlreadyExistsException("Ya existe una factura con este correlativo.");

        request.setNumero(numero);

        Factura entity = FacturaMapper.toEntityCreate(request);
        entity.setEstado("BORRADOR");

        Factura saved = facturaRepository.save(entity);
        return FacturaMapper.toResponse(saved);
    }

    @Override
    public FacturaResponse getById(UUID empresaId, UUID facturaId) {
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));
        return FacturaMapper.toResponse(f);
    }

    @Override
    public List<FacturaResponse> getAllByEmpresa(UUID empresaId) {
        return facturaRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(FacturaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FacturaResponse update(UUID empresaId, UUID facturaId, FacturaUpdateRequest request) {

        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        if (!"BORRADOR".equalsIgnoreCase(f.getEstado())) {
            throw new FacturaNoEditableException("La factura ya fue enviada y no se puede editar.");
        }

        if (request.getNumero() != null) {
            String numero = request.getNumero().trim();
            boolean exists = facturaRepository.existsByEmpresaIdAndNumero(empresaId, numero);
            if (exists && !numero.equalsIgnoreCase(f.getNumero())) {
                throw new FacturaAlreadyExistsException("Ya existe una factura con este correlativo.");
            }
            request.setNumero(numero);
        }

        FacturaMapper.applyUpdate(f, request);
        Factura saved = facturaRepository.save(f);

        return FacturaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID empresaId, UUID facturaId) {

        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        if (!"BORRADOR".equalsIgnoreCase(f.getEstado())) {
            throw new FacturaNoEditableException("La factura ya fue enviada y no se puede eliminar.");
        }

        facturaRepository.delete(f);
    }

    @Override
    @Transactional
    public FacturaResponse enviarAHacienda(UUID empresaId, UUID facturaId) {

        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        if (!"BORRADOR".equalsIgnoreCase(f.getEstado())) {
            throw new FacturaNoEditableException("Ya existe una factura con este correlativo.");
        }

        // Aca se va a ser la logica del envio a hacienda (Aca nos va a llevar la que nos trajo)
        f.setEstado("ENVIADA");

        Factura saved = facturaRepository.save(f);
        return FacturaMapper.toResponse(saved);
    }
}