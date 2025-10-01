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
        return "index";
    }
    
    @GetMapping("/inicio")
    public String inicio() {
        return "index";
    }
    
    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }
    
    @GetMapping("/planes")
    public String planes() {
        return "planes";
    }
    
    @GetMapping("/checkout")
    public String checkout(
            @RequestParam(required = false, defaultValue = "Básico") String plan,
            @RequestParam(required = false, defaultValue = "mensual") String periodo,
            @RequestParam(required = false, defaultValue = "49.900") String precio,
            Model model) {
        model.addAttribute("planNombre", plan);
        model.addAttribute("periodo", periodo);
        model.addAttribute("precio", precio);
        return "checkout";
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
        if (miembro.getFechaRegistro() == null) {
            miembro.setFechaRegistro(java.time.LocalDate.now());
        }
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
    
    @GetMapping("/perfil")
    public String perfilUsuario(Model model) {
        // Datos de ejemplo estáticos - NO conecta a la base de datos
        Miembro miembroEjemplo = new Miembro();
        miembroEjemplo.setId(1L);
        miembroEjemplo.setNombre("Juan");
        miembroEjemplo.setApellido("Pérez");
        miembroEjemplo.setEmail("juan.perez@email.com");
        miembroEjemplo.setTelefono("555-0101");
        miembroEjemplo.setFechaRegistro(java.time.LocalDate.of(2024, 1, 14));
        miembroEjemplo.setMembresiaActiva(true);
        miembroEjemplo.setTipoMembresia("Premium");
        miembroEjemplo.setRol("DEPORTISTA");
        
        model.addAttribute("miembro", miembroEjemplo);
        return "perfil-usuario";
    }
    
    @GetMapping("/configuracion")
    public String configuracion() {
        // Vista de configuración - solo visual, sin conexión a BD
        return "configuracion";
    }
}
