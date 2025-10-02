package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    Optional<Suscripcion> findFirstByDeportista_EmailAndEstadoOrderByProximoPagoAsc(String email, String estado);
}
