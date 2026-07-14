package com.feuca.facturacion.service;

import com.feuca.facturacion.entity.Cliente;
import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.Factura;
import com.feuca.facturacion.entity.FacturaLinea;
import com.feuca.facturacion.entity.Item;

import java.util.List;

public interface DteValidationService {
    void validarPreEmision(
            Empresa empresa,
            Cliente cliente,
            Factura factura,
            List<FacturaLinea> lineas,
            List<Item> items,
            String ambiente
    );
}
