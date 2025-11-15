package com.integradorii.gimnasiov1.config;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Suscripcion;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;
import java.time.format.DateTimeFormatter;

@ControllerAdvice(annotations = Controller.class)
public class SuscripcionModelAdvice {

    private final PersonaRepository personaRepository;
    private final SuscripcionRepository suscripcionRepository;

    public SuscripcionModelAdvice(PersonaRepository personaRepository,
                                  SuscripcionRepository suscripcionRepository) {
        this.personaRepository = personaRepository;
        this.suscripcionRepository = suscripcionRepository;
    }

    @ModelAttribute
    public void addSuscripcionInfo(Model model,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        // Valor por defecto para evitar problemas de variables inexistentes en Thymeleaf
        model.addAttribute("tieneSuscripcionActiva", false);
        model.addAttribute("suscripcionActiva", null);

        if (userDetails == null) {
            return;
        }

        String email = userDetails.getUsername();
        if (email == null || email.isBlank()) {
            return;
        }

        Optional<Persona> personaOpt = personaRepository.findByEmail(email);
        if (personaOpt.isEmpty()) {
            return; // Aplica solo a deportistas (tabla personas)
        }

        Persona persona = personaOpt.get();
        Optional<Suscripcion> suscripcionOpt = suscripcionRepository.findActiveByDeportistaId(persona.getId());

        if (suscripcionOpt.isPresent()) {
            Suscripcion s = suscripcionOpt.get();
            model.addAttribute("tieneSuscripcionActiva", true);
            model.addAttribute("suscripcionActiva", s);

            // Valores ya formateados para mostrar en el perfil del deportista
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            String nombrePlanLegible;
            if (s.getPlan() != null && s.getPlan().getNombre() != null && !s.getPlan().getNombre().isBlank()) {
                nombrePlanLegible = "Plan " + s.getPlan().getNombre();
            } else {
                nombrePlanLegible = "Sin plan";
            }

            String fechaInicioStr = s.getFechaInicio() != null ? s.getFechaInicio().format(formatter) : "—";
            String fechaFinStr = s.getFechaFin() != null ? s.getFechaFin().format(formatter) : "—";
            String proximoPagoStr = s.getProximoPago() != null ? s.getProximoPago().format(formatter) : "—";

            model.addAttribute("suscripcionNombrePlanLegible", nombrePlanLegible);
            model.addAttribute("suscripcionFechaInicioStr", fechaInicioStr);
            model.addAttribute("suscripcionFechaFinStr", fechaFinStr);
            model.addAttribute("suscripcionProximoPagoStr", proximoPagoStr);
        }
    }
}
