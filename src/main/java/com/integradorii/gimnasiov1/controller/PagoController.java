package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Pago;
import com.integradorii.gimnasiov1.repository.PagoRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Muestra el historial completo de pagos de TODOS los deportistas con paginación
     * 
     * Este método permite al personal administrativo:
     * - Ver todos los pagos del sistema
     * - Filtrar por estado (completado, pendiente, fallido)
     * - Buscar por nombre/DNI/email del deportista
     * - Paginar resultados (50 por página)
     * 
     * @param estado Filtro opcional por estado: "completado", "pendiente", "fallido", "todos"
     * @param buscar Término de búsqueda opcional (busca en nombre, DNI, email)
     * @param page Número de página (0-indexed)
     * @param model Modelo para pasar datos a la vista
     * @return Vista "historial-pagos.html"
     */
    @GetMapping
    public String historialPagos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Normalizar parámetros de filtrado
        String estadoFiltro = (estado == null || estado.isBlank()) ? "todos" : estado;
        String buscarTerm = (buscar == null) ? "" : buscar.trim();
        int pageSize = 50;

        // Validar página
        if (page < 0) page = 0;

        // Obtener pagos filtrados desde la BD
        List<Pago> todosPagos = pagoRepository.searchAdmin(estadoFiltro, buscarTerm);

        // Calcular paginación
        int totalPagos = todosPagos.size();
        int totalPages = (int) Math.ceil((double) totalPagos / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page >= totalPages) page = totalPages - 1;

        int start = page * pageSize;
        int end = Math.min(start + pageSize, totalPagos);
        List<Pago> pagosPaginados = totalPagos == 0 ? new java.util.ArrayList<>() : todosPagos.subList(start, end);

        // Pasar datos a la vista
        model.addAttribute("pagos", pagosPaginados);          // Lista de pagos paginados
        model.addAttribute("estadoActual", estadoFiltro);     // Estado seleccionado
        model.addAttribute("buscarActual", buscarTerm);       // Término de búsqueda
        model.addAttribute("currentPage", page);              // Página actual
        model.addAttribute("totalPages", totalPages);         // Total de páginas
        model.addAttribute("pageSize", pageSize);             // Tamaño de página
        model.addAttribute("totalPagos", totalPagos);         // Total de pagos
        model.addAttribute("activeMenu", "pagos");           // Marcar menú activo en sidebar

        return "historial-pagos";
    }

    @GetMapping("/{id}/recibo")
    public String descargarRecibo(@PathVariable Long id, Model model, HttpServletResponse response) {
        Pago pago = pagoRepository.findById(id).orElse(null);
        if (pago == null) {
            return "redirect:/pagos";
        }

        Map<String, Object> gym = new HashMap<>();
        gym.put("name", "FitGym");
        gym.put("legalName", "FitGym");
        gym.put("taxId", "00000000000");
        gym.put("address", "Dirección del gimnasio");
        gym.put("phone", "000-000-000");
        gym.put("email", "info@fitgym.com");

        Map<String, Object> member = new HashMap<>();
        if (pago.getDeportista() != null) {
            member.put("name", pago.getDeportista().getNombreCompleto());
            member.put("documentType", "DNI");
            member.put("documentNumber", pago.getDeportista().getDni());
            member.put("code", String.format("CLI-%05d", pago.getDeportista().getId()));
            member.put("email", pago.getDeportista().getEmail());
        }

        Map<String, Object> payment = new HashMap<>();
        payment.put("code", pago.getCodigoPago());
        if (pago.getFecha() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            payment.put("dateTime", pago.getFecha().format(formatter));
        } else {
            payment.put("dateTime", "");
        }
        payment.put("statusLabel", pago.getEstado());
        payment.put("planName", pago.getPlanServicio());
        payment.put("method", pago.getMetodoPago());
        payment.put("reference", pago.getCodigoPago());
        payment.put("channel", "Sistema");
        String totalFormatted = formatMoney(pago.getMonto());
        payment.put("subtotalFormatted", totalFormatted);
        payment.put("discountFormatted", formatMoney(0.0));
        payment.put("taxFormatted", formatMoney(0.0));
        payment.put("totalFormatted", totalFormatted);

        model.addAttribute("gym", gym);
        model.addAttribute("member", member);
        model.addAttribute("payment", payment);

        response.setHeader("Content-Disposition", "attachment; filename=\"recibo-" + pago.getCodigoPago() + ".html\"");
        response.setContentType("text/html; charset=UTF-8");
        return "recibo-pago";
    }

    private String formatMoney(Double value) {
        if (value == null) {
            value = 0.0;
        }
        return String.format("S/ %.2f", value);
    }
}
