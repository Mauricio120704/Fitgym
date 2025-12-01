package com.integradorii.gimnasiov1.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/suspensiones")
public class AdminSuspensionController {

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public String mostrarSolicitudesSuspension(Model model) {
        model.addAttribute("activeMenu", "suspensiones-admin");
        return "suspension_admin";
    }
}
