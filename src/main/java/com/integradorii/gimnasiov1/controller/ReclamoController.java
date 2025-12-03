package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Reclamo;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.service.ReclamoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de Reclamos del lado del deportista.
 *
 * Función de sistema cubierta:
 *  - "Gestión de reclamos": permite crear y consultar reclamos del deportista.
 *
 * Rutas principales:
 *  - GET  /reclamos              -> Vista con listado de reclamos del usuario autenticado.
 *  - POST /reclamos/crear        -> API REST para registrar un nuevo reclamo.
 *  - GET  /reclamos/mis-reclamos -> API REST que devuelve los reclamos del deportista en JSON.
 */
@Controller
@RequestMapping("/reclamos")
public class ReclamoController {
    
    private final ReclamoService reclamoService;
    private final PersonaRepository personaRepository;
    
    public ReclamoController(ReclamoService reclamoService, PersonaRepository personaRepository) {
        this.reclamoService = reclamoService;
        this.personaRepository = personaRepository;
    }
    
    /**
     * Muestra la vista de reclamos del deportista autenticado.
     *
     * Flujo general:
     *  - Verifica que haya un usuario autenticado en el contexto de seguridad.
     *  - Recupera la entidad Persona asociada al email del usuario.
     *  - Obtiene desde el servicio la lista de reclamos de ese deportista.
     *  - Carga en el modelo los datos del usuario y sus reclamos para
     *    renderizar la plantilla Thymeleaf `reclamos.html`.
     */
    @GetMapping
    public String mostrarReclamos(Model model, 
                                  @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        String email = userDetails.getUsername();
        Persona persona = personaRepository.findByEmail(email).orElse(null);
        
        if (persona == null) {
            return "redirect:/login";
        }
        
        // Obtener los reclamos del deportista
        List<Reclamo> reclamos = reclamoService.obtenerReclamosPorDeportista(persona);
        
        model.addAttribute("usuario", persona);
        model.addAttribute("reclamos", reclamos);
        model.addAttribute("activeMenu", "reclamos");
        return "reclamos";
    }
    
    /**
     * Endpoint REST para crear un nuevo reclamo.
     *
     * - Solo puede ser invocado por un deportista autenticado.
     * - Utiliza el email presente en el contexto de seguridad para
     *   recuperar la entidad Persona correspondiente.
     * - Delegará en `ReclamoService` la creación y validación del reclamo.
     * - Devuelve un JSON con indicadores de éxito, mensaje descriptivo
     *   y el identificador del reclamo generado.
     */
    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearReclamo(
            @RequestParam String categoria,
            @RequestParam String asunto,
            @RequestParam String descripcion,
            @RequestParam(required = false, defaultValue = "Normal") String prioridad,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar autenticación
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "Usuario no autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Obtener el deportista desde la sesión
            String email = userDetails.getUsername();
            Persona deportista = personaRepository.findByEmail(email).orElse(null);
            
            if (deportista == null) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Crear el reclamo
            Reclamo reclamo = reclamoService.crearReclamo(
                deportista, 
                categoria, 
                asunto, 
                descripcion, 
                prioridad
            );
            
            response.put("success", true);
            response.put("message", "Reclamo enviado correctamente");
            response.put("reclamoId", reclamo.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar el reclamo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint REST que devuelve los reclamos del deportista autenticado.
     *
     * Suele ser consumido mediante peticiones AJAX desde la vista para
     * actualizar el listado de reclamos sin recargar toda la página.
     * Si el usuario no está autenticado o no se encuentra la Persona
     * asociada, se devuelve el código de error HTTP correspondiente.
     */
    @GetMapping("/mis-reclamos")
    @ResponseBody
    public ResponseEntity<List<Reclamo>> obtenerMisReclamos(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String email = userDetails.getUsername();
        Persona deportista = personaRepository.findByEmail(email).orElse(null);
        
        if (deportista == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        List<Reclamo> reclamos = reclamoService.obtenerReclamosPorDeportista(deportista);
        return ResponseEntity.ok(reclamos);
    }
}
