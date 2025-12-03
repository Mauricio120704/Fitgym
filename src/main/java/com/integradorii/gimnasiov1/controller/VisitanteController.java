package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Visitante;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.repository.VisitanteRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/visitantes")
public class VisitanteController {

    @Autowired
    private VisitanteRepository visitanteRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("visitante", new Visitante());
        return "admin/visitantes/registro";
    }

    @PostMapping("/registrar")
    public String registrarVisitante(@ModelAttribute Visitante visitante, RedirectAttributes redirectAttributes) {
        try {
            // Obtener el usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Configurar datos del visitante
            visitante.setRegistradoPor(usuario);
            visitante.setFechaHoraIngreso(LocalDateTime.now());
            visitante.setEstado("ACTIVO");
            
            // Generar código de pase único
            String codigoPase = "VP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            visitante.setCodigoPase(codigoPase);
            
            // Guardar el visitante
            visitanteRepository.save(visitante);
            
            redirectAttributes.addFlashAttribute("success", "Visitante registrado exitosamente. Código de pase: " + codigoPase);
            return "redirect:/admin/visitantes/listado";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar el visitante: " + e.getMessage());
            return "redirect:/admin/visitantes/listado";
        }
    }

    @GetMapping("/listado")
    public String listarVisitantes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String documento,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<Visitante> visitantesFiltrados;

        // Prioridad: primero documento, luego estado
        if (documento != null && !documento.isEmpty()) {
            visitantesFiltrados = visitanteRepository.findByDocumentoIdentidad(documento);
        } else if (estado != null && !estado.isEmpty()) {
            visitantesFiltrados = visitanteRepository.findByEstado(estado);
        } else {
            // Si no hay filtros, mostrar todos
            visitantesFiltrados = visitanteRepository.findAll();
        }

        // Configuración de paginación
        int pageSize = 20;
        if (page < 0) page = 0;

        int totalVisitantes = visitantesFiltrados.size();
        int totalPages = (int) Math.ceil((double) totalVisitantes / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page >= totalPages) page = totalPages - 1;

        int start = page * pageSize;
        int end = Math.min(start + pageSize, totalVisitantes);
        List<Visitante> visitantesPaginados =
                totalVisitantes == 0 ? new java.util.ArrayList<>() : visitantesFiltrados.subList(start, end);

        // Pasar los valores actuales de los filtros y la paginación a la vista
        model.addAttribute("visitantes", visitantesPaginados);
        model.addAttribute("estadoActual", estado != null ? estado : "");
        model.addAttribute("documentoActual", documento != null ? documento : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalVisitantes", totalVisitantes);
        return "admin/visitantes/listado";
    }

    @PostMapping("/{id}/registrar-salida")
    public String registrarSalida(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            Visitante visitante = visitanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visitante no encontrado"));
                
            visitante.registrarSalida();
            visitanteRepository.save(visitante);
            
            redirectAttributes.addFlashAttribute("success", "Salida del visitante registrada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar la salida: " + e.getMessage());
        }
        
        return "redirect:/admin/visitantes/listado";
    }
    
    @GetMapping("/buscar-por-codigo")
    @ResponseBody
    public Visitante buscarPorCodigo(@RequestParam String codigo) {
        return visitanteRepository.findByCodigoPase(codigo);
    }
}
