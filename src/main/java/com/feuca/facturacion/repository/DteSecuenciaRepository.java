package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.DteSecuencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

@Repository
public interface DteSecuenciaRepository extends JpaRepository<DteSecuencia, UUID> {
    Optional<DteSecuencia> findByEmpresaIdAndTipoDte(UUID empresaId, String tipoDte);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DteSecuencia d WHERE d.empresaId = :empresaId AND d.tipoDte = :tipoDte")
    Optional<DteSecuencia> findAndLockByEmpresaIdAndTipoDte(@Param("empresaId") UUID empresaId, @Param("tipoDte") String tipoDte);

    @Modifying
    @Query(value = """
            INSERT INTO dte_secuencias (id, empresa_id, tipo_dte, ultimo_correlativo)
            VALUES (:id, :empresaId, :tipoDte, :ultimoCorrelativo)
            ON CONFLICT (empresa_id, tipo_dte) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,
                       @Param("empresaId") UUID empresaId,
                       @Param("tipoDte") String tipoDte,
                       @Param("ultimoCorrelativo") Long ultimoCorrelativo);

    @Query("SELECT COALESCE(SUM(d.ultimoCorrelativo), 0) FROM DteSecuencia d")
    Long sumUltimoCorrelativo();
}
