package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.repository.ClienteRepository;
import com.feuca.facturacion.repository.DteSecuenciaRepository;
import com.feuca.facturacion.repository.EmpresaRepository;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.repository.ItemRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.DteValidationService;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DteServiceImplTest {

    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final FacturaLineaRepository facturaLineaRepository = mock(FacturaLineaRepository.class);
    private final EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final DteSecuenciaRepository dteSecuenciaRepository = mock(DteSecuenciaRepository.class);
    private final AccessControlService accessControlService = mock(AccessControlService.class);
    private final DteValidationService dteValidationService = mock(DteValidationService.class);

    private final DteServiceImpl dteService = new DteServiceImpl(
            facturaRepository,
            facturaLineaRepository,
            empresaRepository,
            clienteRepository,
            itemRepository,
            dteSecuenciaRepository,
            accessControlService,
            dteValidationService,
            "00"
    );

    @Test
    void asignarCodigosIsIdempotentAndDoesNotConsumeSequenceAgain() {
        UUID empresaId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .empresaId(empresaId)
                .tipoDte("01")
                .numeroControl("DTE-01-M001P001-000000000000123")
                .codigoGeneracion(UUID.randomUUID().toString().toUpperCase())
                .build();
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(Empresa.builder()
                .id(empresaId)
                .codEstablecimiento("M001")
                .codPuntoVenta("P001")
                .build()));

        dteService.asignarCodigos(factura);

        assertEquals("DTE-01-M001P001-000000000000123", factura.getNumeroControl());
        verify(dteSecuenciaRepository, never()).findAndLockByEmpresaIdAndTipoDte(empresaId, "01");
        verify(dteSecuenciaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void asignarCodigosRejectsMissingTipoDteInsteadOfDefaultingFacturaElectronica() {
        UUID empresaId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .empresaId(empresaId)
                .build();
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(Empresa.builder()
                .id(empresaId)
                .codEstablecimiento("M001")
                .codPuntoVenta("P001")
                .build()));

        assertThrows(FacturaValidationException.class, () -> dteService.asignarCodigos(factura));

        verify(dteSecuenciaRepository, never()).findAndLockByEmpresaIdAndTipoDte(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void asignarCodigosRejectsMissingEstablecimientoInsteadOfDefaultingM001() {
        UUID empresaId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .empresaId(empresaId)
                .tipoDte("01")
                .build();
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(Empresa.builder()
                .id(empresaId)
                .codPuntoVenta("P001")
                .build()));

        assertThrows(FacturaValidationException.class, () -> dteService.asignarCodigos(factura));

        verify(dteSecuenciaRepository, never()).findAndLockByEmpresaIdAndTipoDte(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void generarDteDoesNotAssignCodesWhenPreEmissionValidationFails() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Factura factura = Factura.builder()
                .id(facturaId)
                .empresaId(empresaId)
                .tipoDte("01")
                .build();
        Empresa empresa = Empresa.builder()
                .id(empresaId)
                .codEstablecimiento("M001")
                .codPuntoVenta("P001")
                .build();
        FacturaLinea linea = FacturaLinea.builder()
                .facturaId(facturaId)
                .itemId(itemId)
                .build();
        Item item = Item.builder()
                .id(itemId)
                .empresaId(empresaId)
                .activo(true)
                .build();
        when(facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)).thenReturn(Optional.of(factura));
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(facturaLineaRepository.findAllByFacturaId(facturaId)).thenReturn(List.of(linea));
        when(itemRepository.findByIdAndEmpresaId(itemId, empresaId)).thenReturn(Optional.of(item));
        doThrow(new FacturaValidationException(List.of("empresa.nit: Campo obligatorio para emitir DTE.")))
                .when(dteValidationService)
                .validarPreEmision(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyString());

        assertThrows(FacturaValidationException.class, () -> dteService.generarDte(empresaId, facturaId));

        verify(dteSecuenciaRepository, never()).findAndLockByEmpresaIdAndTipoDte(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(facturaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
