package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.dto.ClaseViewDTO;
import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.repository.ReservaClaseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class ClaseViewService {

    private final ReservaClaseRepository reservaClaseRepository;

    public ClaseViewService(ReservaClaseRepository reservaClaseRepository) {
        this.reservaClaseRepository = reservaClaseRepository;
    }

    public ClaseViewDTO toView(Clase c) {
    String instructor = c.getEntrenador() != null
            ? ((c.getEntrenador().getNombre() == null ? "" : c.getEntrenador().getNombre()) +
               (c.getEntrenador().getApellido() == null ? "" : (" " + c.getEntrenador().getApellido()))).trim()
            : "";

    // Convertir la fecha a la zona horaria del sistema
    LocalDate fecha = null;
    LocalTime hora = null;
    
    if (c.getFecha() != null) {
        // Convertir a la zona horaria del sistema
        ZonedDateTime zdt = c.getFecha().atZoneSameInstant(ZoneId.systemDefault());
        fecha = zdt.toLocalDate();
        hora = zdt.toLocalTime();
        System.out.println("Hora convertida en toView: " + zdt);
    }

    int cuposPremium = c.getCapacidad();
    int cuposElite = 0;

    long ocupados = c.getId() == null ? 0 : reservaClaseRepository.countOcupados(c.getId());
    long ocupadosPremium = Math.min(ocupados, cuposPremium);
    long ocupadosElite = 0L;

    Long tipoClaseId = (c.getTipoClase() != null) ? c.getTipoClase().getId() : null;
    String tipoClaseNombre = (c.getTipoClase() != null) ? c.getTipoClase().getNombre() : null;

    return new ClaseViewDTO(
            c.getId(),
            c.getNombre(),
            instructor,
            fecha,
            hora,
            c.getDuracionMinutos(),
            cuposPremium,
            cuposElite,
            ocupadosPremium,
            ocupadosElite,
            tipoClaseId,
            tipoClaseNombre
    );
    }
}
