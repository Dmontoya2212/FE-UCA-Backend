package com.feuca.facturacion.service.impl;

import com.feuca.facturacion.dto.dte.DteFacturaElectronica;
import com.feuca.facturacion.entity.*;
import com.feuca.facturacion.exception.Cliente.ClienteNotFoundException;
import com.feuca.facturacion.exception.Factura.FacturaNotFoundException;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.exception.Item.ItemNotFoundException;
import com.feuca.facturacion.mapper.DteBuilder;
import com.feuca.facturacion.repository.*;
import com.feuca.facturacion.service.AccessControlService;
import com.feuca.facturacion.service.DteService;
import com.feuca.facturacion.service.DteValidationService;
import org.springframework.beans.factory.annotation.Value;
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
    private final AccessControlService accessControlService;
    private final DteValidationService dteValidationService;
    private final String dteAmbiente;

    public DteServiceImpl(
            FacturaRepository facturaRepository,
            FacturaLineaRepository facturaLineaRepository,
            EmpresaRepository empresaRepository,
            ClienteRepository clienteRepository,
            ItemRepository itemRepository,
            DteSecuenciaRepository dteSecuenciaRepository,
            AccessControlService accessControlService,
            DteValidationService dteValidationService,
            @Value("${dte.ambiente:}") String dteAmbiente
    ) {
        this.facturaRepository = facturaRepository;
        this.facturaLineaRepository = facturaLineaRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.itemRepository = itemRepository;
        this.dteSecuenciaRepository = dteSecuenciaRepository;
        this.accessControlService = accessControlService;
        this.dteValidationService = dteValidationService;
        this.dteAmbiente = dteAmbiente;
    }

    @Override
    @Transactional
    public DteFacturaElectronica generarDte(UUID empresaId, UUID facturaId) {
        accessControlService.requireEmpresaAccess(empresaId);
        Factura f = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada."));

        Empresa e = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada."));

        Cliente c = null;
        if (f.getClienteId() != null) {
            c = clienteRepository.findByIdAndEmpresaId(f.getClienteId(), empresaId)
                    .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado en la empresa indicada."));
        }

        List<FacturaLinea> lineas = facturaLineaRepository.findAllByFacturaId(facturaId);
        if (lineas.isEmpty()) {
            throw new FacturaValidationException("La factura debe tener al menos una linea antes de generar el DTE.");
        }
        List<Item> items = lineas.stream()
                .map(FacturaLinea::getItemId)
                .filter(id -> id != null)
                .map(itemId -> itemRepository.findByIdAndEmpresaId(itemId, empresaId)
                        .orElseThrow(() -> new ItemNotFoundException("Item no encontrado en la empresa indicada.")))
                .collect(Collectors.toList());
        if (items.stream().anyMatch(item -> Boolean.FALSE.equals(item.getActivo()))) {
            throw new FacturaValidationException("No se puede generar el DTE con items inactivos.");
        }

        applyEmisorSnapshot(f, e);
        dteValidationService.validarPreEmision(e, c, f, lineas, items, dteAmbiente);

        asignarCodigos(f);

        facturaRepository.save(f);

        return DteBuilder.build(f, lineas, items, e, c, f.getNumeroControl(), f.getCodigoGeneracion(), dteAmbiente);
    }

    private void applyEmisorSnapshot(Factura factura, Empresa empresa) {
        if (factura.getEmisorNit() != null) {
            return;
        }
        factura.setEmisorNit(empresa.getNit());
        factura.setEmisorNrc(empresa.getRegistro());
        factura.setEmisorNombre(empresa.getRazonSocial());
        factura.setEmisorCodActividad(empresa.getCodActividad());
        factura.setEmisorDescActividad(empresa.getActividadEconomica());
        factura.setEmisorNombreComercial(empresa.getNombreComercial());
        factura.setEmisorDireccion(empresa.getDireccion());
        factura.setEmisorDepartamento(empresa.getDepartamento());
        factura.setEmisorMunicipio(empresa.getMunicipio());
        factura.setEmisorDistrito(empresa.getDistrito());
        factura.setEmisorTelefono(empresa.getTelefono());
        factura.setEmisorEmail(empresa.getEmail());
        factura.setEmisorCodEstablecimiento(empresa.getCodEstablecimiento());
        factura.setEmisorCodPuntoVenta(empresa.getCodPuntoVenta());
    }

    @Override
    @Transactional
    public void asignarCodigos(Factura f) {
        // La asignacion es idempotente: una vez generado, el codigo/numero no se reemplaza.
        // El avance de secuencia participa en la transaccion; si confirma, el correlativo no se reutiliza.
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
            String tipoDte = required("tipo DTE", f.getTipoDte());
            if (!"01".equals(tipoDte)) {
                throw new FacturaValidationException("Tipo DTE no permitido para factura electronica: " + tipoDte);
            }
            
            String codEstable = required("codigo de establecimiento del emisor", e.getCodEstablecimiento());
            String codPuntoVenta = required("codigo de punto de venta del emisor", e.getCodPuntoVenta());
            if (codEstable.length() < 4 || codPuntoVenta.length() < 4) {
                throw new FacturaValidationException("Los codigos de establecimiento y punto de venta deben tener al menos 4 caracteres.");
            }
            String mPart = codEstable.substring(0, 4);
            String pPart = codPuntoVenta.substring(0, 4);

            ensureSecuenciaExists(empresaId, tipoDte);

            int maxRetries = 5;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                DteSecuencia secuencia = dteSecuenciaRepository.findAndLockByEmpresaIdAndTipoDte(empresaId, tipoDte)
                        .orElseThrow(() -> new IllegalStateException("No se pudo inicializar la secuencia DTE."));

                Long correlativo = secuencia.getUltimoCorrelativo() + 1;
                String numeroControl = String.format("DTE-%s-%s%s-%015d", tipoDte, mPart, pPart, correlativo);

                if (facturaRepository.existsByEmpresaIdAndNumeroControl(empresaId, numeroControl)) {
                    // If it still clashes (e.g. sequence was out of sync but row existed), sync it up and retry.
                    secuencia.setUltimoCorrelativo(correlativo);
                    dteSecuenciaRepository.save(secuencia);
                    continue;
                }

                secuencia.setUltimoCorrelativo(correlativo);
                dteSecuenciaRepository.save(secuencia);

                f.setNumeroControl(numeroControl);
                break;
            }

            if (f.getNumeroControl() == null) {
                throw new FacturaValidationException("Error al generar el numero de control tras " + maxRetries + " intentos.");
            }
        }
    }

    private void ensureSecuenciaExists(UUID empresaId, String tipoDte) {
        Long initialCorrelativo = 0L;
        java.util.Optional<Factura> maxFactura = facturaRepository.findFirstByEmpresaIdAndTipoDteOrderByNumeroDesc(empresaId, tipoDte);
        if (maxFactura.isPresent()) {
            String numeroControl = maxFactura.get().getNumeroControl();
            if (numeroControl == null) {
                numeroControl = maxFactura.get().getNumero();
            }
            if (numeroControl != null && numeroControl.length() >= 15) {
                try {
                    initialCorrelativo = Long.parseLong(numeroControl.substring(numeroControl.length() - 15));
                } catch (NumberFormatException ex) {
                    initialCorrelativo = 0L;
                }
            }
        }
        dteSecuenciaRepository.insertIfAbsent(UUID.randomUUID(), empresaId, tipoDte, initialCorrelativo);
    }

    private String required(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new FacturaValidationException("Falta " + field + " para generar el DTE.");
        }
        return value;
    }
}
