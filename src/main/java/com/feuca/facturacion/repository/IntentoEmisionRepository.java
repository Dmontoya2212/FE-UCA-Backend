package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.IntentoEmision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IntentoEmisionRepository extends JpaRepository<IntentoEmision, UUID> {
    long countByFacturaId(UUID facturaId);
}
