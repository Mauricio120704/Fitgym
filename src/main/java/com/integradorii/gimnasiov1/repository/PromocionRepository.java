package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    List<Promocion> findByEstadoIn(List<Promocion.Estado> estados);
    List<Promocion> findByEstado(Promocion.Estado estado);
    List<Promocion> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String nombre, String descripcion);
}
