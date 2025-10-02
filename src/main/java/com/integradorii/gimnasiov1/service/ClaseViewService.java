package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.dto.ClaseViewDTO;
import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.repository.ReservaClaseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

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

        LocalDate fecha = c.getFecha() != null ? c.getFecha().toLocalDate() : null;
        LocalTime hora = c.getFecha() != null ? c.getFecha().toLocalTime() : null;

        int cuposPremium = c.getCapacidad();
        int cuposElite = 0; // Por ahora UI separa pero BD no; mantenemos 0 para Elite

        long ocupados = c.getId() == null ? 0 : reservaClaseRepository.countOcupados(c.getId());
        long ocupadosPremium = Math.min(ocupados, cuposPremium);
        long ocupadosElite = 0;

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
                ocupadosElite
        );
    }
}
