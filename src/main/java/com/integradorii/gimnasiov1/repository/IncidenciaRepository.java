package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
    List<Incidencia> findByEstado(String estado);
    List<Incidencia> findByPrioridad(String prioridad);
    List<Incidencia> findByEstadoAndPrioridad(String estado, String prioridad);
    List<Incidencia> findByTituloContainingIgnoreCase(String titulo);
}
