package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    
    // Contar personas actualmente en el gimnasio (sin salida registrada)
    int countByFechaHoraSalidaIsNull();
    
    // Obtener últimos registros de asistencia
    List<Asistencia> findTop10ByOrderByFechaHoraIngresoDesc();
    
    // Obtener últimas 50 asistencias
    List<Asistencia> findTop50ByOrderByFechaHoraIngresoDesc();
    
    // Buscar asistencias por rango de fechas
    List<Asistencia> findByFechaHoraIngresoBetweenOrderByFechaHoraIngresoDesc(
        LocalDateTime inicio, 
        LocalDateTime fin
    );
    
    // Buscar asistencia activa de una persona (sin salida)
    @Query("SELECT a FROM Asistencia a WHERE a.persona.id = :personaId AND a.fechaHoraSalida IS NULL ORDER BY a.fechaHoraIngreso DESC")
    List<Asistencia> findAsistenciaActivaByPersonaId(@Param("personaId") Long personaId);
}
