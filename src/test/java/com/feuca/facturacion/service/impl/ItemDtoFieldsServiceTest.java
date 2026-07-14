package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.request.Item.ItemRequest;
import com.feuca.facturacion.dto.response.Item.ItemResponse;
import com.feuca.facturacion.entity.IvaTasa;
import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import com.feuca.facturacion.exception.Item.ItemAlreadyExistsException;
import com.feuca.facturacion.repository.IvaTasaRepository;
import com.feuca.facturacion.repository.ItemRepository;
import com.feuca.facturacion.service.AccessControlService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemDtoFieldsServiceTest {

    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final IvaTasaRepository ivaTasaRepository = mock(IvaTasaRepository.class);
    private final AccessControlService accessControlService = mock(AccessControlService.class);
    private final ItemServiceImpl service = new ItemServiceImpl(itemRepository, ivaTasaRepository, accessControlService);

    @Test
    void createCopiesIvaSnapshotAndExposesCodigoInternoAndUnidadMedida() {
        UUID empresaId = UUID.randomUUID();
        UUID ivaId = UUID.randomUUID();
        when(ivaTasaRepository.findById(ivaId)).thenReturn(Optional.of(IvaTasa.builder()
                .id(ivaId)
                .empresaId(empresaId)
                .porcentaje(new BigDecimal("13.00"))
                .activo(true)
                .build()));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = service.create(ItemRequest.builder()
                .empresaId(empresaId)
                .nombre("Servicio")
                .categoria(ItemCategoria.SERVICIO)
                .ivaId(ivaId)
                .precioSinIva(BigDecimal.TEN)
                .codigoInterno(" srv-001 ")
                .unidadMedida(59)
                .build());

        assertEquals("SRV-001", response.getCodigoInterno());
        assertEquals(59, response.getUnidadMedida());
        assertEquals(new BigDecimal("13.00"), response.getIvaPorcentajeSnapshot());
    }

    @Test
    void createRejectsDuplicateCodigoInternoWithinEmpresa() {
        UUID empresaId = UUID.randomUUID();
        UUID ivaId = UUID.randomUUID();
        when(itemRepository.existsByEmpresaIdAndCodigoInternoIgnoreCase(empresaId, "SRV-001"))
                .thenReturn(true);

        assertThrows(ItemAlreadyExistsException.class, () -> service.create(ItemRequest.builder()
                .empresaId(empresaId)
                .nombre("Servicio")
                .categoria(ItemCategoria.SERVICIO)
                .ivaId(ivaId)
                .precioSinIva(BigDecimal.TEN)
                .codigoInterno(" srv-001 ")
                .unidadMedida(59)
                .build()));

        verify(itemRepository, never()).save(any());
    }
}
