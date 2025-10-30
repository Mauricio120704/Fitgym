package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Reclamo;
import com.integradorii.gimnasiov1.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    
    // Buscar reclamos por deportista
    List<Reclamo> findByDeportistaAndActivoTrueOrderByFechaCreacionDesc(Persona deportista);
    
    // Buscar reclamos por estado
    List<Reclamo> findByEstadoAndActivoTrueOrderByFechaCreacionDesc(String estado);
    
    // Buscar todos los reclamos activos
    List<Reclamo> findByActivoTrueOrderByFechaCreacionDesc();
    
    // Contar reclamos por deportista
    long countByDeportistaAndActivoTrue(Persona deportista);
}
