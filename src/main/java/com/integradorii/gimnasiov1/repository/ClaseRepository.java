package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    List<Clase> findByFecha(LocalDate fecha);
    List<Clase> findByInstructor(String instructor);
    List<Clase> findByEstado(String estado);
    List<Clase> findByNombreContainingIgnoreCase(String nombre);
}
