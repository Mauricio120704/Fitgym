package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.service.ClaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/deportista")
public class MisClasesApiController {

    private final PersonaRepository personaRepository;
    private final ClaseService claseService;

    public MisClasesApiController(PersonaRepository personaRepository, ClaseService claseService) {
        this.personaRepository = personaRepository;
        this.claseService = claseService;
    }

    @GetMapping("/mis-clases")
    public ResponseEntity<?> misClases(@RequestParam(name = "deportistaId", required = false) Long deportistaId,
                                       Principal principal) {
        // Obtener el deportistaId del principal si no se proporciona
        if (deportistaId == null && principal != null) {
            deportistaId = personaRepository.findByEmail(principal.getName())
                    .map(p -> p.getId())
                    .orElse(null);
        }
        
        // Validar que se tenga un deportistaId
        if (deportistaId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "deportistaId requerido"));
        }

        try {
            // Delegar la lógica de negocio al servicio
            Map<String, Object> resultado = claseService.obtenerClasesDeportista(deportistaId);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener las clases del deportista"));
        }
    }
}
