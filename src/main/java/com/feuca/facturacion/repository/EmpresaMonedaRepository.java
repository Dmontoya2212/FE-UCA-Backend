package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Empresa;
import com.feuca.facturacion.entity.EmpresaMoneda;
import com.feuca.facturacion.entity.EmpresaMonedaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmpresaMonedaRepository extends JpaRepository<EmpresaMoneda, EmpresaMonedaId> {
    //READ
    List<EmpresaMoneda> findAllByEmpresa_id(UUID empresaId);
    List<EmpresaMoneda> findAllByMoneda_codigo(String monedaCodigo);
    
    //COUNT
    int countByMoneda_codigo(String monedaCodigo);

    //DELETE
    void deleteByMoneda_codigo(String monedaCodigo);
    void deleteByEmpresa_id(UUID empresaId);
}
