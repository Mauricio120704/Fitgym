package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.PromocionHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromocionHistorialRepository extends JpaRepository<PromocionHistorial, Long> {
    List<PromocionHistorial> findByPromocionIdOrderByRealizadoEnDesc(Long promocionId);

    @Query("select h from PromocionHistorial h join fetch h.promocion p order by h.realizadoEn desc")
    List<PromocionHistorial> findAllWithPromocionOrderByRealizadoEnDesc();
}
