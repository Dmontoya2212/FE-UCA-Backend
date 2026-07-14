package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByIdAndEmpresasId(UUID id, UUID empresaId);

    List<Usuario> findAllByEmpresasId(UUID empresaId);

    Optional<Usuario> findByEmpresasIdAndEmail(UUID empresaId, String email);

    boolean existsByEmpresasIdAndEmail(UUID empresaId, String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usuario> findByEmail(String email);

    long countByRolAndActivoTrue(String rol);
}
