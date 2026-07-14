package com.feuca.facturacion.repository;

import com.feuca.facturacion.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    // READ
    Optional<Empresa> findByNombreLegal(String nombreLegal);
    Optional<Empresa> findByNombreLegalIgnoreCase(String nombreLegal);
    Optional<Empresa> findByNit(String nit);
    Optional<Empresa> findByEmail(String email);
    Optional<Empresa> findByEmailIgnoreCase(String email);
    Optional<Empresa> findByTelefono(String telefono);
    Optional<Empresa> findByNombreComercialAndDireccion(String nombreComercial, String direccion);
    List<Empresa> findAllByDeletedAtIsNull();
    List<Empresa> findAllByIdInAndDeletedAtIsNull(Collection<UUID> ids);
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
    boolean existsByNombreLegalIgnoreCase(String nombreLegal);
    boolean existsByNit(String nit);
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByTelefono(String telefono);

    // DELETE
    void deleteByNombreLegal(String nombreLegal);
    void deleteByNit(String nit);
    void deleteByEmail(String email);
    void deleteByTelefono(String telefono);
}
