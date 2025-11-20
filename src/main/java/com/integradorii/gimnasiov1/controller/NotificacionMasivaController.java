package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.PlanRepository;
import com.integradorii.gimnasiov1.service.NotificacionMasivaService;
import jakarta.mail.MessagingException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/notificaciones")
public class NotificacionMasivaController {

    private final NotificacionMasivaService notificacionMasivaService;
    private final PersonaRepository personaRepository;
    private final PlanRepository planRepository;

    public NotificacionMasivaController(NotificacionMasivaService notificacionMasivaService,
                                        PersonaRepository personaRepository,
                                        PlanRepository planRepository) {
        this.notificacionMasivaService = notificacionMasivaService;
        this.personaRepository = personaRepository;
        this.planRepository = planRepository;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public String mostrarFormulario(Model model) {
        long totalActivos = personaRepository.countDeportistasActivos();
        long totalInactivos = personaRepository.countDeportistasInactivos();
        long total = totalActivos + totalInactivos;
        model.addAttribute("totalActivos", totalActivos);
        model.addAttribute("totalInactivos", totalInactivos);
        model.addAttribute("totalDeportistas", total);
        model.addAttribute("planes", planRepository.findAll());
        if (!model.containsAttribute("asunto")) {
            model.addAttribute("asunto", "");
        }
        if (!model.containsAttribute("mensaje")) {
            model.addAttribute("mensaje", "");
        }
        if (!model.containsAttribute("filtroPlan")) {
            model.addAttribute("filtroPlan", "TODOS");
        }
        return "admin/notificaciones";
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/enviar")
    public String enviar(
            @RequestParam String asunto,
            @RequestParam String mensaje,
            @RequestParam(name = "filtroPlan", required = false, defaultValue = "TODOS") String filtroPlan,
            RedirectAttributes redirectAttributes
    ) {
        String asuntoTrim = asunto != null ? asunto.trim() : "";
        String mensajeTrim = mensaje != null ? mensaje.trim() : "";
        String filtroPlanTrim = filtroPlan != null ? filtroPlan.trim() : "TODOS";
        if (asuntoTrim.isEmpty() || mensajeTrim.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El asunto y el mensaje son obligatorios");
            redirectAttributes.addFlashAttribute("asunto", asunto);
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
            redirectAttributes.addFlashAttribute("filtroPlan", filtroPlanTrim);
            return "redirect:/admin/notificaciones";
        }
        try {
            int enviados = notificacionMasivaService.enviarNotificacionATodosDeportistas(asuntoTrim, mensajeTrim, filtroPlanTrim);
            if (enviados == 0) {
                redirectAttributes.addFlashAttribute("error", "No se encontraron deportistas con email válido para enviar la notificación");
            } else {
                redirectAttributes.addFlashAttribute("success", "Notificación enviada a " + enviados + " deportistas");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar las notificaciones: " + e.getMessage());
        }
        redirectAttributes.addFlashAttribute("asunto", asuntoTrim);
        redirectAttributes.addFlashAttribute("mensaje", mensajeTrim);
        redirectAttributes.addFlashAttribute("filtroPlan", filtroPlanTrim);
        return "redirect:/admin/notificaciones";
    }
}
