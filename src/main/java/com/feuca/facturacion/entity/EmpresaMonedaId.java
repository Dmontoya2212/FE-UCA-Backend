package com.feuca.facturacion.entity;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaMonedaId implements Serializable {
    private UUID empresa_id;
    private String moneda_codigo;
}
