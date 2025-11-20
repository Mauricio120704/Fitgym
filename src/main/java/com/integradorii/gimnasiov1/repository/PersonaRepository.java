package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Persona (solo Deportistas)
 */
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    
    Optional<Persona> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByDni(String dni);
    
    /**
     * Buscar deportistas por nombre, apellido, email, teléfono o DNI
     */
    @Query("SELECT p FROM Persona p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "(p.telefono IS NOT NULL AND p.telefono LIKE CONCAT('%', :term, '%')) OR " +
            "LOWER(p.dni) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Persona> searchDeportistas(@Param("term") String term);

    List<Persona> findByEmailVerificadoTrueAndActivoTrue();

    /**
     * Contar deportistas con membresía activa
     */
    @Query("SELECT COUNT(p) FROM Persona p WHERE p.membresiaActiva = true")
    long countDeportistasActivos();

    /**
     * Contar deportistas con membresía inactiva
     */
    @Query("SELECT COUNT(p) FROM Persona p WHERE p.membresiaActiva = false")
    long countDeportistasInactivos();
    
    /**
     * Obtener deportistas con membresía activa
     */
    List<Persona> findByMembresiaActiva(Boolean membresiaActiva);
    
    /**
     * Obtener solo deportistas con membresía activa
     */
    List<Persona> findByMembresiaActivaTrue();

    /**
     * Obtener deportistas visibles en comunidad
     */
    List<Persona> findByPerfilVisibleTrue();

    /**
     * Obtener deportistas visibles en comunidad excluyendo por email
     */
    List<Persona> findByPerfilVisibleTrueAndEmailNot(String email);
    
    /**
     * Obtener deportistas activos (cuenta y email) con suscripción activa a un plan específico
     */
    @Query("SELECT DISTINCT s.deportista FROM Suscripcion s " +
           "WHERE s.estado = 'Activa' " +
           "AND s.plan.id = :planId " +
           "AND s.deportista.emailVerificado = true " +
           "AND s.deportista.activo = true")
    List<Persona> findDeportistasActivosPorPlanId(@Param("planId") Integer planId);

    /**
     * Buscar deportistas por nombre, apellido o DNI (para búsqueda simple)
     */
    List<Persona> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrDniContaining(
        String nombre, String apellido, String dni);
    
    /**
     * Buscar persona por DNI
     */
    Optional<Persona> findByDni(String dni);
    
    /**
     * Buscar deportistas activos por nombre o apellido (para asignación de lockers)
     */
    @Query("SELECT p FROM Persona p WHERE p.membresiaActiva = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :termino, '%')))")
    List<Persona> buscarDeportistasActivosPorNombreOApellido(@Param("termino") String termino);
}
