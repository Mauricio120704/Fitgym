package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Pago;
import com.integradorii.gimnasiov1.model.Suscripcion;
import com.integradorii.gimnasiov1.repository.PagoRepository;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * CONTROLADOR DE HISTORIAL DE PAGOS - VISTA DEPORTISTA/CLIENTE
 * 
 * Propósito: Permitir que los deportistas/clientes vean su propio historial de pagos
 * Ruta base: /cliente/pagos
 * Acceso: Solo usuarios con rol CLIENTE (deportistas autenticados)
 * Vista asociada: historial-pagos.html (versión cliente)
 * 
 * Relación con tablas: pagos, suscripciones, planes
 * 
 * IMPORTANTE: No confundir con PagoController (/pagos) que es para personal administrativo
 * 
 * Funcionalidades:
 * - Muestra solo los pagos del deportista autenticado
 * - Muestra información de su suscripción activa
 * - Permite filtrar y buscar en sus propios pagos
 */
@Controller
public class PagosController {

    // Repositorio para acceso a datos de pagos en la BD
    private final PagoRepository pagoRepository;
    
    // Repositorio para acceso a datos de suscripciones en la BD
    private final SuscripcionRepository suscripcionRepository;

    public PagosController(PagoRepository pagoRepository, SuscripcionRepository suscripcionRepository) {
        this.pagoRepository = pagoRepository;
        this.suscripcionRepository = suscripcionRepository;
    }

    /**
     * Muestra el historial de pagos del deportista autenticado
     * 
     * Este método:
     * - Obtiene el email del deportista desde la sesión de seguridad
     * - Busca solo los pagos de ese deportista
     * - Calcula estadísticas personales (total pagado, cantidad de pagos)
     * - Obtiene información de la suscripción activa si existe
     * 
     * @param userDetails Información del usuario autenticado (inyectada por Spring Security)
     * @param buscar Término de búsqueda opcional en sus propios pagos
     * @param estado Filtro por estado: "completado", "pendiente", "fallido", "todos"
     * @param model Modelo para pasar datos a la vista
     * @return Vista "historial-pagos.html" (versión cliente)
     */
    @GetMapping("/cliente/pagos")
    public String historialPagos(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            Model model) {

        // Obtener el email del usuario autenticado desde Spring Security
        String usuario = null;
        if (userDetails != null) {
            usuario = userDetails.getUsername(); // El username es el email en nuestro sistema
        }

        // Inicializar variables con valores por defecto
        List<Pago> pagos = Collections.emptyList();
        Double totalAnio = 0.0;
        long cantidadPagos = 0L;
        Map<String, Object> suscripcionActualMap = null;

        // Solo procesar si hay un usuario autenticado
        if (usuario != null && !usuario.isBlank()) {
            // Buscar pagos del deportista filtrados por estado y término de búsqueda
            pagos = pagoRepository.searchByEmail(usuario, estado, buscar);
            
            // Calcular total pagado en el año por este deportista
            totalAnio = Optional.ofNullable(pagoRepository.totalAnioCompletado(usuario)).orElse(0.0);
            
            // Contar cantidad de pagos del deportista
            cantidadPagos = pagos.size();

            // Obtener información de la suscripción activa del deportista
            Suscripcion s = suscripcionRepository
                    .findFirstByDeportista_EmailAndEstadoOrderByProximoPagoAsc(usuario, "Activa")
                    .orElse(null);
                    
            // Si tiene suscripción activa, preparar sus datos para la vista
            if (s != null) {
                suscripcionActualMap = new HashMap<>();
                suscripcionActualMap.put("estado", s.getEstado());
                suscripcionActualMap.put("proximoPago", s.getProximoPago());
                
                // Agregar información del plan si existe
                if (s.getPlan() != null) {
                    suscripcionActualMap.put("plan", s.getPlan().getNombre());
                    suscripcionActualMap.put("precio", s.getPlan().getPrecio());
                    suscripcionActualMap.put("frecuencia", s.getPlan().getFrecuencia());
                }
            }
        }

        // Pasar todos los datos a la vista
        model.addAttribute("pagos", pagos);                                           // Lista de pagos del deportista
        model.addAttribute("buscarActual", buscar != null ? buscar : "");            // Término de búsqueda
        model.addAttribute("estadoActual", estado != null ? estado : "todos");       // Estado seleccionado
        model.addAttribute("totalAnio", totalAnio);                                   // Total pagado en el año
        model.addAttribute("cantidadPagos", cantidadPagos);                          // Cantidad de pagos
        model.addAttribute("suscripcionActual", suscripcionActualMap);               // Info de suscripción activa
        model.addAttribute("activeMenu", "pagos");                                   // Marcar menú activo

        return "historial-pagos";
    }
}
