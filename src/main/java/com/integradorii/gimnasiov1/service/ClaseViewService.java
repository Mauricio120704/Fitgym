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

    Long instructorId = c.getEntrenador() != null ? c.getEntrenador().getId() : null;

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

    // Obtener cupos por membresía desde la entidad (persistidos en BD)
    int cuposBasico = c.getCuposBasico() != null ? c.getCuposBasico() : 0;
    int cuposPremium = c.getCuposPremium() != null ? c.getCuposPremium() : 0;
    int cuposElite = c.getCuposElite() != null ? c.getCuposElite() : 0;

    // Contar ocupados totales
    long ocupados = c.getId() == null ? 0 : reservaClaseRepository.countOcupados(c.getId());

    // Distribuir ocupados: primero Básico, luego Premium y finalmente Elite
    long ocupadosBasico = Math.min(ocupados, cuposBasico);
    long restantes = ocupados - ocupadosBasico;
    long ocupadosPremium = Math.min(restantes, cuposPremium);
    long ocupadosElite = Math.max(0, restantes - cuposPremium);

    Long tipoClaseId = (c.getTipoClase() != null) ? c.getTipoClase().getId() : null;
    String tipoClaseNombre = (c.getTipoClase() != null) ? c.getTipoClase().getNombre() : null;

    return new ClaseViewDTO(
            c.getId(),
            c.getNombre(),
            instructor,
            instructorId,
            fecha,
            hora,
            c.getDuracionMinutos(),
            cuposPremium,
            cuposElite,
            ocupadosBasico,
            ocupadosPremium,
            ocupadosElite,
            tipoClaseId,
            tipoClaseNombre,
            c.getCuposBasico(),
            c.getEsPago(),
            c.getParaTodos(),
            c.getPrecio()
    );
    }
}
