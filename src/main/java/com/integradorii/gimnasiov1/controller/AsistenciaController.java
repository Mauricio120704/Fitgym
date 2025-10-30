package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.dto.AsistenciaDTO;
import com.integradorii.gimnasiov1.service.AsistenciaService;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Asistencia - Registro de entrada de deportistas
 * Ruta: /asistencia | Acceso: ADMIN, RECEPCIONISTA
 * Tabla: asistencias, personas
 * Busca por DNI y registra entrada/salida
 */
@Controller
@RequestMapping("/asistencia")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    // GET /asistencia - Muestra página de control de asistencia
    @GetMapping
    public String mostrarPaginaAsistencia() {
        return "asistencia";
    }

    /**
     * GET /asistencia/buscar?dni=xxx - Busca deportista por DNI
     * Retorna: AsistenciaDTO con datos del deportista y última asistencia
     */
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<?> buscarPorDni(@RequestParam String dni) {
        try {
            AsistenciaDTO datos = asistenciaService.buscarDatosAsistenciaPorDni(dni);
            if (datos != null) {
                return ResponseEntity.ok(datos);
            } else {
                return ResponseEntity.status(404).body("No se encontró un deportista con el DNI proporcionado");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    /**
     * POST /asistencia/registrar - Registra entrada/salida de deportista
     * Alterna automáticamente entre entrada y salida
     */
    @PostMapping("/registrar")
    @ResponseBody
    public ResponseEntity<?> registrarAsistencia(@RequestParam String dni) {
        try {
            Map<String, Object> resultado = asistenciaService.registrarAsistencia(dni);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Error al registrar la asistencia: " + e.getMessage()));
        }
    }

    /**
     * GET /asistencia/recientes - Obtiene últimas 10 asistencias registradas
     * Para mostrar en panel de control
     */
    @GetMapping("/recientes")
    @ResponseBody
    public ResponseEntity<?> obtenerAsistenciasRecientes() {
        try {
            return ResponseEntity.ok(asistenciaService.obtenerAsistenciasRecientes());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener el historial de asistencias: " + e.getMessage());
        }
    }
}
