package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Entrenamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntrenamientoRepository extends JpaRepository<Entrenamiento, Long> {
    List<Entrenamiento> findAllByOrderByIdDesc();
}
