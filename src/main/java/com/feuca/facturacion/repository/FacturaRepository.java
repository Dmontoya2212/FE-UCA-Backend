package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Factura;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, UUID> {
    Optional<Factura> findByIdAndEmpresaId(UUID id, UUID empresaId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Factura f WHERE f.id = :id AND f.empresaId = :empresaId")
    Optional<Factura> findAndLockByIdAndEmpresaId(@Param("id") UUID id, @Param("empresaId") UUID empresaId);
    List<Factura> findAllByEmpresaId(UUID empresaId);
    boolean existsByEmpresaIdAndNumero(UUID empresaId, String numero);
    boolean existsByEmpresaIdAndNumeroControl(UUID empresaId, String numeroControl);
    Optional<Factura> findFirstByEmpresaIdAndTipoDteOrderByNumeroDesc(UUID empresaId, String tipoDte);
    long countByEstado(String estado);
    long countByEstadoAndUpdatedAtBefore(String estado, OffsetDateTime updatedAt);
}
