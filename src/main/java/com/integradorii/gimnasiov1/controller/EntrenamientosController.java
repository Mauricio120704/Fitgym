package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.dto.EntrenamientoViewDTO;
import com.integradorii.gimnasiov1.model.Entrenamiento;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.EntrenamientoRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.service.EntrenamientoViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class EntrenamientosController {

    private final EntrenamientoRepository entrenamientoRepository;
    private final EntrenamientoViewService viewService;
    private final PersonaRepository personaRepository;

    public EntrenamientosController(EntrenamientoRepository entrenamientoRepository,
                                    EntrenamientoViewService viewService,
                                    PersonaRepository personaRepository) {
        this.entrenamientoRepository = entrenamientoRepository;
        this.viewService = viewService;
        this.personaRepository = personaRepository;
    }

    // GET /entrenamientos: llena la vista con la lista de entrenamientos del deportista autenticado
    @GetMapping("/entrenamientos")
    public String listar(Model model, Principal principal) {
        List<Entrenamiento> lista;
        if (principal != null) {
            Persona deportista = personaRepository.findByEmail(principal.getName()).orElse(null);
            if (deportista != null) {
                lista = entrenamientoRepository.findByCreadoPorOrderByIdDesc(deportista);
            } else {
                lista = entrenamientoRepository.findAllByOrderByIdDesc();
            }
        } else {
            lista = entrenamientoRepository.findAllByOrderByIdDesc();
        }
        List<EntrenamientoViewDTO> view = lista.stream().map(viewService::toView).collect(Collectors.toList());
        model.addAttribute("entrenamientos", view);
        // Campos de cabecera opcionales: rutina
        model.addAttribute("rutina", null);
        return "entrenamientos";
    }

    // POST form-urlencoded desde la vista para crear
    @PostMapping(path = "/entrenamientos/agregar", consumes = "application/x-www-form-urlencoded", produces = "application/json")
    @ResponseBody
    public Map<String, Object> agregar(@RequestParam String tipo,
                                       @RequestParam String diaSemana,
                                       @RequestParam String horaInicio,
                                       @RequestParam Integer duracion,
                                       @RequestParam(required = false) String notas,
                                       Principal principal) {
        Entrenamiento e = new Entrenamiento();
        e.setNombre(tipo);
        e.setDescripcion(viewService.serialize(diaSemana, horaInicio, duracion, notas));
        // Asociar entrenamiento al deportista autenticado, si existe
        if (principal != null) {
            personaRepository.findByEmail(principal.getName()).ifPresent(e::setCreadoPor);
        }
        entrenamientoRepository.save(e);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("entrenamiento", viewService.toView(e));
        return resp;
    }

    // POST actualizar
    @PostMapping(path = "/entrenamientos/{id}", consumes = "application/x-www-form-urlencoded", produces = "application/json")
    @ResponseBody
    public Map<String, Object> actualizar(@PathVariable long id,
                                          @RequestParam String tipo,
                                          @RequestParam String diaSemana,
                                          @RequestParam String horaInicio,
                                          @RequestParam Integer duracion,
                                          @RequestParam(required = false) String notas) {
        Map<String, Object> resp = new HashMap<>();
        Optional<Entrenamiento> opt = entrenamientoRepository.findById(id);
        if (opt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Entrenamiento no encontrado");
            return resp;
        }
        Entrenamiento e = opt.get();
        e.setNombre(tipo);
        e.setDescripcion(viewService.serialize(diaSemana, horaInicio, duracion, notas));
        entrenamientoRepository.save(e);
        resp.put("success", true);
        resp.put("entrenamiento", viewService.toView(e));
        return resp;
    }

    // POST eliminar/{id} devuelve JSON {success}
    @PostMapping(path = "/entrenamientos/eliminar/{id}", produces = "application/json")
    @ResponseBody
    public Map<String, Object> eliminar(@PathVariable long id) {
        Map<String, Object> resp = new HashMap<>();
        if (entrenamientoRepository.existsById(id)) {
            entrenamientoRepository.deleteById(id);
            resp.put("success", true);
        } else {
            resp.put("success", false);
            resp.put("message", "Entrenamiento no encontrado");
        }
        return resp;
    }

    // Mapeos auxiliares migrados a EntrenamientoViewService para mantener el controlador limpio
}
