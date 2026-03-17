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
    List<Item> findAllByEmpresaId(UUID empresaId);

    // Listar items activos
    List<Item> findAllByEmpresaIdAndActivoTrue(UUID empresaId);

    // Buscar item por nombre dentro de empresa
    Optional<Item> findByEmpresaIdAndNombre(UUID empresaId, String nombre);

    // Validar si existe nombre dentro de empresa
    boolean existsByEmpresaIdAndNombre(UUID empresaId, String nombre);

    // Listar por categoría (ENUM)
    List<Item> findAllByEmpresaIdAndCategoria(UUID empresaId, ItemCategoria categoria);

    // Listar por IVA
    List<Item> findAllByEmpresaIdAndIvaId(UUID empresaId, UUID ivaId);

    // Filtro por nombre (like)
    List<Item> findAllByEmpresaIdAndNombreContainingIgnoreCase(UUID empresaId, String nombre);


}