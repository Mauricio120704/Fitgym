package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.ReservaClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaClaseRepository extends JpaRepository<ReservaClase, Long> {

    @Query("select count(r) from ReservaClase r where r.clase.id = :claseId and r.estado <> 'Cancelado'")
    long countOcupados(@Param("claseId") Long claseId);

    void deleteByClase_Id(Long claseId);
}
