package com.integradorii.gimnasiov1.controller;

// DEPRECATED: Controlador legacy en memoria. Deshabilitado para evitar
// conflicto con el controlador basado en BD (EvaluacionesController).
// No incluye anotaciones de Spring para no registrarse como bean.

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EvaluacionController {
    
    // Datos en memoria - simulando base de datos
    private static List<Map<String, Object>> evaluacionesEnMemoria = new ArrayList<>();
    private static Long nextEvaluacionId = 1L;
    
    // Inicializar datos de ejemplo
    static {
        inicializarDatosEjemplo();
    }
    
    private static void inicializarDatosEjemplo() {
        // Evaluación 1
        Map<String, Object> evaluacion1 = new HashMap<>();
        evaluacion1.put("id", nextEvaluacionId++);
        evaluacion1.put("usuarioId", 1L);
        evaluacion1.put("fecha", LocalDate.of(2025, 9, 15));
        evaluacion1.put("peso", 72.5);
        evaluacion1.put("grasaCorporal", 15.2);
        evaluacion1.put("masaMuscular", 58.1);
        evaluacion1.put("imc", 22.8);
        evaluacion1.put("notas", "Progreso excelente");
        evaluacionesEnMemoria.add(evaluacion1);
        
        // Evaluación 2
        Map<String, Object> evaluacion2 = new HashMap<>();
        evaluacion2.put("id", nextEvaluacionId++);
        evaluacion2.put("usuarioId", 1L);
        evaluacion2.put("fecha", LocalDate.of(2025, 8, 15));
        evaluacion2.put("peso", 74.2);
        evaluacion2.put("grasaCorporal", 16.8);
        evaluacion2.put("masaMuscular", 57.3);
        evaluacion2.put("imc", 23.4);
        evaluacion2.put("notas", "Buen avance");
        evaluacionesEnMemoria.add(evaluacion2);
        
        // Evaluación 3
        Map<String, Object> evaluacion3 = new HashMap<>();
        evaluacion3.put("id", nextEvaluacionId++);
        evaluacion3.put("usuarioId", 1L);
        evaluacion3.put("fecha", LocalDate.of(2025, 7, 15));
        evaluacion3.put("peso", 75.8);
        evaluacion3.put("grasaCorporal", 18.1);
        evaluacion3.put("masaMuscular", 56.8);
        evaluacion3.put("imc", 23.9);
        evaluacion3.put("notas", "Inicio del programa");
        evaluacionesEnMemoria.add(evaluacion3);
    }
    
    @GetMapping
    public String listarEvaluaciones(Model model) {
        // Obtener evaluaciones del usuario (simulado - usuario ID 1)
        Long usuarioId = 1L;
        
        List<Map<String, Object>> evaluaciones = evaluacionesEnMemoria.stream()
                .filter(e -> e.get("usuarioId").equals(usuarioId))
                .sorted((e1, e2) -> ((LocalDate) e2.get("fecha")).compareTo((LocalDate) e1.get("fecha")))
                .collect(Collectors.toList());
        
        // Calcular estadísticas
        Map<String, Object> estadisticas = calcularEstadisticas(evaluaciones);
        
        model.addAttribute("evaluaciones", evaluaciones);
        model.addAttribute("estadisticas", estadisticas);
        
        return "evaluaciones";
    }
    
    @PostMapping("/agregar")
    @ResponseBody
    public Map<String, Object> agregarEvaluacion(
            @RequestParam Double peso,
            @RequestParam Double grasaCorporal,
            @RequestParam Double masaMuscular,
            @RequestParam(required = false) String notas) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Calcular IMC (asumiendo altura de 1.75m para el ejemplo)
            double altura = 1.75;
            double imc = peso / (altura * altura);
            
            // Crear nueva evaluación
            Map<String, Object> nuevaEvaluacion = new HashMap<>();
            nuevaEvaluacion.put("id", nextEvaluacionId++);
            nuevaEvaluacion.put("usuarioId", 1L);
            nuevaEvaluacion.put("fecha", LocalDate.now());
            nuevaEvaluacion.put("peso", peso);
            nuevaEvaluacion.put("grasaCorporal", grasaCorporal);
            nuevaEvaluacion.put("masaMuscular", masaMuscular);
            nuevaEvaluacion.put("imc", Math.round(imc * 10.0) / 10.0);
            nuevaEvaluacion.put("notas", notas != null ? notas : "");
            
            evaluacionesEnMemoria.add(nuevaEvaluacion);
            
            response.put("success", true);
            response.put("message", "Evaluación agregada exitosamente");
            response.put("evaluacion", nuevaEvaluacion);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al agregar evaluación: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminarEvaluacion(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean eliminado = evaluacionesEnMemoria.removeIf(e -> e.get("id").equals(id));
            
            if (eliminado) {
                response.put("success", true);
                response.put("message", "Evaluación eliminada exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "Evaluación no encontrada");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar evaluación: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/estadisticas")
    @ResponseBody
    public Map<String, Object> obtenerEstadisticas() {
        Long usuarioId = 1L;
        
        List<Map<String, Object>> evaluaciones = evaluacionesEnMemoria.stream()
                .filter(e -> e.get("usuarioId").equals(usuarioId))
                .sorted((e1, e2) -> ((LocalDate) e2.get("fecha")).compareTo((LocalDate) e1.get("fecha")))
                .collect(Collectors.toList());
        
        return calcularEstadisticas(evaluaciones);
    }
    
    private Map<String, Object> calcularEstadisticas(List<Map<String, Object>> evaluaciones) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        if (evaluaciones.isEmpty()) {
            estadisticas.put("totalEvaluaciones", 0);
            estadisticas.put("pesoActual", 0.0);
            estadisticas.put("pesoInicial", 0.0);
            estadisticas.put("diferenciaPeso", 0.0);
            estadisticas.put("grasaActual", 0.0);
            estadisticas.put("musculoActual", 0.0);
            return estadisticas;
        }
        
        Map<String, Object> evaluacionReciente = evaluaciones.get(0);
        Map<String, Object> evaluacionInicial = evaluaciones.get(evaluaciones.size() - 1);
        
        double pesoActual = (Double) evaluacionReciente.get("peso");
        double pesoInicial = (Double) evaluacionInicial.get("peso");
        double diferenciaPeso = pesoActual - pesoInicial;
        
        estadisticas.put("totalEvaluaciones", evaluaciones.size());
        estadisticas.put("pesoActual", pesoActual);
        estadisticas.put("pesoInicial", pesoInicial);
        estadisticas.put("diferenciaPeso", Math.round(diferenciaPeso * 10.0) / 10.0);
        estadisticas.put("grasaActual", evaluacionReciente.get("grasaCorporal"));
        estadisticas.put("musculoActual", evaluacionReciente.get("masaMuscular"));
        estadisticas.put("imcActual", evaluacionReciente.get("imc"));
        
        return estadisticas;
    }
}
