package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    
    @Query("SELECT s FROM Suscripcion s WHERE s.deportista.id = :deportistaId")
    List<Suscripcion> findByDeportistaId(@Param("deportistaId") Long deportistaId);
    
    @Query("SELECT s FROM Suscripcion s WHERE s.deportista.email = :email AND s.estado = :estado ORDER BY s.proximoPago ASC")
    Optional<Suscripcion> findFirstByDeportista_EmailAndEstadoOrderByProximoPagoAsc(
        @Param("email") String email, 
        @Param("estado") String estado
    );
    
    @Query("SELECT s FROM Suscripcion s WHERE s.deportista.id = :deportistaId AND s.estado = 'Activa'")
    Optional<Suscripcion> findActiveByDeportistaId(@Param("deportistaId") Long deportistaId);
    
}
