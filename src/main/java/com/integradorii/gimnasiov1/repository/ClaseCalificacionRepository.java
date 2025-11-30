package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.ClaseCalificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaseCalificacionRepository extends JpaRepository<ClaseCalificacion, Long> {
    Optional<ClaseCalificacion> findByReservaId(Long reservaId);
    List<ClaseCalificacion> findByDeportistaId(Long deportistaId);
    void deleteByReservaId(Long reservaId);
    boolean existsByReservaId(Long reservaId);
    void deleteByClaseId(Long claseId);
}
