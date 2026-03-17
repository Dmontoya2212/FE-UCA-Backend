package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    // READ
    Optional<Empresa> findByNombreLegal(String nombreLegal);
    Optional<Empresa> findByNifCif(String nifCif);
    Optional<Empresa> findByEmail(String email);
    Optional<Empresa> findByTelefono(String telefono);
    Optional<Empresa> findByNombreComercialAndDireccion(String nombreComercial, String direccion);
    List<Empresa> findAllByNombreComercial(String nombreComercial);
    List<Empresa> findAllByCiudad(String ciudad);
    List<Empresa> findAllByCodigoPostal(String codigoPostal);
    List<Empresa> findAllByPais(String pais);

    // COUNT
    int countAllByNombreComercial(String nombreComercial);
    int countAllByCiudad(String ciudad);
    int countAllByCodigoPostal(String codigoPostal);
    int countAllByPais(String pais);

    // EXISTS
    boolean existsByNombreLegal(String nombreLegal);
    boolean existsByNifCif(String nifCif);
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);

    // DELETE
    void deleteByNombreLegal(String nombreLegal);
    void deleteByNifCif(String nifCif);
    void deleteByEmail(String email);
    void deleteByTelefono(String telefono);
}