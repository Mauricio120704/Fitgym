package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.TipoClase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoClaseRepository extends JpaRepository<TipoClase, Long> {
    List<TipoClase> findByActivoTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<TipoClase> findByNombreIgnoreCase(String nombre);
}
