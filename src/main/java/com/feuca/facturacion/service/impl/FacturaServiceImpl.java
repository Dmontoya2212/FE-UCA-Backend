package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Factura.FacturaRequest;
import com.feuca.facturacion.dto.request.Factura.FacturaUpdateRequest;
import com.feuca.facturacion.dto.response.Factura.FacturaResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.enums.InvoiceStatus;
import com.feuca.facturacion.exception.Factura.FacturaAlreadyExistsException;
import com.feuca.facturacion.exception.Factura.FacturaNoEditableException;
import com.feuca.facturacion.exception.Factura.FacturaNotFoundException;
import com.feuca.facturacion.mapper.FacturaMapper;
import com.feuca.facturacion.repository.ClienteRepository;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.service.FacturaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaLineaRepository facturaLineaRepository;
    private final ClienteRepository clienteRepository;

    public FacturaServiceImpl(FacturaRepository facturaRepository,
                              FacturaLineaRepository facturaLineaRepository,
                              ClienteRepository clienteRepository) {
        this.facturaRepository = facturaRepository;
        this.facturaLineaRepository = facturaLineaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public FacturaResponse create(FacturaRequest request) {

        String numero = request.getNumero().trim();
        boolean exists = facturaRepository.existsByEmpresaIdAndNumero(request.getEmpresaId(), numero);
        if (exists) throw new FacturaAlreadyExistsException("Ya existe una factura con este correlativo.");
        request.setNumero(numero);

        Factura factura = FacturaMapper.toEntityCreate(request);
        Factura savedFactura = facturaRepository.save(factura);

        List<FacturaLinea> lineas = request.getLineas().stream()
                .map(lineaReq -> FacturaMapper.toLineaEntity(lineaReq, savedFactura.getId()))
                .collect(Collectors.toList());

        List<FacturaLinea> savedLineas = facturaLineaRepository.saveAll(lineas);

        BigDecimal subtotalSinIva = savedLineas.stream()
                .map(FacturaLinea::getSubtotalSinIva)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalIva = savedLineas.stream()
                .map(FacturaLinea::getTotalIva)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalConIva = subtotalSinIva.add(totalIva).setScale(2, RoundingMode.HALF_UP);

        savedFactura.setSubtotalSinIva(subtotalSinIva);
        savedFactura.setTotalIva(totalIva);
        savedFactura.setTotalConIva(totalConIva);
        facturaRepository.save(savedFactura);

        String clienteNombre = null;
        if (savedFactura.getClienteId() != null) {
            clienteNombre = clienteRepository.findById(savedFactura.getClienteId())
                    .map(c -> c.getNombreRazonSocial())
                    .orElse(null);
        }

        return FacturaMapper.toResponse(savedFactura, savedLineas, clienteNombre);
    }

    @Override
    public FacturaResponse getById(UUID empresaId, UUID facturaId) {
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);

        String clienteNombre = null;
        if (f.getClienteId() != null) {
            clienteNombre = clienteRepository.findById(f.getClienteId())
                    .map(c -> c.getNombreRazonSocial())
                    .orElse(null);
        }

        return FacturaMapper.toResponse(f, lineas, clienteNombre);
    }

    @Override
    public List<FacturaResponse> getAllByEmpresa(UUID empresaId) {
        return facturaRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(f -> {
                    List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(f.getId());
                    String clienteNombre = null;
                    if (f.getClienteId() != null) {
                        clienteNombre = clienteRepository.findById(f.getClienteId())
                                .map(c -> c.getNombreRazonSocial())
                                .orElse(null);
                    }
                    return FacturaMapper.toResponse(f, lineas, clienteNombre);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FacturaResponse update(UUID empresaId, UUID facturaId, FacturaUpdateRequest request) {
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        if (f.getEstado() != InvoiceStatus.BORRADOR) {
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

        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);
        String clienteNombre = null;
        if (saved.getClienteId() != null) {
            clienteNombre = clienteRepository.findById(saved.getClienteId())
                    .map(c -> c.getNombreRazonSocial())
                    .orElse(null);
        }

        return FacturaMapper.toResponse(saved, lineas, clienteNombre);
    }

    @Override
    @Transactional
    public void delete(UUID empresaId, UUID facturaId) {
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        if (f.getEstado() != InvoiceStatus.BORRADOR) {
            throw new FacturaNoEditableException("La factura ya fue enviada y no se puede eliminar.");
        }

        facturaLineaRepository.deleteAllByFacturaId(facturaId);
        facturaRepository.delete(f);
    }

    @Override
    @Transactional
    public FacturaResponse enviarAHacienda(UUID empresaId, UUID facturaId) {
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        if (f.getEstado() != InvoiceStatus.BORRADOR) {
            throw new FacturaNoEditableException("La factura ya fue enviada.");
        }

        f.setEstado(InvoiceStatus.EMITIDA);
        Factura saved = facturaRepository.save(f);

        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);
        String clienteNombre = null;
        if (saved.getClienteId() != null) {
            clienteNombre = clienteRepository.findById(saved.getClienteId())
                    .map(c -> c.getNombreRazonSocial())
                    .orElse(null);
        }

        return FacturaMapper.toResponse(saved, lineas, clienteNombre);
    }
}