package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, UUID> {
    Optional<Factura> findByIdAndEmpresaId(UUID id, UUID empresaId);
    List<Factura> findAllByEmpresaId(UUID empresaId);
    boolean existsByEmpresaIdAndNumero(UUID empresaId, String numero);
}
