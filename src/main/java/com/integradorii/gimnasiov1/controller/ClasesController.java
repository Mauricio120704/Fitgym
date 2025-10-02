package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.dto.ClaseViewDTO;
import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.ClaseRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.ReservaClaseRepository;
import com.integradorii.gimnasiov1.service.ClaseViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ClasesController {

    private final ClaseRepository claseRepository;
    private final ReservaClaseRepository reservaClaseRepository;
    private final PersonaRepository personaRepository;
    private final ClaseViewService claseViewService;

    public ClasesController(ClaseRepository claseRepository,
                            ReservaClaseRepository reservaClaseRepository,
                            PersonaRepository personaRepository,
                            ClaseViewService claseViewService) {
        this.claseRepository = claseRepository;
        this.reservaClaseRepository = reservaClaseRepository;
        this.personaRepository = personaRepository;
        this.claseViewService = claseViewService;
    }

    @GetMapping("/clases")
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        List<Clase> clases = (buscar == null || buscar.isBlank())
                ? claseRepository.findAllByOrderByFechaAsc()
                : claseRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrderByFechaAsc(buscar.trim(), buscar.trim());

        // Mapear a DTOs para la vista
        List<ClaseViewDTO> view = clases.stream().map(claseViewService::toView).collect(Collectors.toList());

        long totalClases = clases.size();
        long clasesLlenas = view.stream()
                .filter(v -> (v.getOcupadosPremium() + v.getOcupadosElite()) >= (v.getCuposPremium() + v.getCuposElite()))
                .count();
        int totalCupos = view.stream().mapToInt(v -> v.getCuposPremium() + v.getCuposElite()).sum();

        model.addAttribute("clases", view);
        model.addAttribute("totalClases", totalClases);
        model.addAttribute("clasesLlenas", clasesLlenas);
        model.addAttribute("totalCupos", totalCupos);
        model.addAttribute("buscarActual", buscar == null ? "" : buscar);
        return "clases";
    }

    @PostMapping(value = "/clases/crear", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Map<String, Object> crear(@RequestBody Map<String, Object> body) {
        Clase c = new Clase();
        applyFromBody(c, body);
        claseRepository.save(c);
        return toJson(c);
    }

    @PutMapping(value = "/clases/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Map<String, Object> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Clase c = claseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
        applyFromBody(c, body);
        claseRepository.save(c);
        return toJson(c);
    }

    @DeleteMapping("/clases/{id}")
    @ResponseBody
    public void eliminar(@PathVariable Long id) {
        reservaClaseRepository.deleteByClase_Id(id);
        claseRepository.deleteById(id);
    }

    private void applyFromBody(Clase c, Map<String, Object> body) {
        c.setNombre(String.valueOf(body.getOrDefault("nombre", "")));
        c.setDescripcion(null);
        c.setDuracionMinutos(parseInt(body.get("duracion"), 60));
        c.setCapacidad(parseInt(body.get("cuposPremium"), 0) + parseInt(body.get("cuposElite"), 0));
        String fechaStr = String.valueOf(body.getOrDefault("fecha", ""));
        String horaStr = String.valueOf(body.getOrDefault("hora", "00:00"));
        if (!fechaStr.isBlank()) {
            LocalDate ld = LocalDate.parse(fechaStr);
            LocalTime lt = horaStr.isBlank() ? LocalTime.of(0,0) : LocalTime.parse(horaStr);
            ZoneId zone = ZoneId.systemDefault();
            OffsetDateTime odt = ld.atTime(lt).atZone(zone).toOffsetDateTime();
            c.setFecha(odt);
        }
        c.setEstado("Programada");
        // Entrenador por nombre libre en UI; si quieres por email, ajustamos. Intento encontrar PERSONAL por nombre.
        String instructor = String.valueOf(body.getOrDefault("instructor", ""));
        if (!instructor.isBlank()) {
            String[] parts = instructor.trim().split(" ", 2);
            String nombre = parts[0];
            String apellido = parts.length > 1 ? parts[1] : "";
            Persona entren = personaRepository.findByTipo("PERSONAL").stream()
                    .filter(p -> p.getNombre().equalsIgnoreCase(nombre) && (apellido.isBlank() || p.getApellido().equalsIgnoreCase(apellido)))
                    .findFirst().orElse(null);
            c.setEntrenador(entren);
        } else {
            c.setEntrenador(null);
        }
    }

    private int parseInt(Object v, int def) {
        try { return v == null ? def : Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
    }

    private Map<String, Object> toJson(Clase c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("nombre", c.getNombre());
        m.put("duracion", c.getDuracionMinutos());
        m.put("capacidad", c.getCapacidad());
        OffsetDateTime odt = c.getFecha();
        if (odt != null) {
            m.put("fecha", odt.toLocalDate().toString());
            m.put("hora", odt.toLocalTime().toString());
        }
        m.put("estado", c.getEstado());
        return m;
    }
}
