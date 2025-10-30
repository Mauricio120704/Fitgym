package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/entrenador/mis-deportistas")
public class MisDeportistasController {

    @Autowired
    private PersonaRepository personaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Lista todos los deportistas activos para el entrenador
     */
    @GetMapping
    public String listarMisDeportistas(
            @RequestParam(required = false) String buscar,
            Model model) {
        
        // Obtener el entrenador autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario entrenador = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Persona> deportistas;
        
        // Si hay búsqueda, filtrar
        if (buscar != null && !buscar.isEmpty()) {
            deportistas = personaRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrDniContaining(
                buscar, buscar, buscar);
        } else {
            // Mostrar todos los deportistas activos
            deportistas = personaRepository.findByMembresiaActivaTrue();
        }
        
        model.addAttribute("deportistas", deportistas);
        model.addAttribute("entrenador", entrenador);
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        
        return "entrenador/mis-deportistas/listado";
    }

    /**
     * Ver perfil detallado de un deportista
     */
    @GetMapping("/{id}/perfil")
    public String verPerfilDeportista(@PathVariable Long id, Model model) {
        Persona deportista = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Deportista no encontrado"));
        
        // Obtener el entrenador autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario entrenador = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("deportista", deportista);
        model.addAttribute("entrenador", entrenador);
        
        return "entrenador/mis-deportistas/perfil";
    }
}
