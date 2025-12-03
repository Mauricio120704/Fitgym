package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.dto.EntrenamientoViewDTO;
import com.integradorii.gimnasiov1.model.Entrenamiento;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.repository.EntrenamientoRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import com.integradorii.gimnasiov1.service.EntrenamientoViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/entrenador/mis-deportistas")
public class MisDeportistasController {

    @Autowired
    private PersonaRepository personaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntrenamientoRepository entrenamientoRepository;

    @Autowired
    private EntrenamientoViewService entrenamientoViewService;

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
    public String verPerfilDeportista(@PathVariable long id, Model model) {
        Persona deportista = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Deportista no encontrado"));
        
        // Obtener el entrenador autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario entrenador = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Entrenamientos creados por este deportista en el planificador de evaluaciones
        List<Entrenamiento> entrenamientos = entrenamientoRepository.findByCreadoPorOrderByIdDesc(deportista);
        List<EntrenamientoViewDTO> entrenamientosView = entrenamientos.stream()
                .map(entrenamientoViewService::toView)
                .collect(Collectors.toList());

        model.addAttribute("deportista", deportista);
        model.addAttribute("entrenador", entrenador);
        model.addAttribute("entrenamientos", entrenamientosView);

        return "entrenador/mis-deportistas/perfil";
    }

    /**
     * Guardar/actualizar nota de un entrenamiento desde el rol entrenador
     */
    @PostMapping(path = "/entrenamientos/{id}/nota", consumes = "application/x-www-form-urlencoded", produces = "application/json")
    @ResponseBody
    public Map<String, Object> actualizarNotaEntrenamiento(@PathVariable long id,
                                                           @RequestParam("nota") String nota) {
        Map<String, Object> resp = new HashMap<>();

        Optional<Entrenamiento> opt = entrenamientoRepository.findById(id);
        if (opt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Entrenamiento no encontrado");
            return resp;
        }

        Entrenamiento entrenamiento = opt.get();
        EntrenamientoViewDTO dto = entrenamientoViewService.toView(entrenamiento);

        int duracion;
        try {
            duracion = Integer.parseInt(dto.getDuracion());
        } catch (NumberFormatException ex) {
            duracion = 0;
        }

        String nuevaDescripcion = entrenamientoViewService.serialize(
                dto.getDiaSemana(),
                dto.getHoraInicio(),
                duracion,
                nota
        );

        entrenamiento.setDescripcion(nuevaDescripcion);
        entrenamientoRepository.save(entrenamiento);

        resp.put("success", true);
        return resp;
    }
}
