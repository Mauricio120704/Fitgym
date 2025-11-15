package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final PersonaRepository personaRepository;

    public PerfilController(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @GetMapping("/editar")
    @PreAuthorize("hasRole('CLIENTE')")
    public String editarPerfil(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Persona p = personaRepository.findByEmail(principal.getName()).orElse(null);
        if (p == null) return "redirect:/login";
        model.addAttribute("miembro", p);
        model.addAttribute("activeMenu", "perfil");
        return "perfil-editar";
    }

    @PostMapping("/editar")
    @PreAuthorize("hasRole('CLIENTE')")
    public String guardarEdicion(
            Principal principal,
            Model model,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) Double peso,
            @RequestParam(required = false) Double altura,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false, defaultValue = "false") boolean mostrarPeso,
            @RequestParam(required = false, defaultValue = "false") boolean mostrarAltura
    ) {
        if (principal == null) return "redirect:/login";
        Persona p = personaRepository.findByEmail(principal.getName()).orElse(null);
        if (p == null) return "redirect:/login";

        // Setear valores ingresados para que persistan en el formulario
        p.setTelefono(telefono);
        p.setGenero(genero);
        p.setPeso(peso);
        p.setAltura(altura);
        p.setDescripcion(descripcion);
        p.setMostrarPeso(mostrarPeso);
        p.setMostrarAltura(mostrarAltura);

        // Validaciones suaves
        String errorPeso = null;
        String errorAltura = null;
        if (peso != null) {
            if (peso < 20 || peso > 300) {
                errorPeso = "El peso debe estar entre 20 y 300 kg";
            }
        }
        if (altura != null) {
            if (altura < 0.5 || altura > 2.5) {
                errorAltura = "La altura debe estar entre 0.50 y 2.50 m";
            }
        }

        if (errorPeso != null || errorAltura != null) {
            model.addAttribute("miembro", p);
            model.addAttribute("activeMenu", "perfil");
            if (errorPeso != null) model.addAttribute("errorPeso", errorPeso);
            if (errorAltura != null) model.addAttribute("errorAltura", errorAltura);
            return "perfil-editar";
        }

        personaRepository.save(p);
        return "redirect:/perfil";
    }
}
