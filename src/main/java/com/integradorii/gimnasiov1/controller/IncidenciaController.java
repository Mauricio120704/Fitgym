package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Incidencia;
import com.integradorii.gimnasiov1.repository.IncidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/incidencias")
public class IncidenciaController {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @GetMapping
    public String listarIncidencias(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String buscar,
            Model model) {
        
        // Obtener todas las incidencias de la base de datos
        List<Incidencia> todasIncidencias = incidenciaRepository.findAll();
        
        // Aplicar filtros
        List<Incidencia> incidenciasFiltradas = todasIncidencias;
        
        if (estado != null && !estado.isEmpty() && !"todos".equals(estado)) {
            incidenciasFiltradas = incidenciasFiltradas.stream()
                    .filter(i -> estado.equalsIgnoreCase(i.getEstado()))
                    .collect(Collectors.toList());
        }
        
        if (prioridad != null && !prioridad.isEmpty() && !"todas".equals(prioridad)) {
            incidenciasFiltradas = incidenciasFiltradas.stream()
                    .filter(i -> prioridad.equalsIgnoreCase(i.getPrioridad()))
                    .collect(Collectors.toList());
        }
        
        if (buscar != null && !buscar.trim().isEmpty()) {
            String buscarLower = buscar.toLowerCase().trim();
            incidenciasFiltradas = incidenciasFiltradas.stream()
                    .filter(i -> 
                        i.getTitulo().toLowerCase().contains(buscarLower) ||
                        i.getDescripcion().toLowerCase().contains(buscarLower) ||
                        i.getReportadoPor().toLowerCase().contains(buscarLower)
                    )
                    .collect(Collectors.toList());
        }
        
        // Calcular estadísticas
        long totalIncidencias = todasIncidencias.size();
        long abiertas = todasIncidencias.stream()
                .filter(i -> "ABIERTO".equals(i.getEstado()))
                .count();
        long resueltas = todasIncidencias.stream()
                .filter(i -> "RESUELTO".equals(i.getEstado()))
                .count();
        
        model.addAttribute("incidencias", incidenciasFiltradas);
        model.addAttribute("totalIncidencias", totalIncidencias);
        model.addAttribute("abiertas", abiertas);
        model.addAttribute("resueltas", resueltas);
        model.addAttribute("estadoActual", estado != null ? estado : "todos");
        model.addAttribute("prioridadActual", prioridad != null ? prioridad : "todas");
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        
        return "incidencias";
    }
    
    @PostMapping("/crear")
    @ResponseBody
    public Incidencia crearIncidencia(@RequestBody Incidencia incidencia) {
        // Guardar en la base de datos
        return incidenciaRepository.save(incidencia);
    }
    
    @PutMapping("/{id}/estado")
    @ResponseBody
    public Incidencia actualizarEstado(@PathVariable Long id, @RequestBody String nuevoEstado) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        // Remover comillas del JSON string si existen
        String estado = nuevoEstado.replace("\"", "");
        incidencia.setEstado(estado);
        return incidenciaRepository.save(incidencia);
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    public void eliminarIncidencia(@PathVariable Long id) {
        incidenciaRepository.deleteById(id);
    }
}
