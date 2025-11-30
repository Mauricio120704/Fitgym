package com.integradorii.gimnasiov1.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de Lockers - Gestión de casilleros del gimnasio
 * Ruta: /lockers | Acceso: ADMIN, RECEPCIONISTA
 * API REST adicional en api/LockerApiController
 */
@Controller
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
public class LockersController {

    // GET /lockers - Página de gestión de lockers
    @GetMapping("/lockers")
    public String lockersPage(org.springframework.ui.Model model) {
        model.addAttribute("activeMenu", "lockers");
        return "lockers";
    }
}
