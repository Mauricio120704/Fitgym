package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.NotificacionMasiva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacionMasivaRepository extends JpaRepository<NotificacionMasiva, Long> {

    List<NotificacionMasiva> findByFechaEnvioAfterOrderByFechaEnvioDesc(LocalDateTime fechaEnvio);
}
