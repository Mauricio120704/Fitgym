package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ClaseRepository extends JpaRepository<Clase, Long> {
    List<Clase> findAllByOrderByFechaAsc();
    List<Clase> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrderByFechaAsc(String nombre, String descripcion);
    List<Clase> findByFechaAfterOrderByFechaAsc(OffsetDateTime fecha);

    // Clases en un día (entre [inicio, fin))
    List<Clase> findByFechaBetweenOrderByFechaAsc(OffsetDateTime inicio, OffsetDateTime fin);

    // Clases por día filtrando por 'tipo' (usamos nombre que contenga el tipo)
    @Query("select c from Clase c where c.fecha between :inicio and :fin and (lower(c.nombre) like lower(concat('%', :tipo, '%')) or lower(coalesce(c.descripcion,'')) like lower(concat('%', :tipo, '%'))) order by c.fecha asc")
    List<Clase> findByFechaBetweenAndTipoLike(@Param("inicio") OffsetDateTime inicio,
                                              @Param("fin") OffsetDateTime fin,
                                              @Param("tipo") String tipo);
}
