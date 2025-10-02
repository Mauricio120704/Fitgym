package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
    List<Evaluacion> findAllByOrderByFechaDesc();
    List<Evaluacion> findByDeportista_EmailOrderByFechaDesc(String email);
    List<Evaluacion> findByFechaBetweenOrderByFechaDesc(OffsetDateTime desde, OffsetDateTime hasta);
}
