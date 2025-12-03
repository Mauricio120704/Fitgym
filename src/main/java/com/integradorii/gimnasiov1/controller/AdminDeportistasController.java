package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/deportistas")
public class AdminDeportistasController {

    private final PersonaRepository personaRepository;

    public AdminDeportistasController(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    @GetMapping
    public String listarDeportistas(@RequestParam(required = false) String buscar,
                                    @RequestParam(required = false, defaultValue = "todos") String filtroBloqueo,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    Model model) {
        // Normalizar página y tamaño
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }

        String term = (buscar != null) ? buscar.trim() : "";
        String estado = (filtroBloqueo != null && !filtroBloqueo.isBlank()) ? filtroBloqueo : "todos";

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Persona> pageResult = personaRepository.searchDeportistasAdmin(term, estado, pageable);

        List<Persona> paginados = pageResult.getContent();
        long totalFiltrados = pageResult.getTotalElements();
        int totalPages = pageResult.getTotalPages() == 0 ? 1 : pageResult.getTotalPages();

        int currentPage = pageResult.getNumber();
        int fromIndex = totalFiltrados == 0 ? 0 : (currentPage * pageResult.getSize()) + 1;
        int toIndex = totalFiltrados == 0 ? 0 : fromIndex + pageResult.getNumberOfElements() - 1;

        long totalDeportistas = personaRepository.count();
        long totalBloqueados = personaRepository.countByBloqueadoTrue();
        long totalNoBloqueados = totalDeportistas - totalBloqueados;

        model.addAttribute("deportistas", paginados);
        model.addAttribute("buscarActual", term);
        model.addAttribute("filtroBloqueo", estado);
        model.addAttribute("totalDeportistas", totalDeportistas);
        model.addAttribute("totalBloqueados", totalBloqueados);
        model.addAttribute("totalNoBloqueados", totalNoBloqueados);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageResult.getSize());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("fromIndex", fromIndex);
        model.addAttribute("toIndex", toIndex);
        model.addAttribute("activeMenu", "deportistas-admin");

        return "admin/deportistas";
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/{id}/bloquear")
    public String bloquearDeportista(@PathVariable long id,
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
    public String desbloquearDeportista(@PathVariable long id,
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
