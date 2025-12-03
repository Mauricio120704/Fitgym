package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByEstado(String estado);

    List<Equipo> findByTipo(String tipo);

    List<Equipo> findByUbicacion(String ubicacion);

    @Query("SELECT e FROM Equipo e WHERE e.proximoMantenimiento <= :fecha AND e.estado = 'ACTIVO'")
    List<Equipo> findEquiposConMantenimientoPendiente(@Param("fecha") LocalDate fecha);

    @Query("SELECT e FROM Equipo e WHERE e.estado = 'MANTENIMIENTO'")
    List<Equipo> findEquiposEnMantenimiento();

    @Query("SELECT e FROM Equipo e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.tipo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.marca) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.modelo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.ubicacion) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Equipo> buscarEquiposPorTermino(@Param("termino") String termino);

    @Query("SELECT COUNT(e) FROM Equipo e WHERE e.estado = :estado")
    Long contarEquiposPorEstado(@Param("estado") String estado);

    @Query("SELECT e FROM Equipo e ORDER BY e.proximoMantenimiento ASC")
    List<Equipo> findEquiposOrdenadosPorProximoMantenimiento();

    // Pageable variants
    Page<Equipo> findByEstado(String estado, Pageable pageable);

    Page<Equipo> findByTipo(String tipo, Pageable pageable);

    @Query("SELECT e FROM Equipo e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.tipo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.marca) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.modelo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.ubicacion) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<Equipo> buscarEquiposPorTermino(@Param("termino") String termino, Pageable pageable);
}
