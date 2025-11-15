package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Promocion;
import com.integradorii.gimnasiov1.repository.PromocionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/deportista")
public class PromocionesDeportistaController {

    private final PromocionRepository promocionRepository;

    public PromocionesDeportistaController(PromocionRepository promocionRepository) {
        this.promocionRepository = promocionRepository;
    }

    @GetMapping("/promociones")
    public String promocionesVigentes(Model model) {
        LocalDate hoy = LocalDate.now();
        List<Promocion> base = promocionRepository.findByEstado(Promocion.Estado.ACTIVE);
        List<Promocion> vigentes = base.stream()
                .filter(p -> (p.getFechaInicio() == null || !p.getFechaInicio().isAfter(hoy))
                        && (p.getFechaFin() == null || !p.getFechaFin().isBefore(hoy)))
                .collect(Collectors.toList());

        List<Promocion> promocionesMembresia = vigentes.stream()
                .filter(p -> p.getMembresias() != null && !p.getMembresias().isEmpty())
                .collect(Collectors.toList());

        List<Promocion> promocionesClases = vigentes.stream()
                .filter(p -> p.getMembresias() == null || p.getMembresias().isEmpty())
                .collect(Collectors.toList());

        model.addAttribute("promociones", vigentes);
        model.addAttribute("promocionesMembresia", promocionesMembresia);
        model.addAttribute("promocionesClases", promocionesClases);
        model.addAttribute("hoy", hoy);
        model.addAttribute("activeMenu", "promociones-cliente");
        return "deportista/promociones";
    }
}
