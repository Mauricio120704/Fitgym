package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Suscripcion;
import com.integradorii.gimnasiov1.model.SuspensionMembresia;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import com.integradorii.gimnasiov1.repository.SuspensionMembresiaRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/suspension")
public class SuspensionController {

    private final PersonaRepository personaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final SuspensionMembresiaRepository suspensionRepository;

    public SuspensionController(PersonaRepository personaRepository,
                                SuscripcionRepository suscripcionRepository,
                                SuspensionMembresiaRepository suspensionRepository) {
        this.personaRepository = personaRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.suspensionRepository = suspensionRepository;
    }

    @GetMapping
    public String mostrarFormulario(Model model,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String email = userDetails.getUsername();
        Persona persona = personaRepository.findByEmail(email).orElse(null);
        if (persona == null) {
            return "redirect:/login";
        }

        Suscripcion suscripcion = suscripcionRepository
                .findActiveByDeportistaId(persona.getId())
                .orElse(null);
        if (suscripcion == null) {
            // Sin suscripción activa no tiene sentido suspender
            return "redirect:/perfil";
        }

        return "suspension";
    }

    @PostMapping
    @Transactional
    public String solicitarSuspension(@RequestParam("motivo") String motivo,
                                      @RequestParam("fecha_inicio") String fechaInicioStr,
                                      @RequestParam("fecha_fin") String fechaFinStr,
                                      @RequestParam(name = "detalles", required = false) String detalles,
                                      Model model,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String email = userDetails.getUsername();
        Persona persona = personaRepository.findByEmail(email).orElse(null);
        if (persona == null) {
            return "redirect:/login";
        }

        Suscripcion suscripcion = suscripcionRepository
                .findActiveByDeportistaId(persona.getId())
                .orElse(null);
        if (suscripcion == null) {
            model.addAttribute("error", "No tienes una suscripción activa para suspender.");
            return "suspension";
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicio;
        LocalDate fechaFin;
        try {
            fechaInicio = LocalDate.parse(fechaInicioStr);
            fechaFin = LocalDate.parse(fechaFinStr);
        } catch (Exception e) {
            model.addAttribute("error", "Las fechas ingresadas no son válidas.");
            return "suspension";
        }

        // Validaciones básicas de fechas
        if (fechaInicio.isBefore(hoy)) {
            model.addAttribute("error", "La fecha de inicio no puede ser anterior a hoy.");
            return "suspension";
        }

        if (!fechaFin.isAfter(fechaInicio)) {
            model.addAttribute("error", "La fecha de fin debe ser posterior a la fecha de inicio.");
            return "suspension";
        }

        long diasSuspension = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        if (diasSuspension < 3) {
            model.addAttribute("error", "El rango mínimo de suspensión es de 3 días.");
            return "suspension";
        }

        // Buscar suspensiones previas del usuario
        List<SuspensionMembresia> suspensionesUsuario = suspensionRepository.findByUsuario_Id(persona.getId());

        // Regla: no debe tener una suspensión actualmente activa
        boolean tieneActiva = suspensionesUsuario.stream().anyMatch(s -> {
            LocalDate ini = s.getFechaInicio();
            LocalDate fin = s.getFechaFin();
            return !hoy.isBefore(ini) && !hoy.isAfter(fin)
                    && ("aprobada".equalsIgnoreCase(s.getEstado())
                        || "activa".equalsIgnoreCase(s.getEstado()));
        });

        if (tieneActiva) {
            model.addAttribute("error", "Ya tienes una suspensión activa de tu membresía.");
            return "suspension";
        }

        // Regla: máximo 30 días acumulados por año
        int year = fechaInicio.getYear();
        long diasUsadosAnio = suspensionesUsuario.stream()
                .filter(s -> {
                    LocalDate ini = s.getFechaInicio();
                    return ini != null && ini.getYear() == year
                            && ("aprobada".equalsIgnoreCase(s.getEstado())
                                || "activa".equalsIgnoreCase(s.getEstado()));
                })
                .mapToLong(s -> ChronoUnit.DAYS.between(s.getFechaInicio(), s.getFechaFin()) + 1)
                .sum();

        if (diasUsadosAnio + diasSuspension > 30) {
            model.addAttribute("error", "No puedes superar los 30 días de suspensión acumulados por año.");
            return "suspension";
        }

        // Motivo completo (tipo + detalles)
        String motivoFinal = motivo;
        if (detalles != null && !detalles.isBlank()) {
            motivoFinal = motivo + " - " + detalles.trim();
        }

        // Crear suspensión (auto aprobada)
        SuspensionMembresia suspension = new SuspensionMembresia();
        suspension.setSuscripcion(suscripcion);
        suspension.setUsuario(persona);
        suspension.setFechaInicio(fechaInicio);
        suspension.setFechaFin(fechaFin);
        suspension.setMotivo(motivoFinal);
        suspension.setEstado("aprobada");
        suspensionRepository.save(suspension);

        // Extender fechas de suscripción según días suspendidos
        if (suscripcion.getFechaFin() != null) {
            suscripcion.setFechaFin(suscripcion.getFechaFin().plusDays(diasSuspension));
        }
        if (suscripcion.getProximoPago() != null) {
            suscripcion.setProximoPago(suscripcion.getProximoPago().plusDays(diasSuspension));
        }
        suscripcionRepository.save(suscripcion);

        // Redirigir al perfil del deportista
        return "redirect:/perfil";
    }
}
