package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Pago;
import com.integradorii.gimnasiov1.repository.PagoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoRepository pagoRepository;

    public PagoController(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @GetMapping
    public String historialPagos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String buscar,
            Model model) {

        String estadoFiltro = (estado == null || estado.isBlank()) ? "todos" : estado;
        String buscarTerm = (buscar == null) ? "" : buscar.trim();

        List<Pago> pagos = pagoRepository.searchAdmin(estadoFiltro, buscarTerm);

        double totalAnio = pagos.stream()
                .filter(p -> "Completado".equalsIgnoreCase(p.getEstado()))
                .mapToDouble(Pago::getMonto)
                .sum();

        long cantidadPagos = pagos.size();

        model.addAttribute("pagos", pagos);
        model.addAttribute("totalAnio", totalAnio);
        model.addAttribute("cantidadPagos", cantidadPagos);
        model.addAttribute("estadoActual", estadoFiltro);
        model.addAttribute("buscarActual", buscarTerm);

        return "historial-pagos";
    }
}
