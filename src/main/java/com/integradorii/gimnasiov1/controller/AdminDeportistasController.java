package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/deportistas")
public class AdminDeportistasController {

    private final PersonaRepository personaRepository;

    public AdminDeportistasController(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public String listarDeportistas(@RequestParam(required = false) String buscar,
                                    @RequestParam(required = false, defaultValue = "todos") String filtroBloqueo,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    Model model) {

        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }

        List<Persona> todos = personaRepository.findAll();
        List<Persona> filtrados = new ArrayList<>(todos);

        if (buscar != null && !buscar.trim().isEmpty()) {
            String term = buscar.trim().toLowerCase();
            filtrados = filtrados.stream()
                    .filter(p ->
                            (p.getNombre() != null && p.getNombre().toLowerCase().contains(term)) ||
                            (p.getApellido() != null && p.getApellido().toLowerCase().contains(term)) ||
                            (p.getEmail() != null && p.getEmail().toLowerCase().contains(term)) ||
                            (p.getDni() != null && p.getDni().toLowerCase().contains(term)))
                    .collect(Collectors.toList());
        }

        String filtro = filtroBloqueo != null ? filtroBloqueo.trim().toLowerCase() : "todos";
        if ("bloqueados".equals(filtro)) {
            filtrados = filtrados.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getBloqueado()))
                    .collect(Collectors.toList());
        } else if ("no_bloqueados".equals(filtro)) {
            filtrados = filtrados.stream()
                    .filter(p -> !Boolean.TRUE.equals(p.getBloqueado()))
                    .collect(Collectors.toList());
        }

        filtrados.sort(Comparator.comparing(Persona::getId));

        int totalFiltrados = filtrados.size();

        int totalPages = (int) Math.ceil((double) totalFiltrados / size);
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        int start = page * size;
        int end = Math.min(start + size, totalFiltrados);

        List<Persona> paginados = totalFiltrados == 0 ? new ArrayList<>() : filtrados.subList(start, end);

        long totalDeportistas = todos.size();
        long totalBloqueados = todos.stream().filter(p -> Boolean.TRUE.equals(p.getBloqueado())).count();
        long totalNoBloqueados = totalDeportistas - totalBloqueados;

        int fromIndex = totalFiltrados == 0 ? 0 : start + 1;
        int toIndex = totalFiltrados == 0 ? 0 : end;

        model.addAttribute("deportistas", paginados);
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        model.addAttribute("filtroBloqueo", filtroBloqueo != null ? filtroBloqueo : "todos");
        model.addAttribute("totalDeportistas", totalDeportistas);
        model.addAttribute("totalBloqueados", totalBloqueados);
        model.addAttribute("totalNoBloqueados", totalNoBloqueados);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("fromIndex", fromIndex);
        model.addAttribute("toIndex", toIndex);

        return "admin/deportistas";
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/{id}/bloquear")
    public String bloquearDeportista(@PathVariable Long id,
                                     @RequestParam(required = false) String motivo,
                                     RedirectAttributes redirectAttributes) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de deportista invalido: " + id));

        persona.setBloqueado(Boolean.TRUE);
        String motivoTrim = motivo != null ? motivo.trim() : "";
        if (motivoTrim.isEmpty()) {
            motivoTrim = "Bloqueado por administrador por incumplimiento de normas";
        }
        persona.setMotivoBloqueo(motivoTrim);
        personaRepository.save(persona);

        redirectAttributes.addFlashAttribute("success", "Deportista bloqueado correctamente");
        return "redirect:/admin/deportistas";
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/{id}/desbloquear")
    public String desbloquearDeportista(@PathVariable Long id,
                                        RedirectAttributes redirectAttributes) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de deportista invalido: " + id));

        persona.setBloqueado(Boolean.FALSE);
        persona.setMotivoBloqueo(null);
        personaRepository.save(persona);

        redirectAttributes.addFlashAttribute("success", "Deportista desbloqueado correctamente");
        return "redirect:/admin/deportistas";
    }
}
