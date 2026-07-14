package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Cliente.ClienteRequest;
import com.feuca.facturacion.dto.request.Item.ItemRequest;
import com.feuca.facturacion.entity.IvaTasa;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.exception.Cliente.ClienteAlreadyExistsException;
import com.feuca.facturacion.exception.Item.ItemAlreadyExistsException;
import com.feuca.facturacion.repository.ClienteRepository;
import com.feuca.facturacion.repository.IvaTasaRepository;
import com.feuca.facturacion.repository.ItemRepository;
import com.feuca.facturacion.service.AccessControlService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NormalizationServiceTest {

    @Test
    void clienteCreateChecksDuplicateEmailUsingNormalizedValue() {
        UUID empresaId = UUID.randomUUID();
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        ClienteServiceImpl service = new ClienteServiceImpl(clienteRepository, accessControlService);

        when(clienteRepository.existsByEmpresaIdAndEmailIgnoreCase(empresaId, "cliente@example.com"))
                .thenReturn(true);

        assertThrows(ClienteAlreadyExistsException.class, () -> service.create(ClienteRequest.builder()
                .empresaId(empresaId)
                .nombreRazonSocial("Cliente")
                .email(" CLIENTE@Example.COM ")
                .build()));

        verify(clienteRepository).existsByEmpresaIdAndEmailIgnoreCase(empresaId, "cliente@example.com");
    }

    @Test
    void itemCreateChecksDuplicateNameTrimmedAndCaseInsensitive() {
        UUID empresaId = UUID.randomUUID();
        UUID ivaId = UUID.randomUUID();
        ItemRepository itemRepository = mock(ItemRepository.class);
        IvaTasaRepository ivaTasaRepository = mock(IvaTasaRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        ItemServiceImpl service = new ItemServiceImpl(itemRepository, ivaTasaRepository, accessControlService);

        when(itemRepository.existsByEmpresaIdAndNombreIgnoreCase(empresaId, "Servicio Premium"))
                .thenReturn(true);
        when(ivaTasaRepository.findById(ivaId)).thenReturn(Optional.of(IvaTasa.builder()
                .id(ivaId)
                .empresaId(empresaId)
                .activo(true)
                .build()));

        assertThrows(ItemAlreadyExistsException.class, () -> service.create(ItemRequest.builder()
                .empresaId(empresaId)
                .nombre(" Servicio Premium ")
                .categoria(ItemCategoria.SERVICIO)
                .ivaId(ivaId)
                .precioSinIva(BigDecimal.TEN)
                .build()));

        verify(itemRepository).existsByEmpresaIdAndNombreIgnoreCase(empresaId, "Servicio Premium");
    }
}
