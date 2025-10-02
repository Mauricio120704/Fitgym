package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Clase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ClaseRepository extends JpaRepository<Clase, Long> {
    List<Clase> findAllByOrderByFechaAsc();
    List<Clase> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrderByFechaAsc(String nombre, String descripcion);
    List<Clase> findByFechaAfterOrderByFechaAsc(OffsetDateTime fecha);
}
