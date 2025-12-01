package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.SuspensionMembresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuspensionMembresiaRepository extends JpaRepository<SuspensionMembresia, Long> {

    List<SuspensionMembresia> findByUsuario_Id(Long usuarioId);

    List<SuspensionMembresia> findBySuscripcion_Id(Long suscripcionId);

    List<SuspensionMembresia> findByEstadoIgnoreCase(String estado);

    Optional<SuspensionMembresia> findTopByUsuario_IdOrderByFechaCreacionDesc(Long usuarioId);
}
