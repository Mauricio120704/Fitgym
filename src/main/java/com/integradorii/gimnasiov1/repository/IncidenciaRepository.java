package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    @Query("select i from Incidencia i where " +
            "(:estado = 'todos' or i.estado = :estadoDb) and " +
            "(:prioridad = 'todas' or i.prioridad = :prioridadDb) and (" +
            " :buscar is null or :buscar = '' or lower(i.titulo) like lower(concat('%', :buscar, '%')) or " +
            " lower(i.descripcion) like lower(concat('%', :buscar, '%')) ) " +
            " order by i.ultimaActualizacion desc")
    List<Incidencia> findFiltered(@Param("estado") String estado,
                                  @Param("estadoDb") String estadoDb,
                                  @Param("prioridad") String prioridad,
                                  @Param("prioridadDb") String prioridadDb,
                                  @Param("buscar") String buscar);

    @Query("select count(i) from Incidencia i")
    long total();

    @Query("select count(i) from Incidencia i where i.estado = 'Abierta'")
    long abiertas();

    @Query("select count(i) from Incidencia i where i.estado = 'Resuelto'")
    long resueltas();
}
