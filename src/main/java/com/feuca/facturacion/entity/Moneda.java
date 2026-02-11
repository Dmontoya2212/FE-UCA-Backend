package com.feuca.facturacion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monedas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Moneda {
    @Id
    @Column(name = "codigo", length = 3)
    private String codigo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "simbolo")
    private String simbolo;
}
