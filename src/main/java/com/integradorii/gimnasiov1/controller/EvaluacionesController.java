package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.dto.EvaluacionViewDTO;
import com.integradorii.gimnasiov1.model.Evaluacion;
import com.integradorii.gimnasiov1.repository.EvaluacionRepository;
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

    public EvaluacionesController(EvaluacionRepository evaluacionRepository,
                                  EvaluacionViewService evaluacionViewService) {
        this.evaluacionRepository = evaluacionRepository;
        this.evaluacionViewService = evaluacionViewService;
    }

    @GetMapping("/evaluaciones")
    public String listar(@RequestParam(required = false) String usuario,
                         Model model) {
        List<Evaluacion> evals = (usuario == null || usuario.isBlank())
                ? evaluacionRepository.findAllByOrderByFechaDesc()
                : evaluacionRepository.findByDeportista_EmailOrderByFechaDesc(usuario.trim());

        List<EvaluacionViewDTO> view = evals.stream().map(evaluacionViewService::toView).collect(Collectors.toList());

        model.addAttribute("evaluaciones", view);
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
