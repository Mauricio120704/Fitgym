package com.integradorii.gimnasiov1.controller;

// DEPRECATED: Legacy in-memory ClaseController. Disabled to avoid conflicts with
// the DB-backed ClasesController. Kept only for reference; no Spring annotations.

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClaseController {

    // Datos en memoria - simulando base de datos
    private static List<Map<String, Object>> clasesEnMemoria = new ArrayList<>();
    private static Long nextId = 1L;
    
    // Inicializar datos de ejemplo
    static {
        inicializarDatosEjemplo();
    }
    
    private static void inicializarDatosEjemplo() {
        Map<String, Object> clase1 = new HashMap<>();
        clase1.put("id", nextId++);
        clase1.put("nombre", "Yoga Matutino");
        clase1.put("instructor", "Ana García");
        clase1.put("fecha", LocalDate.now().plusDays(1));
        clase1.put("hora", LocalTime.of(7, 0));
        clase1.put("duracion", 60);
        clase1.put("cuposPremium", 10);
        clase1.put("cuposElite", 5);
        clase1.put("ocupadosPremium", 8);
        clase1.put("ocupadosElite", 3);
        
        Map<String, Object> clase2 = new HashMap<>();
        clase2.put("id", nextId++);
        clase2.put("nombre", "CrossFit Intenso");
        clase2.put("instructor", "Carlos Ruiz");
        clase2.put("fecha", LocalDate.now().plusDays(1));
        clase2.put("hora", LocalTime.of(18, 0));
        clase2.put("duracion", 90);
        clase2.put("cuposPremium", 8);
        clase2.put("cuposElite", 4);
        clase2.put("ocupadosPremium", 8);
        clase2.put("ocupadosElite", 4);
        
        Map<String, Object> clase3 = new HashMap<>();
        clase3.put("id", nextId++);
        clase3.put("nombre", "Pilates");
        clase3.put("instructor", "María López");
        clase3.put("fecha", LocalDate.now().plusDays(2));
        clase3.put("hora", LocalTime.of(10, 0));
        clase3.put("duracion", 60);
        clase3.put("cuposPremium", 12);
        clase3.put("cuposElite", 6);
        clase3.put("ocupadosPremium", 5);
        clase3.put("ocupadosElite", 2);
        
        Map<String, Object> clase4 = new HashMap<>();
        clase4.put("id", nextId++);
        clase4.put("nombre", "Spinning");
        clase4.put("instructor", "Pedro Sánchez");
        clase4.put("fecha", LocalDate.now().plusDays(2));
        clase4.put("hora", LocalTime.of(19, 0));
        clase4.put("duracion", 45);
        clase4.put("cuposPremium", 15);
        clase4.put("cuposElite", 5);
        clase4.put("ocupadosPremium", 12);
        clase4.put("ocupadosElite", 4);
        
        clasesEnMemoria.add(clase1);
        clasesEnMemoria.add(clase2);
        clasesEnMemoria.add(clase3);
        clasesEnMemoria.add(clase4);
    }

    @GetMapping
    public String listarClases(
            @RequestParam(required = false) String buscar,
            Model model) {
        
        // Obtener todas las clases
        List<Map<String, Object>> todasClases = new ArrayList<>(clasesEnMemoria);
        
        // Aplicar búsqueda
        List<Map<String, Object>> clasesFiltradas = todasClases;
        if (buscar != null && !buscar.trim().isEmpty()) {
            String buscarLower = buscar.toLowerCase().trim();
            clasesFiltradas = todasClases.stream()
                    .filter(c -> 
                        ((String) c.get("nombre")).toLowerCase().contains(buscarLower) ||
                        ((String) c.get("instructor")).toLowerCase().contains(buscarLower)
                    )
                    .collect(Collectors.toList());
        }
        
        // Calcular estadísticas
        long totalClases = todasClases.size();
        long clasesLlenas = todasClases.stream()
                .filter(c -> {
                    int totalOcupados = (int) c.get("ocupadosPremium") + (int) c.get("ocupadosElite");
                    int totalCupos = (int) c.get("cuposPremium") + (int) c.get("cuposElite");
                    return totalOcupados >= totalCupos;
                })
                .count();
        
        int totalCupos = todasClases.stream()
                .mapToInt(c -> (int) c.get("cuposPremium") + (int) c.get("cuposElite"))
                .sum();
        
        model.addAttribute("clases", clasesFiltradas);
        model.addAttribute("totalClases", totalClases);
        model.addAttribute("clasesLlenas", clasesLlenas);
        model.addAttribute("totalCupos", totalCupos);
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        
        return "clases";
    }
    
    @PostMapping("/crear")
    @ResponseBody
    public Map<String, Object> crearClase(@RequestBody Map<String, Object> clase) {
        // Convertir fecha y hora de String a LocalDate/LocalTime si es necesario
        if (clase.get("fecha") instanceof String) {
            clase.put("fecha", LocalDate.parse((String) clase.get("fecha")));
        }
        if (clase.get("hora") instanceof String) {
            clase.put("hora", LocalTime.parse((String) clase.get("hora")));
        }
        
        // Agregar ID y guardar en memoria
        clase.put("id", nextId++);
        clasesEnMemoria.add(clase);
        return clase;
    }
    
    @PutMapping("/{id}")
    @ResponseBody
    public Map<String, Object> actualizarClase(@PathVariable Long id, @RequestBody Map<String, Object> claseActualizada) {
        // Convertir fecha y hora de String a LocalDate/LocalTime si es necesario
        if (claseActualizada.get("fecha") instanceof String) {
            claseActualizada.put("fecha", LocalDate.parse((String) claseActualizada.get("fecha")));
        }
        if (claseActualizada.get("hora") instanceof String) {
            claseActualizada.put("hora", LocalTime.parse((String) claseActualizada.get("hora")));
        }
        
        // Buscar y actualizar clase
        for (Map<String, Object> clase : clasesEnMemoria) {
            if (clase.get("id").equals(id)) {
                clase.putAll(claseActualizada);
                clase.put("id", id); // Mantener el ID original
                return clase;
            }
        }
        throw new RuntimeException("Clase no encontrada con id: " + id);
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    public void eliminarClase(@PathVariable Long id) {
        clasesEnMemoria.removeIf(c -> c.get("id").equals(id));
    }
}
