package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    List<Persona> findByTipo(String tipo);
    Optional<Persona> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);

    @Query("select p from Persona p where p.tipo = 'PERSONAL' and (" +
            " lower(p.nombre) like lower(concat('%', :term, '%')) or" +
            " lower(p.apellido) like lower(concat('%', :term, '%')) or" +
            " lower(p.email) like lower(concat('%', :term, '%')) or" +
            " (p.telefono is not null and p.telefono like concat('%', :term, '%')) or" +
            " lower(p.dni) like lower(concat('%', :term, '%')) )")
    List<Persona> searchPersonal(@Param("term") String term);

    @Query("select count(p) from Persona p where p.tipo='PERSONAL' and p.membresiaActiva=true")
    long countPersonalActivos();

    @Query("select count(p) from Persona p where p.tipo='PERSONAL' and p.membresiaActiva=false")
    long countPersonalInactivos();
}
