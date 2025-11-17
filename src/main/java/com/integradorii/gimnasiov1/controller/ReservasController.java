package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.ReservaClase;
import com.integradorii.gimnasiov1.repository.ClaseRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.ReservaClaseRepository;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador de Reservas - Sistema de reserva de clases
 * Ruta: /reservas, /api/reservas/** | Acceso: ROLE_CLIENTE (deportistas)
 * Tablas: clases, reserva_clase, personas
 */
@Controller
public class ReservasController {

    private final ClaseRepository claseRepository;
    private final ReservaClaseRepository reservaClaseRepository;
    private final PersonaRepository personaRepository;
    private final SuscripcionRepository suscripcionRepository;

    public ReservasController(ClaseRepository claseRepository,
                              ReservaClaseRepository reservaClaseRepository,
                              PersonaRepository personaRepository,
                              SuscripcionRepository suscripcionRepository) {
        this.claseRepository = claseRepository;
        this.reservaClaseRepository = reservaClaseRepository;
        this.personaRepository = personaRepository;
        this.suscripcionRepository = suscripcionRepository;
    }

    /**
     * GET /reservas - Página de reservas de clases
     * Solo accesible para deportistas autenticados
     */
    @GetMapping("/reservas")
    public String reservasView(Model model,
                               @AuthenticationPrincipal UserDetails user) {
        // Verificar autenticación
        if (user == null) return "redirect:/login";
        
        // Buscar deportista por email
        Persona persona = personaRepository.findByEmail(user.getUsername()).orElse(null);
        if (persona == null) return "redirect:/login";
        
        // Pasar datos del deportista a la vista
        model.addAttribute("usuario", persona);
        return "reservas";
    }

    /**
     * GET /api/reservas/clases - API: Obtiene clases disponibles por fecha
     * Parámetros: fecha (YYYY-MM-DD), tipo (opcional: yoga, spinning, etc.)
     * Retorna: JSON con lista de clases del día
     */
    @GetMapping("/api/reservas/clases")
    @ResponseBody
    public List<Map<String, Object>> clasesPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String tipo) {
        
        // Convertir fecha a rango de 24 horas
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime inicio = fecha.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime fin = fecha.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        // Buscar clases con o sin filtro de tipo
        List<Clase> clases;
        if (tipo != null && !tipo.equalsIgnoreCase("todas") && !tipo.isBlank()) {
            // Filtrar por tipo específico (yoga, spinning, etc.)
            clases = claseRepository.findByFechaBetweenAndTipoLike(inicio, fin, tipo.trim());
        } else {
            // Traer todas las clases del día
            clases = claseRepository.findByFechaBetweenOrderByFechaAsc(inicio, fin);
        }

        // Convertir cada clase a formato JSON para el frontend
        return clases.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            
            // Datos básicos de la clase
            m.put("id", c.getId());
            m.put("nombre", c.getNombre());
            
            // Derivar tipo desde el nombre (yoga, spinning, etc.)
            String tipoDerivado = derivarTipo(c.getNombre(), c.getDescripcion());
            m.put("tipo", tipoDerivado);
            
            // Calcular horario (inicio y fin)
            OffsetDateTime odt = c.getFecha();
            LocalTime ini = odt.toLocalTime();
            LocalTime finH = ini.plusMinutes(Optional.ofNullable(c.getDuracionMinutos()).orElse(60));
            m.put("horaInicio", ini.toString());
            m.put("horaFin", finH.toString());
            
            // Nombre del instructor
            String instructor = c.getEntrenador() == null ? "-" : 
                (c.getEntrenador().getNombre() + " " + Optional.ofNullable(c.getEntrenador().getApellido()).orElse(""));
            m.put("instructor", instructor.trim());
            
            // Cupos disponibles (ocupados/total)
            long ocupados = reservaClaseRepository.countOcupados(c.getId());
            m.put("cupos", ocupados + "/" + c.getCapacidad());
            
            // Rating fijo (placeholder)
            m.put("rating", "4.8");
            m.put("esPago", Boolean.TRUE.equals(c.getEsPago()));
            m.put("precio", c.getPrecio());
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * GET /api/reservas/proximas - API: Obtiene próximas reservas del deportista
     * Retorna solo clases futuras (desde ahora en adelante)
     */
    @GetMapping("/api/reservas/proximas")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> proximas(
            @AuthenticationPrincipal UserDetails user) {
        
        // Validar autenticación
        if (user == null) return ResponseEntity.status(401).build();
        
        // Buscar deportista
        Persona p = personaRepository.findByEmail(user.getUsername()).orElse(null);
        if (p == null) return ResponseEntity.status(404).build();
        
        // Obtener reservas desde ahora en adelante
        OffsetDateTime desde = OffsetDateTime.now();
        List<ReservaClase> reservas = reservaClaseRepository.findProximasByDeportista(p.getId(), desde);
        
        // Convertir a JSON simplificado
        List<Map<String, Object>> out = reservas.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            Clase c = r.getClase();
            m.put("nombre", c.getNombre());
            m.put("fecha", c.getFecha().toLocalDate().toString());
            m.put("hora", c.getFecha().toLocalTime().toString());
            return m;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(out);
    }

    /**
     * POST /api/reservas - API: Crea nueva reserva de clase
     * Parámetro: claseId
     * Validaciones: autenticación, estado clase, fecha, duplicados, capacidad
     */
    @PostMapping("/api/reservas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearReserva(
            @RequestParam("claseId") long claseId,
            @AuthenticationPrincipal UserDetails user) {
        
        Map<String, Object> res = new HashMap<>();
        
        // Validación 1: Usuario autenticado
        if (user == null) {
            res.put("success", false);
            res.put("message", "No autenticado");
            return ResponseEntity.status(401).body(res);
        }

        // Validación 2: Deportista existe
        Persona deportista = personaRepository.findByEmail(user.getUsername()).orElse(null);
        if (deportista == null) {
            res.put("success", false);
            res.put("message", "Usuario no encontrado");
            return ResponseEntity.status(404).body(res);
        }

        // Validación 3: Clase existe
        Clase clase = claseRepository.findById(claseId).orElse(null);
        if (clase == null) {
            res.put("success", false);
            res.put("message", "Clase no encontrada");
            return ResponseEntity.status(404).body(res);
        }

        // Validación 4: Estado de la clase (debe estar "Programada")
        if (!"Programada".equalsIgnoreCase(clase.getEstado())) {
            res.put("success", false);
            res.put("message", "La clase no está disponible para reserva");
            return ResponseEntity.badRequest().body(res);
        }

        // Validación 5: Fecha válida (debe ser futura)
        OffsetDateTime ahora = OffsetDateTime.now();
        if (clase.getFecha() == null || !clase.getFecha().isAfter(ahora)) {
            res.put("success", false);
            res.put("message", "La clase ya ocurrió o no tiene fecha válida");
            return ResponseEntity.badRequest().body(res);
        }

        // Validación 6: Evitar duplicados (deportista ya reservó esta clase)
        boolean yaReservada = reservaClaseRepository.existsByClase_IdAndDeportista_IdAndEstadoNot(
            clase.getId(), deportista.getId(), "Cancelado");
        if (yaReservada) {
            res.put("success", false);
            res.put("message", "Ya tienes una reserva para esta clase");
            return ResponseEntity.badRequest().body(res);
        }

        // Validación 7: Verificar capacidad disponible
        long ocupados = reservaClaseRepository.countOcupados(clase.getId());
        if (ocupados >= clase.getCapacidad()) {
            res.put("success", false);
            res.put("message", "La clase está completa");
            return ResponseEntity.badRequest().body(res);
        }

        // Validación 8: Manejar clases pagadas según membresía y bandera "para todos"
        if (Boolean.TRUE.equals(clase.getEsPago())) {
            boolean requierePago = false;

            var suscripcionActivaOpt = suscripcionRepository.findActiveByDeportistaId(deportista.getId());
            boolean esPremiumOElite = false;
            if (suscripcionActivaOpt.isPresent() && suscripcionActivaOpt.get().getPlan() != null) {
                String nombrePlan = Optional.ofNullable(suscripcionActivaOpt.get().getPlan().getNombre())
                        .orElse("").toLowerCase();
                // Premium o Elite reservan gratis
                if (nombrePlan.contains("premium") || nombrePlan.contains("elite")) {
                    esPremiumOElite = true;
                }
            }

            if (esPremiumOElite) {
                // Miembros Premium/Elite reservan sin pago adicional
                requierePago = false;
            } else {
                // Básico o sin membresía: solo pueden reservar pagando si la clase es "para todos"
                if (!Boolean.TRUE.equals(clase.getParaTodos())) {
                    res.put("success", false);
                    res.put("message", "Esta clase es solo para miembros.");
                    return ResponseEntity.badRequest().body(res);
                }
                requierePago = true;
            }

            if (requierePago) {
                res.put("success", false);
                res.put("requiresPayment", true);
                res.put("message", "Esta clase requiere pago para tu tipo de membresía.");
                res.put("checkoutUrl", "/checkout/clase?claseId=" + clase.getId());
                return ResponseEntity.ok(res);
            }
        }

        // Todas las validaciones pasaron, crear la reserva
        ReservaClase r = new ReservaClase();
        r.setClase(clase);
        r.setDeportista(deportista);
        r.setReservadoEn(ahora);
        r.setEstado("Reservado"); // Estados: Reservado, Asistió, Cancelado
        reservaClaseRepository.save(r);

        // Responder con éxito
        res.put("success", true);
        res.put("message", "Reserva creada correctamente");
        res.put("reservaId", r.getId());
        return ResponseEntity.ok(res);
    }

    /**
     * Método auxiliar: Deriva el tipo de clase desde el nombre
     * Ejemplos: "Yoga Principiantes" → "yoga", "Spinning Avanzado" → "spinning"
     * Si no hay nombre, intenta con descripción, sino retorna "general"
     */
    private String derivarTipo(String nombre, String descripcion) {
        // Intentar derivar desde el nombre
        if (nombre != null && !nombre.isBlank()) {
            String first = nombre.trim().split("\\s+")[0];
            return first.toLowerCase();
        }
        // Si no hay nombre, intentar desde descripción
        if (descripcion != null && !descripcion.isBlank()) {
            String first = descripcion.trim().split("\\s+")[0];
            return first.toLowerCase();
        }
        // Tipo por defecto
        return "general";
    }
}
