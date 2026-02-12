package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.IvaTasa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IvaTasaRepository extends JpaRepository<IvaTasa, UUID> {

    // Listar IVAs por empresa
    List<IvaTasa> findAllByEmpresa_id(UUID empresa_id);

    // Buscar por nombre dentro de empresa
    Optional<IvaTasa> findByEmpresa_idAndNombre(UUID empresa_id, String nombre);

    // Validar si existe nombre dentro de empresa
    boolean existsByEmpresa_idAndNombre(UUID empresa_id, String nombre);

    // Buscar por porcentaje dentro de empresa
    Optional<IvaTasa> findByEmpresa_idAndPorcentaje(UUID empresa_id, Double porcentaje);

    // Validar si existe porcentaje dentro de empresa
    boolean existsByEmpresa_idAndPorcentaje(UUID empresa_id, Double porcentaje);
}
