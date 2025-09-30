package com.integradorii.gimnasiov1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pagos")
public class PagoController {
    
    @GetMapping
    public String historialPagos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String buscar,
            Model model) {
        
        // Datos de ejemplo en memoria (sin base de datos)
        List<Map<String, Object>> todosPagos = new ArrayList<>();
        
        Map<String, Object> pago1 = new HashMap<>();
        pago1.put("codigoPago", "PAY-001");
        pago1.put("fecha", LocalDate.of(2024, 1, 14));
        pago1.put("planServicio", "Membresía Premium");
        pago1.put("metodoPago", "Tarjeta de Crédito");
        pago1.put("monto", 899.00);
        pago1.put("estado", "Completado");
        
        Map<String, Object> pago2 = new HashMap<>();
        pago2.put("codigoPago", "PAY-002");
        pago2.put("fecha", LocalDate.of(2023, 12, 14));
        pago2.put("planServicio", "Membresía Premium");
        pago2.put("metodoPago", "Tarjeta de Crédito");
        pago2.put("monto", 899.00);
        pago2.put("estado", "Completado");
        
        Map<String, Object> pago3 = new HashMap<>();
        pago3.put("codigoPago", "PAY-003");
        pago3.put("fecha", LocalDate.of(2023, 11, 14));
        pago3.put("planServicio", "Membresía Premium");
        pago3.put("metodoPago", "Transferencia");
        pago3.put("monto", 899.00);
        pago3.put("estado", "Completado");
        
        Map<String, Object> pago4 = new HashMap<>();
        pago4.put("codigoPago", "PAY-004");
        pago4.put("fecha", LocalDate.of(2024, 1, 19));
        pago4.put("planServicio", "Entrenamiento Personal");
        pago4.put("metodoPago", "Tarjeta de Débito");
        pago4.put("monto", 1209.00);
        pago4.put("estado", "Pendiente");
        
        Map<String, Object> pago5 = new HashMap<>();
        pago5.put("codigoPago", "PAY-005");
        pago5.put("fecha", LocalDate.of(2024, 1, 5));
        pago5.put("planServicio", "Clase de Yoga");
        pago5.put("metodoPago", "Efectivo");
        pago5.put("monto", 250.00);
        pago5.put("estado", "Fallido");
        
        todosPagos.add(pago1);
        todosPagos.add(pago2);
        todosPagos.add(pago3);
        todosPagos.add(pago4);
        todosPagos.add(pago5);
        
        // Aplicar filtros por estado
        List<Map<String, Object>> pagosFiltrados = todosPagos;
        if (estado != null && !estado.isEmpty() && !"todos".equals(estado)) {
            pagosFiltrados = todosPagos.stream()
                    .filter(p -> estado.equalsIgnoreCase((String) p.get("estado")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Aplicar búsqueda
        if (buscar != null && !buscar.trim().isEmpty()) {
            String buscarLower = buscar.toLowerCase().trim();
            pagosFiltrados = pagosFiltrados.stream()
                    .filter(p -> 
                        ((String) p.get("codigoPago")).toLowerCase().contains(buscarLower) ||
                        ((String) p.get("planServicio")).toLowerCase().contains(buscarLower) ||
                        ((String) p.get("metodoPago")).toLowerCase().contains(buscarLower)
                    )
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Calcular estadísticas totales
        double totalAnio = todosPagos.stream()
                .filter(p -> "Completado".equals(p.get("estado")))
                .mapToDouble(p -> (Double) p.get("monto"))
                .sum();
        
        long cantidadPagos = todosPagos.stream()
                .filter(p -> "Completado".equals(p.get("estado")))
                .count();
        
        model.addAttribute("pagos", pagosFiltrados);
        model.addAttribute("totalAnio", totalAnio);
        model.addAttribute("cantidadPagos", cantidadPagos);
        model.addAttribute("estadoActual", estado != null ? estado : "todos");
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        
        return "historial-pagos";
    }
    
    @GetMapping("/suscripciones")
    public String suscripciones(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String buscar,
            Model model) {
        
        // Datos de ejemplo de suscripciones
        List<Map<String, Object>> todasSuscripciones = new ArrayList<>();
        
        Map<String, Object> suscripcion1 = new HashMap<>();
        suscripcion1.put("id", "SUB-001");
        suscripcion1.put("miembro", "Juan Pérez");
        suscripcion1.put("plan", "Membresía Premium");
        suscripcion1.put("precio", 899.00);
        suscripcion1.put("frecuencia", "Mensual");
        suscripcion1.put("fechaInicio", LocalDate.of(2024, 1, 14));
        suscripcion1.put("proximoPago", LocalDate.of(2025, 2, 14));
        suscripcion1.put("estado", "Activa");
        
        Map<String, Object> suscripcion2 = new HashMap<>();
        suscripcion2.put("id", "SUB-002");
        suscripcion2.put("miembro", "María García");
        suscripcion2.put("plan", "Membresía Básica");
        suscripcion2.put("precio", 599.00);
        suscripcion2.put("frecuencia", "Mensual");
        suscripcion2.put("fechaInicio", LocalDate.of(2023, 6, 1));
        suscripcion2.put("proximoPago", LocalDate.of(2025, 2, 1));
        suscripcion2.put("estado", "Activa");
        
        Map<String, Object> suscripcion3 = new HashMap<>();
        suscripcion3.put("id", "SUB-003");
        suscripcion3.put("miembro", "Carlos López");
        suscripcion3.put("plan", "Membresía VIP");
        suscripcion3.put("precio", 1299.00);
        suscripcion3.put("frecuencia", "Mensual");
        suscripcion3.put("fechaInicio", LocalDate.of(2024, 3, 10));
        suscripcion3.put("proximoPago", LocalDate.of(2025, 2, 10));
        suscripcion3.put("estado", "Activa");
        
        Map<String, Object> suscripcion4 = new HashMap<>();
        suscripcion4.put("id", "SUB-004");
        suscripcion4.put("miembro", "Ana Martínez");
        suscripcion4.put("plan", "Membresía Premium");
        suscripcion4.put("precio", 899.00);
        suscripcion4.put("frecuencia", "Mensual");
        suscripcion4.put("fechaInicio", LocalDate.of(2023, 8, 20));
        suscripcion4.put("proximoPago", LocalDate.of(2024, 12, 20));
        suscripcion4.put("estado", "Cancelada");
        
        todasSuscripciones.add(suscripcion1);
        todasSuscripciones.add(suscripcion2);
        todasSuscripciones.add(suscripcion3);
        todasSuscripciones.add(suscripcion4);
        
        // Aplicar filtros por estado
        List<Map<String, Object>> suscripcionesFiltradas = todasSuscripciones;
        if (estado != null && !estado.isEmpty() && !"todas".equals(estado)) {
            suscripcionesFiltradas = todasSuscripciones.stream()
                    .filter(s -> estado.equalsIgnoreCase((String) s.get("estado")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Aplicar búsqueda
        if (buscar != null && !buscar.trim().isEmpty()) {
            String buscarLower = buscar.toLowerCase().trim();
            suscripcionesFiltradas = suscripcionesFiltradas.stream()
                    .filter(s -> 
                        ((String) s.get("id")).toLowerCase().contains(buscarLower) ||
                        ((String) s.get("miembro")).toLowerCase().contains(buscarLower) ||
                        ((String) s.get("plan")).toLowerCase().contains(buscarLower)
                    )
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Calcular estadísticas
        long totalActivas = todasSuscripciones.stream()
                .filter(s -> "Activa".equals(s.get("estado")))
                .count();
        
        double ingresoMensual = todasSuscripciones.stream()
                .filter(s -> "Activa".equals(s.get("estado")))
                .mapToDouble(s -> (Double) s.get("precio"))
                .sum();
        
        model.addAttribute("suscripciones", suscripcionesFiltradas);
        model.addAttribute("totalSuscripciones", todasSuscripciones.size());
        model.addAttribute("totalActivas", totalActivas);
        model.addAttribute("ingresoMensual", ingresoMensual);
        model.addAttribute("estadoActual", estado != null ? estado : "todas");
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        
        return "suscripciones";
    }
}
