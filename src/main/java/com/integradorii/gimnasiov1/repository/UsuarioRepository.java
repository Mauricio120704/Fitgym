package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Usuario (Personal Administrativo)
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Buscar usuario por email
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Buscar usuario por DNI
     */
    Optional<Usuario> findByDni(String dni);
    
    /**
     * Verificar si existe un email
     */
    boolean existsByEmail(String email);
    
    /**
     * Verificar si existe un DNI
     */
    boolean existsByDni(String dni);
    
    /**
     * Buscar usuarios activos
     */
    List<Usuario> findByActivoTrue();
    
    /**
     * Buscar usuarios inactivos
     */
    List<Usuario> findByActivoFalse();
    
    /**
     * Buscar usuarios por rol
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.codigo = :rolCodigo")
    List<Usuario> findByRolCodigo(@Param("rolCodigo") String rolCodigo);
    
    /**
     * Buscar usuarios activos por rol
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.codigo = :rolCodigo AND u.activo = true")
    List<Usuario> findActiveByRolCodigo(@Param("rolCodigo") String rolCodigo);
    
    /**
     * Búsqueda de usuarios administrativos
     */
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.dni) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Usuario> searchUsuarios(@Param("query") String query);
    
    /**
     * Contar usuarios activos
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = true")
    long countUsuariosActivos();
    
    /**
     * Contar usuarios inactivos
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = false")
    long countUsuariosInactivos();
    
    /**
     * Contar usuarios por rol
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.codigo = :rolCodigo")
    long countByRolCodigo(@Param("rolCodigo") String rolCodigo);
    
    /**
     * Obtener todos los administradores activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.codigo = 'ADMINISTRADOR' AND u.activo = true")
    List<Usuario> findActiveAdministradores();
    
    /**
     * Obtener todos los entrenadores activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.codigo = 'ENTRENADOR' AND u.activo = true")
    List<Usuario> findActiveEntrenadores();
    
    /**
     * Obtener todos los recepcionistas activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.codigo = 'RECEPCIONISTA' AND u.activo = true")
    List<Usuario> findActiveRecepcionistas();
}
