package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.repository.ClaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/clases")
public class ClaseController {

    @Autowired
    private ClaseRepository claseRepository;

    @GetMapping
    public String listarClases(
            @RequestParam(required = false) String buscar,
            Model model) {
        
        // Obtener todas las clases de la base de datos
        List<Clase> todasClases = claseRepository.findAll();
        
        // Aplicar búsqueda
        List<Clase> clasesFiltradas = todasClases;
        if (buscar != null && !buscar.trim().isEmpty()) {
            String buscarLower = buscar.toLowerCase().trim();
            clasesFiltradas = todasClases.stream()
                    .filter(c -> 
                        c.getNombre().toLowerCase().contains(buscarLower) ||
                        c.getInstructor().toLowerCase().contains(buscarLower)
                    )
                    .collect(Collectors.toList());
        }
        
        // Calcular estadísticas
        long totalClases = todasClases.size();
        long clasesLlenas = todasClases.stream()
                .filter(c -> {
                    int totalOcupados = c.getOcupadosPremium() + c.getOcupadosElite();
                    int totalCupos = c.getCuposPremium() + c.getCuposElite();
                    return totalOcupados >= totalCupos;
                })
                .count();
        
        int totalCupos = todasClases.stream()
                .mapToInt(c -> c.getCuposPremium() + c.getCuposElite())
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
    public Clase crearClase(@RequestBody Clase clase) {
        return claseRepository.save(clase);
    }
    
    @PutMapping("/{id}")
    @ResponseBody
    public Clase actualizarClase(@PathVariable Long id, @RequestBody Clase claseActualizada) {
        return claseRepository.findById(id)
                .map(clase -> {
                    clase.setNombre(claseActualizada.getNombre());
                    clase.setInstructor(claseActualizada.getInstructor());
                    clase.setFecha(claseActualizada.getFecha());
                    clase.setHora(claseActualizada.getHora());
                    clase.setDuracion(claseActualizada.getDuracion());
                    clase.setCuposPremium(claseActualizada.getCuposPremium());
                    clase.setCuposElite(claseActualizada.getCuposElite());
                    return claseRepository.save(clase);
                })
                .orElseThrow(() -> new RuntimeException("Clase no encontrada con id: " + id));
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    public void eliminarClase(@PathVariable Long id) {
        claseRepository.deleteById(id);
    }
}
