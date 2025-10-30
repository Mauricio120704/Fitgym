package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Locker;
import com.integradorii.gimnasiov1.model.EstadoLocker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LockerRepository extends JpaRepository<Locker, Long> {
    
    // Buscar locker por número
    Optional<Locker> findByNumero(String numero);
    
    // Buscar lockers por estado
    List<Locker> findByEstado(EstadoLocker estado);
    
    // Buscar lockers asignados a una persona
    List<Locker> findByPersonaAsignadaId(Long personaId);
    
    // Contar lockers por estado
    long countByEstado(EstadoLocker estado);
}
