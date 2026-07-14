package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Moneda.AddMonedaRequest;
import com.feuca.facturacion.dto.response.Empresa.EmpresaResponse;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.EmpresaMoneda;
import com.feuca.facturacion.entity.Moneda;
import com.feuca.facturacion.repository.EmpresaMonedaRepository;
import com.feuca.facturacion.repository.EmpresaRepository;
import com.feuca.facturacion.repository.MonedaRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.SecretEncryptionService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmpresaServiceImplTest {

    private final EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
    private final EmpresaMonedaRepository empresaMonedaRepository = mock(EmpresaMonedaRepository.class);
    private final MonedaRepository monedaRepository = mock(MonedaRepository.class);
    private final SecretEncryptionService secretEncryptionService = mock(SecretEncryptionService.class);
    private final AccessControlService accessControlService = mock(AccessControlService.class);

    private final EmpresaServiceImpl empresaService = new EmpresaServiceImpl(
            empresaRepository,
            empresaMonedaRepository,
            monedaRepository,
            secretEncryptionService,
            accessControlService
    );

    @Test
    void getAllForRegularUserReturnsOnlyAssignedNonDeletedEmpresas() {
        UUID assignedEmpresaId = UUID.randomUUID();
        Empresa assignedEmpresa = Empresa.builder()
                .id(assignedEmpresaId)
                .nombreLegal("empresa asignada")
                .build();
        when(accessControlService.isSuperAdmin()).thenReturn(false);
        when(accessControlService.getCurrentUserEmpresaIds()).thenReturn(List.of(assignedEmpresaId));
        when(empresaRepository.findAllByIdInAndDeletedAtIsNull(List.of(assignedEmpresaId)))
                .thenReturn(List.of(assignedEmpresa));
        when(empresaMonedaRepository.findAllByEmpresa_idIn(List.of(assignedEmpresaId))).thenReturn(List.of());

        List<EmpresaResponse> result = empresaService.getAll();

        assertEquals(1, result.size());
        assertEquals(assignedEmpresaId, result.get(0).getId());
        verify(empresaRepository, never()).findAll();
        verify(empresaRepository, never()).findAllByDeletedAtIsNull();
    }

    @Test
    void getAllForSuperAdminUsesOnlyNonDeletedEmpresas() {
        UUID activeEmpresaId = UUID.randomUUID();
        Empresa activeEmpresa = Empresa.builder()
                .id(activeEmpresaId)
                .nombreLegal("empresa activa")
                .build();
        when(accessControlService.isSuperAdmin()).thenReturn(true);
        when(empresaRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(activeEmpresa));
        when(empresaMonedaRepository.findAllByEmpresa_idIn(List.of(activeEmpresaId))).thenReturn(List.of());

        List<EmpresaResponse> result = empresaService.getAll();

        assertEquals(1, result.size());
        assertEquals(activeEmpresaId, result.get(0).getId());
        verify(empresaRepository, never()).findAll();
    }

    @Test
    void filteredListsRemoveDeletedEmpresasBeforeReturning() {
        UUID activeEmpresaId = UUID.randomUUID();
        Empresa activeEmpresa = Empresa.builder()
                .id(activeEmpresaId)
                .nombreComercial("tienda")
                .build();
        Empresa deletedEmpresa = Empresa.builder()
                .id(UUID.randomUUID())
                .nombreComercial("tienda")
                .deletedAt(OffsetDateTime.now())
                .build();
        when(accessControlService.isSuperAdmin()).thenReturn(true);
        when(empresaRepository.findAllByNombreComercial("tienda"))
                .thenReturn(List.of(activeEmpresa, deletedEmpresa));
        when(empresaMonedaRepository.findAllByEmpresa_idIn(List.of(activeEmpresaId))).thenReturn(List.of());

        List<EmpresaResponse> result = empresaService.getAllByNombreComercial("tienda");

        assertEquals(1, result.size());
        assertEquals(activeEmpresaId, result.get(0).getId());
        assertTrue(result.stream().noneMatch(empresa -> empresa.getId().equals(deletedEmpresa.getId())));
    }

    @Test
    void updateMonedasDefinesPrincipalWhenEmpresaHasNone() {
        UUID empresaId = UUID.randomUUID();
        Empresa empresa = Empresa.builder()
                .id(empresaId)
                .nombreLegal("empresa")
                .build();
        Moneda usd = Moneda.builder().codigo("USD").nombre("Dolar").simbolo("$").build();
        Moneda eur = Moneda.builder().codigo("EUR").nombre("Euro").simbolo("EUR").build();
        EmpresaMoneda usdRel = EmpresaMoneda.builder()
                .empresa_id(empresaId)
                .moneda_codigo("USD")
                .moneda(usd)
                .principal(true)
                .build();
        EmpresaMoneda eurRel = EmpresaMoneda.builder()
                .empresa_id(empresaId)
                .moneda_codigo("EUR")
                .moneda(eur)
                .principal(false)
                .build();
        when(empresaRepository.findById(empresaId)).thenReturn(java.util.Optional.of(empresa));
        when(monedaRepository.findAllById(java.util.Set.of("USD", "EUR"))).thenReturn(List.of(usd, eur));
        when(empresaMonedaRepository.findAllByEmpresa_id(empresaId))
                .thenReturn(List.of(), List.of(usdRel, eurRel));

        EmpresaResponse response = empresaService.updateMonedas(empresaId, AddMonedaRequest.builder()
                .codigos(List.of("usd", "eur"))
                .build());

        ArgumentCaptor<List<EmpresaMoneda>> captor = ArgumentCaptor.forClass(List.class);
        verify(empresaMonedaRepository).saveAll(captor.capture());
        assertEquals("USD", captor.getValue().stream()
                .filter(rel -> Boolean.TRUE.equals(rel.getPrincipal()))
                .findFirst()
                .orElseThrow()
                .getMoneda_codigo());
        assertEquals(1, response.getMonedas().stream()
                .filter(moneda -> Boolean.TRUE.equals(moneda.getPrincipal()))
                .count());
    }
}
