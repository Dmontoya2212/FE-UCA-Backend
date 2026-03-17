package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.IvaTasa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IvaTasaRepository extends JpaRepository<IvaTasa, UUID> {

    // Listar IVAs por empresa
    List<IvaTasa> findAllByEmpresaId(UUID empresaId);

    // Buscar por nombre dentro de empresa
    Optional<IvaTasa> findByEmpresaIdAndNombre(UUID empresaId, String nombre);

    // Validar si existe nombre dentro de empresa
    boolean existsByEmpresaIdAndNombre(UUID empresaId, String nombre);

    // Buscar por porcentaje dentro de empresa
    Optional<IvaTasa> findByEmpresaIdAndPorcentaje(UUID empresaId, BigDecimal porcentaje);

    // Validar si existe porcentaje dentro de empresa
    boolean existsByEmpresaIdAndPorcentaje(UUID empresaId, BigDecimal porcentaje);
}