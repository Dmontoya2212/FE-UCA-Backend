package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByIdAndEmpresaId(UUID id, UUID empresaId);

    List<Usuario> findAllByEmpresaId(UUID empresaId);

    Optional<Usuario> findByEmpresaIdAndEmail(UUID empresaId, String email);

    boolean existsByEmpresaIdAndEmail(UUID empresaId, String email);
}
