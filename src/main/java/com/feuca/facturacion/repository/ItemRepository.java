package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Item;
import com.feuca.facturacion.entity.ItemCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    // Listar items de una empresa
    List<Item> findAllByEmpresa_id(UUID empresa_id);

    // Listar items activos
    List<Item> findAllByEmpresa_idAndActivoTrue(UUID empresa_id);

    // Buscar item por nombre dentro de empresa
    Optional<Item> findByEmpresa_idAndNombre(UUID empresa_id, String nombre);

    // Validar si existe nombre dentro de empresa
    boolean existsByEmpresa_idAndNombre(UUID empresa_id, String nombre);

    // Listar por categoría (ENUM)
    List<Item> findAllByEmpresa_idAndCategoria(UUID empresa_id, ItemCategoria categoria);

    // Listar por IVA
    List<Item> findAllByEmpresa_idAndIva_id(UUID empresa_id, UUID iva_id);

    // Filtro por nombre (like)
    List<Item> findAllByEmpresa_idAndNombreContainingIgnoreCase(UUID empresa_id, String nombre);
}
