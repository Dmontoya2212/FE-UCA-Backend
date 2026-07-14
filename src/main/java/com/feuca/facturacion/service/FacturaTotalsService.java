package com.feuca.facturacion.service;

import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.exception.Factura.FacturaNotFoundException;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.util.FacturaLineaCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FacturaTotalsService {

    private final FacturaRepository facturaRepository;
    private final FacturaLineaRepository facturaLineaRepository;

    public FacturaTotalsService(FacturaRepository facturaRepository,
                                FacturaLineaRepository facturaLineaRepository) {
        this.facturaRepository = facturaRepository;
        this.facturaLineaRepository = facturaLineaRepository;
    }

    @Transactional
    public Factura recalcularTotalesFactura(UUID facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));
        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);

        BigDecimal subtotalSinIva = lineas.stream()
                .map(FacturaLinea::getSubtotalSinIva)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(FacturaLineaCalculator.TOTAL_SCALE, FacturaLineaCalculator.ROUNDING_MODE);

        BigDecimal totalIva = lineas.stream()
                .map(FacturaLinea::getTotalIva)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(FacturaLineaCalculator.TOTAL_SCALE, FacturaLineaCalculator.ROUNDING_MODE);

        BigDecimal totalConIva = subtotalSinIva.add(totalIva)
                .setScale(FacturaLineaCalculator.TOTAL_SCALE, FacturaLineaCalculator.ROUNDING_MODE);

        factura.setSubtotalSinIva(subtotalSinIva);
        factura.setTotalIva(totalIva);
        factura.setTotalConIva(totalConIva);
        factura.setUpdatedAt(OffsetDateTime.now());

        return facturaRepository.save(factura);
    }
}
