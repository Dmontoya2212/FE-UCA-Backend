package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.entity.*;
import com.feuca.facturacion.exception.Factura.FacturaNotFoundException;
import com.feuca.facturacion.mapper.DteBuilder;
import com.feuca.facturacion.repository.*;
import com.feuca.facturacion.service.DteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DteServiceImpl implements DteService {

    private final FacturaRepository facturaRepository;
    private final FacturaLineaRepository facturaLineaRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final ItemRepository itemRepository;
    private final DteSecuenciaRepository dteSecuenciaRepository;

    public DteServiceImpl(
            FacturaRepository facturaRepository,
            FacturaLineaRepository facturaLineaRepository,
            EmpresaRepository empresaRepository,
            ClienteRepository clienteRepository,
            ItemRepository itemRepository,
            DteSecuenciaRepository dteSecuenciaRepository
    ) {
        this.facturaRepository = facturaRepository;
        this.facturaLineaRepository = facturaLineaRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.itemRepository = itemRepository;
        this.dteSecuenciaRepository = dteSecuenciaRepository;
    }

    @Override
    @Transactional
    public DteFacturaElectronica generarDte(UUID empresaId, UUID facturaId) {
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        Empresa e = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada."));

        Cliente c = null;
        if (f.getClienteId() != null) {
            c = clienteRepository.findById(f.getClienteId()).orElse(null);
        }

        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);
        List<UUID> itemIds = lineas.stream().map(FacturaLinea::getItemId).filter(id -> id != null).collect(Collectors.toList());
        List<Item> items = itemRepository.findAllById(itemIds);

        asignarCodigos(f);

        facturaRepository.save(f);

        return DteBuilder.build(f, lineas, items, e, c, f.getNumeroControl(), f.getCodigoGeneracion());
    }

    @Override
    @Transactional
    public void asignarCodigos(Factura f) {
        UUID empresaId = f.getEmpresaId();
        Empresa e = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada."));

        // Generar codigoGeneracion
        String codigoGeneracion = f.getCodigoGeneracion();
        if (codigoGeneracion == null) {
            codigoGeneracion = UUID.randomUUID().toString().toUpperCase();
            f.setCodigoGeneracion(codigoGeneracion);
        }

        // Generar numeroControl
        if (f.getNumeroControl() == null) {
            String tipoDte = f.getTipoDte() != null ? f.getTipoDte() : "01";
            
            String codEstable = e.getCodEstablecimiento() != null ? e.getCodEstablecimiento() : "M001";
            String codPuntoVenta = e.getCodPuntoVenta() != null ? e.getCodPuntoVenta() : "P001";
            String mPart = codEstable.length() >= 4 ? codEstable.substring(0, 4) : String.format("M%03d", 1);
            String pPart = codPuntoVenta.length() >= 4 ? codPuntoVenta.substring(0, 4) : String.format("P%03d", 1);

            int maxRetries = 5;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    DteSecuencia secuencia = dteSecuenciaRepository.findAndLockByEmpresaIdAndTipoDte(empresaId, tipoDte)
                            .orElseGet(() -> {
                                // Initialize from max existing correlative if missing
                                Long initialCorrelativo = 0L;
                                java.util.Optional<Factura> maxFactura = facturaRepository.findFirstByEmpresaIdAndTipoDteOrderByNumeroDesc(empresaId, tipoDte);
                                if (maxFactura.isPresent()) {
                                    String nc = maxFactura.get().getNumeroControl();
                                    if (nc == null) {
                                        nc = maxFactura.get().getNumero();
                                    }
                                    if (nc != null && nc.length() >= 15) {
                                        try {
                                            initialCorrelativo = Long.parseLong(nc.substring(nc.length() - 15));
                                        } catch (NumberFormatException ex) {
                                            initialCorrelativo = 0L;
                                        }
                                    }
                                }
                                return DteSecuencia.builder()
                                        .id(UUID.randomUUID())
                                        .empresaId(empresaId)
                                        .tipoDte(tipoDte)
                                        .ultimoCorrelativo(initialCorrelativo)
                                        .build();
                            });

                    Long correlativo = secuencia.getUltimoCorrelativo() + 1;
                    String numeroControl = String.format("DTE-%s-%s%s-%015d", tipoDte, mPart, pPart, correlativo);

                    if (facturaRepository.existsByEmpresaIdAndNumeroControl(empresaId, numeroControl)) {
                        // If it still clashes (e.g. sequence was out of sync but row existed), sync it up and retry
                        secuencia.setUltimoCorrelativo(correlativo);
                        dteSecuenciaRepository.save(secuencia);
                        continue; // try next loop
                    }

                    secuencia.setUltimoCorrelativo(correlativo);
                    dteSecuenciaRepository.save(secuencia);
                    
                    f.setNumeroControl(numeroControl);
                    break; // Success
                } catch (Exception ex) {
                    if (attempt == maxRetries) {
                        throw new RuntimeException("Error al generar el número de control tras " + maxRetries + " intentos.", ex);
                    }
                }
            }
        }
    }
}
