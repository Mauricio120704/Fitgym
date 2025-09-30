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
}
