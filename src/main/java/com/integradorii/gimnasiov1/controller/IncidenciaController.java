package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.dto.IncidenciaViewDTO;
import com.integradorii.gimnasiov1.model.Incidencia;
import com.integradorii.gimnasiov1.repository.IncidenciaRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import com.integradorii.gimnasiov1.service.IncidenciaViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class IncidenciaController {
    private final IncidenciaRepository incidenciaRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IncidenciaViewService incidenciaViewService;

    public IncidenciaController(IncidenciaRepository incidenciaRepository,
                               PersonaRepository personaRepository,
                               UsuarioRepository usuarioRepository,
                               IncidenciaViewService incidenciaViewService) {
        this.incidenciaRepository = incidenciaRepository;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.incidenciaViewService = incidenciaViewService;
    }

    @GetMapping("/incidencias")
    public String listar(
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false, defaultValue = "todas") String prioridad,
            @RequestParam(required = false) String buscar,
            Model model) {

        // Mapear valores UI -> BD
        String estadoDb = null;
        if ("ABIERTO".equalsIgnoreCase(estado)) estadoDb = "Abierta";
        else if ("RESUELTO".equalsIgnoreCase(estado)) estadoDb = "Resuelto";

        String prioridadDb = null;
        if ("ALTA".equalsIgnoreCase(prioridad)) prioridadDb = "Alta";
        else if ("MEDIA".equalsIgnoreCase(prioridad)) prioridadDb = "Media";
        else if ("BAJA".equalsIgnoreCase(prioridad)) prioridadDb = "Baja";

        List<Incidencia> list = incidenciaRepository.findFiltered(
                estado == null ? "todos" : estado,
                estadoDb,
                prioridad == null ? "todas" : prioridad,
                prioridadDb,
                buscar == null ? "" : buscar.trim());

        // Mapear a DTOs mediante el servicio
        List<IncidenciaViewDTO> viewList = list.stream()
                .map(incidenciaViewService::toView)
                .collect(Collectors.toList());

        long totalInc = incidenciaRepository.total();
        long abiertas = incidenciaRepository.abiertas();
        long resueltas = incidenciaRepository.resueltas();

        model.addAttribute("incidencias", viewList);
        model.addAttribute("totalIncidencias", totalInc);
        model.addAttribute("abiertas", abiertas);
        model.addAttribute("resueltas", resueltas);
        model.addAttribute("buscarActual", buscar == null ? "" : buscar);
        model.addAttribute("estadoActual", estado == null ? "todos" : estado);
        model.addAttribute("prioridadActual", prioridad == null ? "todas" : prioridad);
        model.addAttribute("activeMenu", "incidencias");

        return "incidencias";
    }

    @PostMapping({"/incidencias", "/incidencias/crear"})
    public String crear(
            @RequestParam String titulo,
            @RequestParam String descripcion,
            @RequestParam(required = false) String reportado,
            @RequestParam(required = false) String asignado,
            @RequestParam String prioridad,
            @RequestParam(required = false) String imagenes
    ) {
        Incidencia i = new Incidencia();
        i.setTitulo(titulo);
        // Guardar fallbacks de nombres en la descripción
        StringBuilder descBuilder = new StringBuilder(descripcion == null ? "" : descripcion);
        if (reportado != null && !reportado.isBlank()) {
            descBuilder.append("\n__REPORTADO__=").append(reportado.trim());
        }
        if (asignado != null && !asignado.isBlank()) {
            descBuilder.append("\n__ASIGNADO__=").append(asignado.trim());
        }
        String descToSave = descBuilder.toString();
        i.setDescripcion(descToSave);

        // Procesar imágenes: guardar el JSON completo (array de data URLs) como UTF-8
        byte[] imagenesBytes = null;
        if (imagenes != null && !imagenes.isBlank()) {
            try {
                imagenesBytes = imagenes.getBytes(StandardCharsets.UTF_8);
            } catch (Exception e) {
                imagenesBytes = null;
            }
        }
        i.setImagenes(imagenesBytes);
        i.setCategoria("General");

        String pr = "Media";
        if ("ALTA".equalsIgnoreCase(prioridad)) pr = "Alta"; else if ("BAJA".equalsIgnoreCase(prioridad)) pr = "Baja";
        i.setPrioridad(pr);
        i.setEstado("Abierta");
        i.setFechaReporte(OffsetDateTime.now(ZoneId.systemDefault()));
        i.setUltimaActualizacion(OffsetDateTime.now(ZoneId.systemDefault()));

        // Intentar resolver personas por email (si se enviara email), o dejar null
        if (reportado != null && !reportado.isBlank() && reportado.contains("@")) {
            personaRepository.findByEmail(reportado.trim()).ifPresent(i::setReportadoPor);
        }
        if (asignado != null && !asignado.isBlank() && asignado.contains("@")) {
            personaRepository.findByEmail(asignado.trim()).ifPresent(i::setAsignadoA);
        }

        incidenciaRepository.save(i);
        return "redirect:/incidencias";
    }

    @RequestMapping(value = "/incidencias/{id}/estado", method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public Map<String, Object> actualizarEstado(@PathVariable long id, @RequestBody(required = false) String body,
                                                @RequestParam(required = false) String estado) {
        // estado puede venir en body JSON simple ("RESUELTO") o como parámetro
        String nuevoEstado = estado;
        if ((nuevoEstado == null || nuevoEstado.isBlank()) && body != null) {
            nuevoEstado = body.replace("\"", "").trim();
        }

        Map<String, Object> resp = new HashMap<>();
        Incidencia i = incidenciaRepository.findById(id).orElse(null);
        if (i == null) {
            resp.put("success", false);
            resp.put("message", "Incidencia no encontrada");
            return resp;
        }

        if ("RESUELTO".equalsIgnoreCase(nuevoEstado)) {
            i.setEstado("Resuelto");
            i.setFechaCierre(OffsetDateTime.now(ZoneId.systemDefault()));
        } else {
            i.setEstado("Abierta");
            i.setFechaCierre(null);
        }
        i.setUltimaActualizacion(OffsetDateTime.now(ZoneId.systemDefault()));
        incidenciaRepository.save(i);

        resp.put("success", true);
        resp.put("estado", i.getEstado());
        return resp;
    }

    @PostMapping("/incidencias/{id}/eliminar")
    public String eliminar(@PathVariable long id) {
        incidenciaRepository.deleteById(id);
        return "redirect:/incidencias";
    }

    // Sugerencias de personas para autocompletar "Reportado por"
    @GetMapping(path = "/personas/sugerencias", produces = "application/json")
    @ResponseBody
    public List<Map<String, String>> sugerencias(@RequestParam String term) {
        String q = term == null ? "" : term.trim();
        if (q.isEmpty()) return Collections.emptyList();
        // Buscar en usuarios administrativos en lugar de personas
        return usuarioRepository.searchUsuarios(q).stream().limit(10).map(p -> {
            Map<String, String> m = new HashMap<>();
            String nombre = (p.getNombre() == null ? "" : p.getNombre());
            String apellido = (p.getApellido() == null ? "" : (" " + p.getApellido()));
            m.put("nombreCompleto", (nombre + apellido).trim());
            m.put("email", p.getEmail());
            return m;
        }).toList();
    }
}
