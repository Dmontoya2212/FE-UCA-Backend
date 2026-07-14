package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.entity.IvaTasa;
import com.feuca.facturacion.exception.IvaTasa.IvaTasaNotFoundException;
import com.feuca.facturacion.repository.IvaTasaRepository;
import com.feuca.facturacion.service.AccessControlService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IvaTasaSoftDeleteTest {

    private final IvaTasaRepository ivaTasaRepository = mock(IvaTasaRepository.class);
    private final AccessControlService accessControlService = mock(AccessControlService.class);
    private final IvaTasaServiceImpl service = new IvaTasaServiceImpl(ivaTasaRepository, accessControlService);

    @Test
    void deleteByIdMarksInactiveAndDeletedAtInsteadOfPhysicalDelete() {
        UUID ivaId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        IvaTasa iva = IvaTasa.builder()
                .id(ivaId)
                .empresaId(empresaId)
                .nombre("IVA 13")
                .porcentaje(new BigDecimal("13.00"))
                .activo(true)
                .build();
        when(ivaTasaRepository.findById(ivaId)).thenReturn(Optional.of(iva));
        when(ivaTasaRepository.save(any(IvaTasa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteById(ivaId);

        assertFalse(iva.getActivo());
        assertNotNull(iva.getDeletedAt());
        verify(ivaTasaRepository).save(iva);
        verify(ivaTasaRepository, never()).delete(any(IvaTasa.class));
    }

    @Test
    void getAllUsesOnlyActiveNotDeletedRates() {
        UUID empresaId = UUID.randomUUID();
        when(ivaTasaRepository.findAllByEmpresaIdAndActivoTrueAndDeletedAtIsNull(empresaId))
                .thenReturn(List.of(IvaTasa.builder()
                        .id(UUID.randomUUID())
                        .empresaId(empresaId)
                        .nombre("IVA 13")
                        .porcentaje(new BigDecimal("13.00"))
                        .activo(true)
                        .build()));

        assertEquals(1, service.getAllByEmpresaId(empresaId).size());

        verify(ivaTasaRepository).findAllByEmpresaIdAndActivoTrueAndDeletedAtIsNull(empresaId);
    }

    @Test
    void getByIdDoesNotExposeDeletedRate() {
        UUID ivaId = UUID.randomUUID();
        when(ivaTasaRepository.findById(ivaId)).thenReturn(Optional.of(IvaTasa.builder()
                .id(ivaId)
                .empresaId(UUID.randomUUID())
                .deletedAt(java.time.OffsetDateTime.now())
                .build()));

        assertThrows(IvaTasaNotFoundException.class, () -> service.getById(ivaId));
    }
}
