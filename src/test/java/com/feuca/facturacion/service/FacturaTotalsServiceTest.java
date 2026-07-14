package com.feuca.facturacion.service;

import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FacturaTotalsServiceTest {

    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final FacturaLineaRepository facturaLineaRepository = mock(FacturaLineaRepository.class);
    private final FacturaTotalsService facturaTotalsService = new FacturaTotalsService(
            facturaRepository,
            facturaLineaRepository
    );

    @Test
    void recalcularTotalesFacturaUsesCurrentLinesAfterUpdate() {
        UUID facturaId = UUID.randomUUID();
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(Factura.builder()
                .id(facturaId)
                .build()));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(
                linea("10.00000000", "1.30000000"),
                linea("20.00000000", "2.60000000")
        ));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        facturaTotalsService.recalcularTotalesFactura(facturaId);

        Factura saved = capturarFacturaGuardada();
        assertEquals(new BigDecimal("30.00000000"), saved.getSubtotalSinIva());
        assertEquals(new BigDecimal("3.90000000"), saved.getTotalIva());
        assertEquals(new BigDecimal("33.90000000"), saved.getTotalConIva());
    }

    @Test
    void recalcularTotalesFacturaUsesRemainingLinesAfterDelete() {
        UUID facturaId = UUID.randomUUID();
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(Factura.builder()
                .id(facturaId)
                .build()));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(
                linea("7.50000000", "0.97500000")
        ));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        facturaTotalsService.recalcularTotalesFactura(facturaId);

        Factura saved = capturarFacturaGuardada();
        assertEquals(new BigDecimal("7.50000000"), saved.getSubtotalSinIva());
        assertEquals(new BigDecimal("0.97500000"), saved.getTotalIva());
        assertEquals(new BigDecimal("8.47500000"), saved.getTotalConIva());
    }

    @Test
    void recalcularTotalesFacturaEqualsExactSumOfAllLines() {
        UUID facturaId = UUID.randomUUID();
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(Factura.builder()
                .id(facturaId)
                .build()));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(
                linea("20.00000000", "2.60000000"),
                linea("15.00000000", "0.00000000"),
                linea("1.23456789", "0.16049383")
        ));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        facturaTotalsService.recalcularTotalesFactura(facturaId);

        Factura saved = capturarFacturaGuardada();
        assertEquals(new BigDecimal("36.23456789"), saved.getSubtotalSinIva());
        assertEquals(new BigDecimal("2.76049383"), saved.getTotalIva());
        assertEquals(new BigDecimal("38.99506172"), saved.getTotalConIva());
    }

    private Factura capturarFacturaGuardada() {
        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(captor.capture());
        return captor.getValue();
    }

    private FacturaLinea linea(String subtotalSinIva, String totalIva) {
        return FacturaLinea.builder()
                .subtotalSinIva(new BigDecimal(subtotalSinIva))
                .totalIva(new BigDecimal(totalIva))
                .build();
    }
}
