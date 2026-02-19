package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    //READ
    Optional<Empresa> findByNombre_legal(String nombreLegal);
    Optional<Empresa> findByNif_cif(String nifCif);
    Optional<Empresa> findByEmail(String email);
    Optional<Empresa> findByTelefono(String telefono);
    Optional<Empresa> findByNombre_comercialAndDireccion(String nombreComercial, String direccion);
    List<Empresa> findAllByOrderByNombre_comercial(String nombreComercial);
    List<Empresa> findAllByCiudad(String ciudad);
    List<Empresa> findAllByCodigo_postal(String codigoPostal);
    List<Empresa> findAllByPais(String pais);

    //COUNT
    int countAllByNombre_comercial(String nombreComercial);
    int countAllByCiudad(String ciudad);
    int countAllByCodigo_postal(String codigoPostal);
    int countAllByPais(String pais);

    //EXISTS
    boolean existsByNombre_legal(String nombreLegal);
    boolean existsByNif_cif(String nifCif);
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);

    //DELETE
    void deleteByNombre_legal(String nombreLegal);
    void deleteByNif_cif(String nifCif);
    void deleteByEmail(String email);
    void deleteByTelefono(String telefono);
}
