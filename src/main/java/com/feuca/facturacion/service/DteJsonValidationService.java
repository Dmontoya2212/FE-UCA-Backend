package com.feuca.facturacion.service;

import com.feuca.facturacion.dto.dte.DteFacturaElectronica;

public interface DteJsonValidationService {
    String validarYSerializar(DteFacturaElectronica dte);
}
