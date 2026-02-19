package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Moneda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonedaRepository extends JpaRepository<Moneda, String> {
    //READ
    Optional<Moneda> findByCodigo(String codigo);
    Optional<Moneda> findByNombre(String nombre);
    Optional<Moneda> findBySimbolo(String simbolo);
}
