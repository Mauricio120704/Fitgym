package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Visitante;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.repository.VisitanteRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
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

/**
 * Controlador para gestionar invitados asociados a miembros del gimnasio
 */
@Controller
@RequestMapping("/admin/invitados")
public class InvitadoController {

    @Autowired
    private VisitanteRepository visitanteRepository;
    
    @Autowired
    private PersonaRepository personaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Muestra el listado de personas (miembros) con opción de agregar invitados
     */
    @GetMapping
    public String listarMiembrosParaInvitados(
            @RequestParam(required = false) String buscar,
            Model model) {
        
        List<Persona> personas;
        
        if (buscar != null && !buscar.trim().isEmpty()) {
            personas = personaRepository.searchDeportistas(buscar.trim());
        } else {
            personas = personaRepository.findAll();
        }
        
        model.addAttribute("personas", personas);
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        
        return "admin/invitados/listado-miembros";
    }

    /**
     * Muestra el formulario para registrar un invitado asociado a una persona
     */
    @GetMapping("/registrar/{personaId}")
    public String mostrarFormularioRegistroInvitado(
            @PathVariable long personaId,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
            
            Visitante visitante = new Visitante();
            
            model.addAttribute("persona", persona);
            model.addAttribute("visitante", visitante);
            
            return "admin/invitados/registro";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/admin/invitados";
        }
    }

    /**
     * Procesa el registro de un invitado
     */
    @PostMapping("/registrar/{personaId}")
    public String registrarInvitado(
            @PathVariable long personaId,
            @ModelAttribute Visitante visitante,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Obtener la persona (miembro) que invita
            Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
            
            // Obtener el usuario autenticado (recepcionista)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Configurar datos del visitante/invitado
            visitante.setInvitadoPorPersona(persona);
            visitante.setRegistradoPor(usuario);
            visitante.setFechaHoraIngreso(LocalDateTime.now());
            visitante.setEstado("ACTIVO");
            
            // Generar código de pase único
            String codigoPase = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            visitante.setCodigoPase(codigoPase);
            
            // Guardar el invitado
            visitanteRepository.save(visitante);
            
            redirectAttributes.addFlashAttribute("success", 
                "Invitado registrado exitosamente para " + persona.getNombreCompleto() + 
                ". Código de pase: " + codigoPase);
            
            return "redirect:/admin/invitados";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar el invitado: " + e.getMessage());
            return "redirect:/admin/invitados";
        }
    }

    /**
     * Muestra los invitados de una persona específica
     */
    @GetMapping("/persona/{personaId}")
    public String verInvitadosDePersona(
            @PathVariable long personaId,
            @RequestParam(required = false) String estado,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
            
            List<Visitante> invitados;
            
            if (estado != null && !estado.isEmpty()) {
                invitados = visitanteRepository.findByInvitadoPorPersonaAndEstado(persona, estado);
            } else {
                invitados = visitanteRepository.findByInvitadoPorPersona(persona);
            }
            
            Long totalInvitados = visitanteRepository.countByInvitadoPorPersona(persona);
            
            model.addAttribute("persona", persona);
            model.addAttribute("invitados", invitados);
            model.addAttribute("totalInvitados", totalInvitados);
            model.addAttribute("estadoActual", estado != null ? estado : "");
            
            return "admin/invitados/listado-invitados";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar los invitados: " + e.getMessage());
            return "redirect:/admin/invitados";
        }
    }

    /**
     * Registra la salida de un invitado
     */
    @PostMapping("/{id}/registrar-salida")
    public String registrarSalidaInvitado(
            @PathVariable long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            Visitante visitante = visitanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitado no encontrado"));
            
            Long personaId = visitante.getInvitadoPorPersona() != null ? 
                visitante.getInvitadoPorPersona().getId() : null;
            
            visitante.registrarSalida();
            visitanteRepository.save(visitante);
            
            redirectAttributes.addFlashAttribute("success", "Salida del invitado registrada exitosamente");
            
            if (personaId != null) {
                return "redirect:/admin/invitados/persona/" + personaId;
            } else {
                return "redirect:/admin/invitados";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar la salida: " + e.getMessage());
            return "redirect:/admin/invitados";
        }
    }
}
