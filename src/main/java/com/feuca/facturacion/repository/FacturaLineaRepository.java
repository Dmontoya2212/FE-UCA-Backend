package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.FacturaLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface FacturaLineaRepository extends JpaRepository <FacturaLinea, UUID> {
    List<FacturaLinea> findAllByFacturaId(UUID facturaId);

    Optional<FacturaLinea> findByIdAndFacturaId(UUID id, UUID facturaId);
}
