package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Pago;
import com.integradorii.gimnasiov1.repository.PagoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * CONTROLADOR DE HISTORIAL DE PAGOS - VISTA ADMINISTRATIVA
 * 
 * Propósito: Gestionar el historial completo de pagos para el personal administrativo
 * Ruta base: /pagos
 * Acceso: Solo ADMINISTRADORES, RECEPCIONISTAS y ENTRENADORES (configurado en SecurityConfig)
 * Vista asociada: historial-pagos.html (versión administrativa)
 * 
 * Relación con tabla: pagos
 * 
 * IMPORTANTE: No confundir con PagosController (/cliente/pagos) que es para deportistas
 */
@Controller
@RequestMapping("/pagos")
public class PagoController {

    // Repositorio para acceso a datos de pagos en la BD
    private final PagoRepository pagoRepository;

    public PagoController(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    /**
     * Muestra el historial completo de pagos de TODOS los deportistas
     * 
     * Este método permite al personal administrativo:
     * - Ver todos los pagos del sistema
     * - Filtrar por estado (completado, pendiente, fallido)
     * - Buscar por nombre/DNI/email del deportista
     * - Ver estadísticas (total anual, cantidad de pagos)
     * 
     * @param estado Filtro opcional por estado: "completado", "pendiente", "fallido", "todos"
     * @param buscar Término de búsqueda opcional (busca en nombre, DNI, email)
     * @param model Modelo para pasar datos a la vista
     * @return Vista "historial-pagos.html"
     */
    @GetMapping
    public String historialPagos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String buscar,
            Model model) {

        // Normalizar parámetros de filtrado
        String estadoFiltro = (estado == null || estado.isBlank()) ? "todos" : estado;
        String buscarTerm = (buscar == null) ? "" : buscar.trim();

        // Obtener pagos filtrados desde la BD
        List<Pago> pagos = pagoRepository.searchAdmin(estadoFiltro, buscarTerm);

        // Calcular total de pagos completados en el año
        double totalAnio = pagos.stream()
                .filter(p -> "Completado".equalsIgnoreCase(p.getEstado()))
                .mapToDouble(Pago::getMonto)
                .sum();

        // Contar cantidad total de pagos
        long cantidadPagos = pagos.size();

        // Pasar datos a la vista
        model.addAttribute("pagos", pagos);                   // Lista de pagos filtrados
        model.addAttribute("totalAnio", totalAnio);           // Total recaudado
        model.addAttribute("cantidadPagos", cantidadPagos);   // Cantidad de pagos
        model.addAttribute("estadoActual", estadoFiltro);     // Estado seleccionado
        model.addAttribute("buscarActual", buscarTerm);       // Término de búsqueda

        return "historial-pagos";
    }
}
