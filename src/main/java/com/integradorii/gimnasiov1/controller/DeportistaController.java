package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.repository.PersonaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Controlador de Deportista - Panel del deportista
 * Ruta: /deportista/** | Acceso: ROLE_CLIENTE
 * Tabla: personas
 */
@Controller
public class DeportistaController {

    private final PersonaRepository personaRepository;

    public DeportistaController(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    /**
     * GET /deportista/mis-clases - Vista de clases del deportista
     * Muestra clases reservadas y disponibles
     */
    @GetMapping("/deportista/mis-clases")
    public String misClases(Model model, Principal principal) {
        if (principal != null) {
            // Obtener ID del deportista autenticado
            personaRepository.findByEmail(principal.getName()).ifPresent(p ->
                    model.addAttribute("deportistaId", p.getId())
            );
        }
        // Marcar menú activo en sidebar
        model.addAttribute("activeMenu", "mis-clases");
        return "deportista/mis-clases";
    }
}
