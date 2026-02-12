package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    // Listar todos los clientes de una empresa
    List<Cliente> findAllByEmpresa_id(UUID empresa_id);

    // Listar solo activos
    List<Cliente> findAllByEmpresa_idAndActivoTrue(UUID empresa_id);

    // Buscar cliente por nif dentro de una empresa
    Optional<Cliente> findByEmpresa_idAndNif_cif(UUID empresa_id, String nif_cif);

    // Validar si ya existe un nif en la empresa
    boolean existsByEmpresa_idAndNif_cif(UUID empresa_id, String nif_cif);

    // Buscar por email dentro de una empresa
    Optional<Cliente> findByEmpresa_idAndEmail(UUID empresa_id, String email);

    // Validar si existe email dentro de una empresa
    boolean existsByEmpresa_idAndEmail(UUID empresa_id, String email);

    // Buscar por nombre/razon social
    List<Cliente> findAllByEmpresa_idAndNombre_razon_socialContainingIgnoreCase(UUID empresa_id, String nombre_razon_social);
}
