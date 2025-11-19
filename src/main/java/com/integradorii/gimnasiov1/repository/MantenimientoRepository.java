package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {

    List<Mantenimiento> findByEquipoId(Long equipoId);

    List<Mantenimiento> findByEstado(String estado);

    List<Mantenimiento> findByTipoServicio(String tipoServicio);

    List<Mantenimiento> findByTecnicoResponsable(String tecnicoResponsable);

    @Query("SELECT m FROM Mantenimiento m WHERE m.fechaServicio BETWEEN :fechaInicio AND :fechaFin")
    List<Mantenimiento> findByFechaServicioBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                                   @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT m FROM Mantenimiento m WHERE m.equipo.id = :equipoId ORDER BY m.fechaServicio DESC")
    List<Mantenimiento> findHistorialMantenimientoPorEquipo(@Param("equipoId") Long equipoId);

    @Query("SELECT COUNT(m) FROM Mantenimiento m WHERE m.estado = :estado")
    Long contarMantenimientosPorEstado(@Param("estado") String estado);

    @Query("SELECT m FROM Mantenimiento m WHERE m.estado = 'PROGRAMADO' AND m.fechaServicio <= :fecha")
    List<Mantenimiento> findMantenimientosProgramadosPendientes(@Param("fecha") LocalDate fecha);

    @Query("SELECT m FROM Mantenimiento m WHERE LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(m.tecnicoResponsable) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(m.observaciones) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Mantenimiento> buscarMantenimientosPorTermino(@Param("termino") String termino);
}
