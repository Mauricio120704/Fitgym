package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Comunidad - Módulo visible solo para deportistas (ROLE_CLIENTE)
 * Rutas:
 *  - GET  /deportista/comunidad           -> Vista
 *  - GET  /api/comunidad/miembros         -> Listado de deportistas visibles
 *  - GET  /api/comunidad/miembros/{id}    -> Detalle de deportista
 */
@Controller
public class ComunidadController {

    private final PersonaRepository personaRepository;

    public ComunidadController(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    /**
     * Vista principal de la comunidad de deportistas.
     *
     * Solo accesible para usuarios con rol CLIENTE. El contenido dinámico de la
     * vista se llena vía llamadas AJAX a los endpoints REST de este mismo controlador.
     */
    @GetMapping("/deportista/comunidad")
    @PreAuthorize("hasRole('CLIENTE')")
    public String comunidad(Model model, Principal principal) {
        model.addAttribute("activeMenu", "comunidad");
        return "deportista/comunidad";
    }

    /**
     * Devuelve el listado de miembros visibles en la comunidad.
     *
     * - Solo se incluyen personas con `perfilVisible = true`.
     * - Si hay un usuario autenticado, se excluye su propio perfil del listado.
     * - Permite filtrar por nombre, apellido o DNI usando el parámetro `q`.
     * - La estructura de respuesta está pensada para cards/resúmenes en la UI.
     */
    @GetMapping("/api/comunidad/miembros")
    @ResponseBody
    @PreAuthorize("hasRole('CLIENTE')")
    public List<Map<String, Object>> miembros(@RequestParam(value = "q", required = false) String q,
                                              Principal principal) {
        String emailActual = principal != null ? principal.getName() : null;
        List<Persona> base = emailActual != null
                ? personaRepository.findByPerfilVisibleTrueAndEmailNot(emailActual)
                : personaRepository.findByPerfilVisibleTrue();

        if (q != null && !q.isBlank()) {
            String term = q.trim();
            base = base.stream().filter(p ->
                    (p.getNombre() != null && p.getNombre().toLowerCase().contains(term.toLowerCase())) ||
                    (p.getApellido() != null && p.getApellido().toLowerCase().contains(term.toLowerCase())) ||
                    (p.getDni() != null && p.getDni().toLowerCase().contains(term.toLowerCase()))
            ).collect(Collectors.toList());
        }

        return base.stream().map(this::toCard).collect(Collectors.toList());
    }

    /**
     * Devuelve el detalle de un miembro específico de la comunidad.
     *
     * Si el perfil no es visible (`perfilVisible = false`) o el ID no existe,
     * responde 404 para evitar exponer datos privados.
     */
    @GetMapping("/api/comunidad/miembros/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Map<String, Object>> detalle(@PathVariable long id) {
        return personaRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getPerfilVisible()))
                .map(p -> ResponseEntity.ok(toDetail(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Construye el mapa de datos mínimos para mostrar la "card" de un deportista
     * en el listado de comunidad.
     *
     * Solo se incluyen peso y altura si el usuario marcó explícitamente que desea
     * mostrarlos (`mostrarPeso` / `mostrarAltura`).
     */
    private Map<String, Object> toCard(Persona p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("iniciales", (safeInitial(p.getNombre()) + safeInitial(p.getApellido())).trim());
        m.put("nombreCompleto", (p.getNombre() != null ? p.getNombre() : "") + " " + (p.getApellido() != null ? p.getApellido() : ""));
        m.put("genero", p.getGenero());
        m.put("membresiaActiva", p.getMembresiaActiva());
        m.put("descripcion", p.getDescripcion());
        if (Boolean.TRUE.equals(p.getMostrarPeso())) {
            m.put("peso", p.getPeso());
        }
        if (Boolean.TRUE.equals(p.getMostrarAltura())) {
            m.put("altura", p.getAltura());
        }
        return m;
    }

    /**
     * Construye el detalle ampliado de un deportista para la vista de perfil
     * dentro de la comunidad.
     *
     * Extiende los datos de la card con información de contacto (email, teléfono, DNI)
     * y respeta la misma lógica de visibilidad para peso y altura.
     */
    private Map<String, Object> toDetail(Persona p) {
        Map<String, Object> m = toCard(p);
        m.put("email", p.getEmail());
        m.put("telefono", p.getTelefono());
        m.put("dni", p.getDni());
        m.put("descripcion", p.getDescripcion());
        if (Boolean.TRUE.equals(p.getMostrarPeso())) {
            m.put("peso", p.getPeso());
        }
        if (Boolean.TRUE.equals(p.getMostrarAltura())) {
            m.put("altura", p.getAltura());
        }
        // Campos adicionales de ejemplo que podrían mostrarse
        // m.put("especialidad", ...);
        return m;
    }

    /**
     * Devuelve la inicial segura de un texto (nombre o apellido).
     *
     * Si el valor es nulo o en blanco, devuelve cadena vacía para evitar errores
     * y que la UI pueda decidir cómo mostrar un placeholder.
     */
    private String safeInitial(String s) {
        return (s != null && !s.isBlank()) ? s.substring(0, 1).toUpperCase() : "";
        
    }
}
