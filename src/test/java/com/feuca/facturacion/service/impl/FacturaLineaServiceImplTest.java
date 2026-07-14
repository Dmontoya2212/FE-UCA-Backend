package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.FacturaLinea.FacturaLineaUpdateRequest;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.repository.ItemRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.FacturaTotalsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FacturaLineaServiceImplTest {

    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final FacturaLineaRepository facturaLineaRepository = mock(FacturaLineaRepository.class);
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final AccessControlService accessControlService = mock(AccessControlService.class);
    private final FacturaTotalsService facturaTotalsService = mock(FacturaTotalsService.class);

    private final FacturaLineaServiceImpl facturaLineaService = new FacturaLineaServiceImpl(
            facturaRepository,
            facturaLineaRepository,
            itemRepository,
            accessControlService,
            facturaTotalsService
    );

    @Test
    void updateRecalculatesLineTotalsFromBusinessFields() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        UUID lineaId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .id(facturaId)
                .empresaId(empresaId)
                .estado("BORRADOR")
                .build();
        FacturaLinea linea = FacturaLinea.builder()
                .id(lineaId)
                .facturaId(facturaId)
                .descripcion("Servicio anterior")
                .cantidad(BigDecimal.ONE)
                .precioSinIva(BigDecimal.ONE)
                .ivaPorcentaje(BigDecimal.ZERO)
                .subtotalSinIva(BigDecimal.ZERO)
                .totalIva(BigDecimal.ZERO)
                .totalConIva(BigDecimal.ZERO)
                .build();

        when(facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaLineaRepository.findByIdAndFacturaId(lineaId, facturaId)).thenReturn(Optional.of(linea));
        when(facturaLineaRepository.save(any(FacturaLinea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        facturaLineaService.update(empresaId, facturaId, lineaId, FacturaLineaUpdateRequest.builder()
                .cantidad(new BigDecimal("2"))
                .precioSinIva(new BigDecimal("5"))
                .ivaPorcentaje(new BigDecimal("10"))
                .build());

        ArgumentCaptor<FacturaLinea> captor = ArgumentCaptor.forClass(FacturaLinea.class);
        verify(facturaLineaRepository).save(captor.capture());
        FacturaLinea saved = captor.getValue();

        assertEquals(new BigDecimal("2.00"), saved.getCantidad());
        assertEquals(new BigDecimal("5.00000000"), saved.getPrecioSinIva());
        assertEquals(new BigDecimal("10.00"), saved.getIvaPorcentaje());
        assertEquals(new BigDecimal("10.00000000"), saved.getSubtotalSinIva());
        assertEquals(new BigDecimal("1.00000000"), saved.getTotalIva());
        assertEquals(new BigDecimal("11.00000000"), saved.getTotalConIva());
        verify(facturaTotalsService).recalcularTotalesFactura(facturaId);
    }

    @Test
    void deleteRecalculatesInvoiceTotalsAfterRemovingLine() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        UUID lineaId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .id(facturaId)
                .empresaId(empresaId)
                .estado("BORRADOR")
                .build();
        FacturaLinea linea = FacturaLinea.builder()
                .id(lineaId)
                .facturaId(facturaId)
                .build();

        when(facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(facturaLineaRepository.findByIdAndFacturaId(lineaId, facturaId)).thenReturn(Optional.of(linea));

        facturaLineaService.delete(empresaId, facturaId, lineaId);

        verify(facturaLineaRepository).delete(linea);
        verify(facturaTotalsService).recalcularTotalesFactura(facturaId);
    }
}
