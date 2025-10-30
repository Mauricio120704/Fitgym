package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.ReservaClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReservaClaseRepository extends JpaRepository<ReservaClase, Long> {

    @Query("select count(r) from ReservaClase r where r.clase.id = :claseId and r.estado <> 'Cancelado'")
    long countOcupados(@Param("claseId") Long claseId);

    void deleteByClase_Id(Long claseId);

    // Próximas reservas del deportista (no canceladas)
    @Query("select r from ReservaClase r where r.deportista.id = :deportistaId and r.clase.fecha > :desde and r.estado <> 'Cancelado' order by r.clase.fecha asc")
    List<ReservaClase> findProximasByDeportista(@Param("deportistaId") Long deportistaId,
                                                @Param("desde") OffsetDateTime desde);

    boolean existsByClase_IdAndDeportista_IdAndEstadoNot(Long claseId, Long deportistaId, String estadoExcluido);
}
