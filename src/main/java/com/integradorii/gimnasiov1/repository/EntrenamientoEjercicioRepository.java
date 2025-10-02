package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.EntrenamientoEjercicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrenamientoEjercicioRepository extends JpaRepository<EntrenamientoEjercicio, Long> {
    void deleteByEntrenamiento_Id(Long entrenamientoId);
}
