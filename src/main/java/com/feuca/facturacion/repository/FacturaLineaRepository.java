package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.FacturaLinea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FacturaLineaRepository extends JpaRepository<FacturaLinea, UUID> {
    List<FacturaLinea> findAllByFacturaId(UUID facturaId);
    void deleteAllByFacturaId(UUID facturaId);
    Optional<FacturaLinea> findByIdAndFacturaId(UUID id, UUID facturaId);
}
