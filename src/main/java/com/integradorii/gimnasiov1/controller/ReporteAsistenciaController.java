package com.integradorii.gimnasiov1.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para el reporte de asistencia a clases
 * Ruta: /reportes/asistencia-clases
 */
@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ReporteAsistenciaController {

    /**
     * Muestra la página principal del reporte de asistencia
     */
    @GetMapping("/asistencia-clases")
    public String mostrarReporteAsistencia() {
        return "reportes/asistencia";
    }
}
