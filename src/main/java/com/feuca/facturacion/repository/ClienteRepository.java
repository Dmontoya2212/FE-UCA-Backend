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
    List<Cliente> findAllByEmpresaId(UUID empresaId);

    // Listar solo activos
    List<Cliente> findAllByEmpresaIdAndActivoTrue(UUID empresaId);

    // Buscar cliente por nif dentro de una empresa
    Optional<Cliente> findByEmpresaIdAndNifCif(UUID empresaId, String nifCif);
    Optional<Cliente> findByEmpresaIdAndNifCifIgnoreCase(UUID empresaId, String nifCif);

    Optional<Cliente> findByIdAndEmpresaId(UUID id, UUID empresaId);

    // Validar si ya existe un nif en la empresa
    boolean existsByEmpresaIdAndNifCif(UUID empresaId, String nifCif);
    boolean existsByEmpresaIdAndNifCifIgnoreCase(UUID empresaId, String nifCif);

    // Buscar por email dentro de una empresa
    Optional<Cliente> findByEmpresaIdAndEmail(UUID empresaId, String email);
    Optional<Cliente> findByEmpresaIdAndEmailIgnoreCase(UUID empresaId, String email);

    // Validar si existe email dentro de una empresa
    boolean existsByEmpresaIdAndEmail(UUID empresaId, String email);
    boolean existsByEmpresaIdAndEmailIgnoreCase(UUID empresaId, String email);

    // Buscar por nombre/razon social
    List<Cliente> findAllByEmpresaIdAndNombreRazonSocialContainingIgnoreCase(UUID empresaId, String nombreRazonSocial);
}
