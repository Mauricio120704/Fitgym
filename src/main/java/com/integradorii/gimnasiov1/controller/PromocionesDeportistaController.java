package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Promocion;
import com.integradorii.gimnasiov1.repository.PromocionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vista de promociones para el deportista.
 *
 * Muestra únicamente promociones vigentes (por fecha y estado ACTIVE) y las
 * separa en dos grupos para la vista:
 * - `promocionesMembresia`: promociones asociadas a planes/membresías.
 * - `promocionesClases`: promociones aplicables a clases individuales.
 *
 * La vista resultante puede usarse como punto de entrada al flujo de checkout,
 * donde el usuario selecciona una promoción y luego continúa con el pago.
 */
@Controller
@RequestMapping("/deportista")
public class PromocionesDeportistaController {

    private final PromocionRepository promocionRepository;

    public PromocionesDeportistaController(PromocionRepository promocionRepository) {
        this.promocionRepository = promocionRepository;
    }

    /**
     * GET /deportista/promociones
     *
     * Carga en el modelo todas las promociones vigentes para el día de hoy y
     * las organiza en listas separadas para facilitar su renderizado en la UI.
     *
     * También marca el menú activo para que la vista de layout resalte la sección
     * de promociones del cliente.
     */
    @GetMapping("/promociones")
    public String promocionesVigentes(Model model) {
        LocalDate hoy = LocalDate.now();
        List<Promocion> base = promocionRepository.findByEstado(Promocion.Estado.ACTIVE);
        List<Promocion> vigentes = base.stream()
                .filter(p -> (p.getFechaInicio() == null || !p.getFechaInicio().isAfter(hoy))
                        && (p.getFechaFin() == null || !p.getFechaFin().isBefore(hoy)))
                .collect(Collectors.toList());

        List<Promocion> promocionesMembresia = vigentes.stream()
                .filter(p -> p.getMembresias() != null && !p.getMembresias().isEmpty())
                .collect(Collectors.toList());

        List<Promocion> promocionesClases = vigentes.stream()
                .filter(p -> p.getMembresias() == null || p.getMembresias().isEmpty())
                .collect(Collectors.toList());

        model.addAttribute("promociones", vigentes);
        model.addAttribute("promocionesMembresia", promocionesMembresia);
        model.addAttribute("promocionesClases", promocionesClases);
        model.addAttribute("hoy", hoy);
        model.addAttribute("activeMenu", "promociones-cliente");
        return "deportista/promociones";
    }
}
