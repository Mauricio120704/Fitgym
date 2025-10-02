package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Pago;
import com.integradorii.gimnasiov1.model.Suscripcion;
import com.integradorii.gimnasiov1.repository.PagoRepository;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class PagosController {

    private final PagoRepository pagoRepository;
    private final SuscripcionRepository suscripcionRepository;

    public PagosController(PagoRepository pagoRepository, SuscripcionRepository suscripcionRepository) {
        this.pagoRepository = pagoRepository;
        this.suscripcionRepository = suscripcionRepository;
    }

    @GetMapping("/cliente/pagos")
    public String historialPagos(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            Model model) {

        List<Pago> pagos = Collections.emptyList();
        Double totalAnio = 0.0;
        long cantidadPagos = 0L;
        Map<String, Object> suscripcionActualMap = null;

        if (usuario != null && !usuario.isBlank()) {
            pagos = pagoRepository.searchByEmail(usuario, estado, buscar);
            totalAnio = Optional.ofNullable(pagoRepository.totalAnioCompletado(usuario)).orElse(0.0);
            cantidadPagos = pagos.size();

            Suscripcion s = suscripcionRepository
                    .findFirstByDeportista_EmailAndEstadoOrderByProximoPagoAsc(usuario, "Activa")
                    .orElse(null);
            if (s != null) {
                suscripcionActualMap = new HashMap<>();
                suscripcionActualMap.put("estado", s.getEstado());
                suscripcionActualMap.put("proximoPago", s.getProximoPago());
                if (s.getPlan() != null) {
                    suscripcionActualMap.put("plan", s.getPlan().getNombre());
                    suscripcionActualMap.put("precio", s.getPlan().getPrecio());
                    suscripcionActualMap.put("frecuencia", s.getPlan().getFrecuencia());
                }
            }
        }

        model.addAttribute("pagos", pagos);
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        model.addAttribute("estadoActual", estado != null ? estado : "todos");
        model.addAttribute("totalAnio", totalAnio);
        model.addAttribute("cantidadPagos", cantidadPagos);
        model.addAttribute("suscripcionActual", suscripcionActualMap);

        return "historial-pagos";
    }
}
