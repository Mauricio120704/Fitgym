package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Miembro;
import com.integradorii.gimnasiov1.service.MiembroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {
    
    @Autowired
    private MiembroService miembroService;
    
    @GetMapping("/")
    public String home() {
        return "redirect:/miembros";
    }
    
    @GetMapping("/miembros")
    public String listarMiembros(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) String buscar,
            Model model) {
        
        java.util.List<Miembro> miembros;
        
        // Aplicar filtros
        if ("activos".equals(filtro)) {
            miembros = miembroService.obtenerMiembrosActivos();
        } else if ("inactivos".equals(filtro)) {
            miembros = miembroService.obtenerTodos().stream()
                    .filter(m -> !m.getMembresiaActiva())
                    .collect(java.util.stream.Collectors.toList());
        } else {
            miembros = miembroService.obtenerTodos();
        }
        
        // Aplicar búsqueda
        if (buscar != null && !buscar.trim().isEmpty()) {
            String buscarLower = buscar.toLowerCase().trim();
            miembros = miembros.stream()
                    .filter(m -> 
                        m.getNombre().toLowerCase().contains(buscarLower) ||
                        m.getApellido().toLowerCase().contains(buscarLower) ||
                        m.getEmail().toLowerCase().contains(buscarLower) ||
                        (m.getTelefono() != null && m.getTelefono().contains(buscarLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Obtener estadísticas totales (sin filtros)
        java.util.List<Miembro> todosMiembros = miembroService.obtenerTodos();
        long totalActivos = todosMiembros.stream().filter(Miembro::getMembresiaActiva).count();
        long totalInactivos = todosMiembros.size() - totalActivos;
        
        model.addAttribute("miembros", miembros);
        model.addAttribute("filtroActual", filtro != null ? filtro : "todos");
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        model.addAttribute("totalMiembros", todosMiembros.size());
        model.addAttribute("totalActivos", totalActivos);
        model.addAttribute("totalInactivos", totalInactivos);
        
        return "miembros";
    }
    
    @GetMapping("/miembros/nuevo")
    public String nuevoMiembroForm(Model model) {
        model.addAttribute("miembro", new Miembro());
        return "nuevo-miembro";
    }
    
    @PostMapping("/miembros")
    public String guardarMiembro(@ModelAttribute Miembro miembro) {
        miembroService.guardar(miembro);
        return "redirect:/miembros";
    }
    
    @GetMapping("/miembros/editar/{id}")
    public String editarMiembroForm(@PathVariable Long id, Model model) {
        Miembro miembro = miembroService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        model.addAttribute("miembro", miembro);
        return "editar-miembro";
    }
    
    @PostMapping("/miembros/{id}")
    public String actualizarMiembro(@PathVariable Long id, @ModelAttribute Miembro miembro) {
        miembro.setId(id);
        miembroService.guardar(miembro);
        return "redirect:/miembros";
    }
    
    @GetMapping("/miembros/eliminar/{id}")
    public String eliminarMiembro(@PathVariable Long id) {
        miembroService.eliminar(id);
        return "redirect:/miembros";
    }
}
