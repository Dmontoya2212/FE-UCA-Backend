package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Item.ItemRequest;
import com.feuca.facturacion.dto.request.IvaTasa.IvaTasaUpdateRequest;
import com.feuca.facturacion.dto.request.Usuario.UsuarioUpdateRequest;
import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.entity.IvaTasa;
import com.feuca.facturacion.exception.Cliente.ClienteNotFoundException;
import com.feuca.facturacion.repository.ClienteRepository;
import com.feuca.facturacion.repository.EmpresaMonedaRepository;
import com.feuca.facturacion.repository.EmpresaRepository;
import com.feuca.facturacion.repository.FacturaLineaRepository;
import com.feuca.facturacion.repository.FacturaRepository;
import com.feuca.facturacion.repository.IvaTasaRepository;
import com.feuca.facturacion.repository.ItemRepository;
import com.feuca.facturacion.repository.UsuarioRepository;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.DteJsonValidationService;
import com.feuca.facturacion.service.DteService;
import com.feuca.facturacion.service.EmisionEvidenceService;
import com.feuca.facturacion.service.FacturaTotalsService;
import com.feuca.facturacion.service.FacturaStateValidator;
import com.feuca.facturacion.service.HaciendaService;
import com.feuca.facturacion.service.OperationalMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MultiEmpresaIsolationServiceTest {

    @Test
    void usuarioDeEmpresaACannotReadClienteFromEmpresaB() {
        UUID clienteId = UUID.randomUUID();
        UUID empresaB = UUID.randomUUID();
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        ClienteServiceImpl clienteService = new ClienteServiceImpl(clienteRepository, accessControlService);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(Cliente.builder()
                .id(clienteId)
                .empresaId(empresaB)
                .build()));
        doThrow(new AccessDeniedException("No tiene acceso a la empresa indicada."))
                .when(accessControlService).requireEmpresaAccess(empresaB);

        assertThrows(AccessDeniedException.class, () -> clienteService.getById(clienteId));
    }

    @Test
    void usuarioDeEmpresaACannotCreateItemForEmpresaB() {
        UUID empresaB = UUID.randomUUID();
        ItemRepository itemRepository = mock(ItemRepository.class);
        IvaTasaRepository ivaTasaRepository = mock(IvaTasaRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        ItemServiceImpl itemService = new ItemServiceImpl(itemRepository, ivaTasaRepository, accessControlService);
        doThrow(new AccessDeniedException("No tiene acceso a la empresa indicada."))
                .when(accessControlService).requireEmpresaAccess(empresaB);

        assertThrows(AccessDeniedException.class, () -> itemService.create(itemRequest(empresaB)));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void usuarioDeEmpresaACannotUpdateIvaFromEmpresaB() {
        UUID ivaId = UUID.randomUUID();
        UUID empresaB = UUID.randomUUID();
        IvaTasaRepository ivaTasaRepository = mock(IvaTasaRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        IvaTasaServiceImpl ivaTasaService = new IvaTasaServiceImpl(ivaTasaRepository, accessControlService);
        when(ivaTasaRepository.findById(ivaId)).thenReturn(Optional.of(IvaTasa.builder()
                .id(ivaId)
                .empresaId(empresaB)
                .build()));
        doThrow(new AccessDeniedException("No tiene acceso a la empresa indicada."))
                .when(accessControlService).requireEmpresaAccess(empresaB);

        assertThrows(AccessDeniedException.class, () -> ivaTasaService.update(ivaId, IvaTasaUpdateRequest.builder()
                .nombre("IVA")
                .porcentaje(BigDecimal.valueOf(13))
                .build()));

        verify(ivaTasaRepository, never()).save(any());
    }

    @Test
    void administradorDeEmpresaACannotAssignEmpresaBToUser() {
        UUID empresaB = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        UsuarioServiceImpl usuarioService = new UsuarioServiceImpl(usuarioRepository, empresaRepository, passwordEncoder, accessControlService);
        doThrow(new AccessDeniedException("Solo SUPERADMIN puede realizar esta operacion."))
                .when(accessControlService).requireSuperAdmin();

        assertThrows(AccessDeniedException.class, () -> usuarioService.update(
                empresaB,
                userId,
                UsuarioUpdateRequest.builder().empresaIds(java.util.List.of(empresaB)).build()
        ));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void usuarioDeEmpresaACannotReadFacturaFromEmpresaB() {
        UUID empresaB = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        FacturaRepository facturaRepository = mock(FacturaRepository.class);
        FacturaLineaRepository facturaLineaRepository = mock(FacturaLineaRepository.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        ItemRepository itemRepository = mock(ItemRepository.class);
        EmpresaMonedaRepository empresaMonedaRepository = mock(EmpresaMonedaRepository.class);
        DteService dteService = mock(DteService.class);
        DteJsonValidationService dteJsonValidationService = mock(DteJsonValidationService.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        FacturaTotalsService facturaTotalsService = mock(FacturaTotalsService.class);
        HaciendaService haciendaService = mock(HaciendaService.class);
        EmisionEvidenceService emisionEvidenceService = mock(EmisionEvidenceService.class);
        FacturaStateValidator facturaStateValidator = new FacturaStateValidator();
        OperationalMetricsService operationalMetricsService = mock(OperationalMetricsService.class);
        FacturaServiceImpl facturaService = new FacturaServiceImpl(
                facturaRepository,
                facturaLineaRepository,
                clienteRepository,
                itemRepository,
                empresaMonedaRepository,
                dteService,
                dteJsonValidationService,
                accessControlService,
                facturaTotalsService,
                haciendaService,
                emisionEvidenceService,
                facturaStateValidator,
                operationalMetricsService
        );
        doThrow(new AccessDeniedException("No tiene acceso a la empresa indicada."))
                .when(accessControlService).requireEmpresaAccess(empresaB);

        assertThrows(AccessDeniedException.class, () -> facturaService.getById(empresaB, facturaId));

        verify(facturaRepository, never()).findByIdAndEmpresaId(facturaId, empresaB);
    }

    @Test
    void superadminCanCreateItemForAnyEmpresaWhenResourceBelongsToThatEmpresa() {
        UUID empresaB = UUID.randomUUID();
        UUID ivaId = UUID.randomUUID();
        ItemRepository itemRepository = mock(ItemRepository.class);
        IvaTasaRepository ivaTasaRepository = mock(IvaTasaRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        ItemServiceImpl itemService = new ItemServiceImpl(itemRepository, ivaTasaRepository, accessControlService);
        when(itemRepository.existsByEmpresaIdAndNombreIgnoreCase(empresaB, "Servicio")).thenReturn(false);
        when(ivaTasaRepository.findById(ivaId)).thenReturn(Optional.of(IvaTasa.builder()
                .id(ivaId)
                .empresaId(empresaB)
                .porcentaje(BigDecimal.valueOf(13))
                .activo(true)
                .build()));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> itemService.create(ItemRequest.builder()
                .empresaId(empresaB)
                .nombre("Servicio")
                .categoria(ItemCategoria.SERVICIO)
                .ivaId(ivaId)
                .precioSinIva(BigDecimal.TEN)
                .build()));
    }

    @Test
    void nonexistentClienteIdReturnsNotFound() {
        UUID clienteId = UUID.randomUUID();
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        ClienteServiceImpl clienteService = new ClienteServiceImpl(clienteRepository, accessControlService);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.getById(clienteId));
    }

    private ItemRequest itemRequest(UUID empresaId) {
        return ItemRequest.builder()
                .empresaId(empresaId)
                .nombre("Servicio")
                .categoria(ItemCategoria.SERVICIO)
                .ivaId(UUID.randomUUID())
                .precioSinIva(BigDecimal.TEN)
                .build();
    }
}
