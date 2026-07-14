package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.dto.response.FacturaLinea.FacturaLineaResponse;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.enums.EstadoFactura;
import com.feuca.facturacion.exception.Factura.FacturaNoEditableException;
import com.feuca.facturacion.exception.Factura.FacturaNotFoundException;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.exception.FacturaLinea.FacturaLineaNotFoundException;
import com.feuca.facturacion.exception.Item.ItemNotFoundException;
import com.feuca.facturacion.mapper.FacturaLineaMapper;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.repository.ItemRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.FacturaLineaService;
import com.feuca.facturacion.service.FacturaTotalsService;
import com.feuca.facturacion.util.FacturaLineaCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FacturaLineaServiceImpl implements FacturaLineaService {

    private final FacturaRepository facturaRepository;
    private final FacturaLineaRepository facturaLineaRepository;
    private final ItemRepository itemRepository;
    private final AccessControlService accessControlService;
    private final FacturaTotalsService facturaTotalsService;

    public FacturaLineaServiceImpl(FacturaRepository facturaRepository,
                                   FacturaLineaRepository facturaLineaRepository,
                                   ItemRepository itemRepository,
                                   AccessControlService accessControlService,
                                   FacturaTotalsService facturaTotalsService) {
        this.facturaRepository = facturaRepository;
        this.facturaLineaRepository = facturaLineaRepository;
        this.itemRepository = itemRepository;
        this.accessControlService = accessControlService;
        this.facturaTotalsService = facturaTotalsService;
    }

    private Factura getFacturaOrThrow(UUID empresaId, UUID facturaId) {
        accessControlService.requireEmpresaAccess(empresaId);
        return facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));
    }

    private void validarEditable(Factura f) {
        if (!EstadoFactura.fromValue(f.getEstado()).esEditable()) {
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
        validateItemBelongsToEmpresa(request.getItemId(), empresaId);

        FacturaLinea linea = facturaLineaRepository.findByIdAndFacturaId(lineaId, facturaId)
                .orElseThrow(() -> new FacturaLineaNotFoundException("Detalle no encontrado."));

        FacturaLineaMapper.applyUpdate(linea, request);
        if (request.getItemId() != null) {
            applyItemSnapshot(linea, request.getItemId(), empresaId);
        }
        FacturaLineaCalculator.recalcular(linea);
        linea.setUpdatedAt(OffsetDateTime.now());
        FacturaLinea saved = facturaLineaRepository.save(linea);
        facturaTotalsService.recalcularTotalesFactura(facturaId);

        return FacturaLineaMapper.toResponse(saved);
    }

    private void validateItemBelongsToEmpresa(UUID itemId, UUID empresaId) {
        if (itemId == null) {
            return;
        }
        var item = itemRepository.findByIdAndEmpresaId(itemId, empresaId)
                .orElseThrow(() -> new ItemNotFoundException("Item no encontrado en la empresa indicada."));
        if (Boolean.FALSE.equals(item.getActivo())) {
            throw new FacturaValidationException("No se puede usar un item inactivo en la factura.");
        }
    }

    private void applyItemSnapshot(FacturaLinea linea, UUID itemId, UUID empresaId) {
        itemRepository.findByIdAndEmpresaId(itemId, empresaId)
                .ifPresent(item -> {
                    linea.setItemCodigoInterno(item.getCodigoInterno());
                    linea.setItemUnidadMedida(item.getUnidadMedida() != null ? item.getUnidadMedida() : 59);
                    linea.setItemTipo(item.getCategoria() == ItemCategoria.PRODUCTO ? 1 : 2);
                    linea.setItemCategoria(item.getCategoria() != null ? item.getCategoria().name() : null);
                });
    }

    @Override
    @Transactional
    public void delete(UUID empresaId, UUID facturaId, UUID lineaId) {
        Factura factura = getFacturaOrThrow(empresaId, facturaId);
        validarEditable(factura);

        FacturaLinea linea = facturaLineaRepository.findByIdAndFacturaId(lineaId, facturaId)
                .orElseThrow(() -> new FacturaLineaNotFoundException("Detalle no encontrado."));

        facturaLineaRepository.delete(linea);
        facturaTotalsService.recalcularTotalesFactura(facturaId);
    }
}
