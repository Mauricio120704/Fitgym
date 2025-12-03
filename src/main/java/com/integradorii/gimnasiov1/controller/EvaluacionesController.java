package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.dto.EvaluacionViewDTO;
import com.integradorii.gimnasiov1.dto.EntrenamientoViewDTO;
import com.integradorii.gimnasiov1.model.Evaluacion;
import com.integradorii.gimnasiov1.model.Entrenamiento;
import com.integradorii.gimnasiov1.repository.EntrenamientoRepository;
import com.integradorii.gimnasiov1.repository.EvaluacionRepository;
import com.integradorii.gimnasiov1.service.EntrenamientoViewService;
import com.integradorii.gimnasiov1.service.EvaluacionViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class EvaluacionesController {

    private final EvaluacionRepository evaluacionRepository;
    private final EvaluacionViewService evaluacionViewService;
    private final EntrenamientoRepository entrenamientoRepository;
    private final EntrenamientoViewService entrenamientoViewService;

    public EvaluacionesController(EvaluacionRepository evaluacionRepository,
                                  EvaluacionViewService evaluacionViewService,
                                  EntrenamientoRepository entrenamientoRepository,
                                  EntrenamientoViewService entrenamientoViewService) {
        this.evaluacionRepository = evaluacionRepository;
        this.evaluacionViewService = evaluacionViewService;
        this.entrenamientoRepository = entrenamientoRepository;
        this.entrenamientoViewService = entrenamientoViewService;
    }

    @GetMapping("/evaluaciones")
    public String listar(@RequestParam(required = false) String usuario,
                         Model model) {
        List<Evaluacion> evals = (usuario == null || usuario.isBlank())
                ? evaluacionRepository.findAllByOrderByFechaDesc()
                : evaluacionRepository.findByDeportista_EmailOrderByFechaDesc(usuario.trim());

        List<EvaluacionViewDTO> view = evals.stream().map(evaluacionViewService::toView).collect(Collectors.toList());

        model.addAttribute("evaluaciones", view);
        List<Entrenamiento> entrenamientos = entrenamientoRepository.findAllByOrderByIdDesc();
        List<EntrenamientoViewDTO> entrenamientosView = entrenamientos.stream()
                .map(entrenamientoViewService::toView)
                .collect(Collectors.toList());
        model.addAttribute("entrenamientos", entrenamientosView);
        model.addAttribute("rutina", null);
        model.addAttribute("activeMenu", "evaluaciones");
        return "evaluaciones";
    }

    @PostMapping("/evaluaciones/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminar(@PathVariable long id) {
        Map<String, Object> resp = new HashMap<>();
        if (evaluacionRepository.existsById(id)) {
            evaluacionRepository.deleteById(id);
            resp.put("success", true);
        } else {
            resp.put("success", false);
            resp.put("message", "Evaluación no encontrada");
        }
        return resp;
    }
}
