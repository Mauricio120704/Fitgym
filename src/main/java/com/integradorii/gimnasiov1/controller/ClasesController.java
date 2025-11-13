package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.dto.ClaseViewDTO;
import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.model.TipoClase;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.repository.ClaseRepository;
import com.integradorii.gimnasiov1.repository.TipoClaseRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import com.integradorii.gimnasiov1.repository.ReservaClaseRepository;
import com.integradorii.gimnasiov1.service.ClaseViewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador de Clases - Gestión de clases grupales
 * Ruta: /clases | Acceso: ADMIN, RECEPCIONISTA, ENTRENADOR
 * Tablas: clases, reserva_clase, usuarios (entrenadores)
 * CRUD completo + API REST JSON
 */
@Controller
public class ClasesController {

    private final ClaseRepository claseRepository;
    private final ReservaClaseRepository reservaClaseRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClaseViewService claseViewService;
    private final TipoClaseRepository tipoClaseRepository;

    public ClasesController(ClaseRepository claseRepository,
                            ReservaClaseRepository reservaClaseRepository,
                            UsuarioRepository usuarioRepository,
                            ClaseViewService claseViewService,
                            TipoClaseRepository tipoClaseRepository) {
        this.claseRepository = claseRepository;
        this.reservaClaseRepository = reservaClaseRepository;
        this.usuarioRepository = usuarioRepository;
        this.claseViewService = claseViewService;
        this.tipoClaseRepository = tipoClaseRepository;
    }

    /**
     * GET /clases - Lista todas las clases con estadísticas
     * Permite búsqueda por nombre/descripción
     * Muestra: total clases, clases llenas, total cupos
     */
    @GetMapping("/clases")
    public String listar(@RequestParam(required = false) String buscar,
                         @RequestParam(defaultValue = "todos") String periodo,
                         @RequestParam(required = false) String fechaInicio,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size,
                         Model model) {
        // Calcular rango de fechas según el periodo
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = null;
        LocalDate fin = null;

        // Manejar el caso donde se proporciona una fecha de inicio
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            try {
                inicio = LocalDate.parse(fechaInicio);
                if (periodo.equals("semana")) {
                    fin = inicio.plusDays(6);
                }
            } catch (Exception e) {
                // Si hay un error al parsear la fecha, usar la fecha actual
                inicio = hoy;
                fin = hoy;
            }
        }

        // Si no se proporcionó fecha de inicio o hubo un error, calcular según el período
        if (inicio == null) {
            switch (periodo) {
                case "hoy":
                    inicio = hoy;
                    fin = hoy;
                    break;
                case "semana":
                    inicio = hoy.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    fin = inicio.plusDays(6);
                    break;
                case "mes":
                    inicio = hoy.withDayOfMonth(1);
                    fin = hoy.withDayOfMonth(hoy.lengthOfMonth());
                    break;
                case "todos":
                default:
                    // No se establecen fechas para mostrar todas las clases
                    break;
            }
        }

        // Crear Pageable para la paginación
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Clase> clasesPaginadas;
        if (inicio != null && fin != null) {
            OffsetDateTime inicioOdt = inicio.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
            OffsetDateTime finOdt = fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
            
            clasesPaginadas = (buscar == null || buscar.isBlank())
                    ? claseRepository.findByFechaBetweenOrderByFechaAsc(inicioOdt, finOdt, pageable)
                    : claseRepository.findByFechaBetweenAndTipoLike(inicioOdt, finOdt, buscar.trim(), pageable);
        } else {
            // Para el caso de "todos", no aplicamos filtro de fecha
            clasesPaginadas = (buscar == null || buscar.isBlank())
                    ? claseRepository.findAllByOrderByFechaAsc(pageable)
                    : claseRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrderByFechaAsc(
                            buscar.trim(), buscar.trim(), pageable);
        }
        
        List<Clase> clases = clasesPaginadas.getContent();

        // Mapear a DTOs para la vista
        List<ClaseViewDTO> view = clases.stream().map(claseViewService::toView).collect(Collectors.toList());

        long totalClasesEnPagina = clases.size();
        long totalClases = clasesPaginadas.getTotalElements();
        long clasesLlenas = view.stream()
                .filter(v -> (v.getOcupadosPremium() + v.getOcupadosElite()) >= (v.getCuposPremium() + v.getCuposElite()))
                .count();
        int totalCupos = view.stream().mapToInt(v -> v.getCuposPremium() + v.getCuposElite()).sum();

        model.addAttribute("clases", view);
        model.addAttribute("totalClases", totalClases);
        model.addAttribute("clasesLlenas", clasesLlenas);
        model.addAttribute("totalCupos", totalCupos);
        model.addAttribute("buscarActual", buscar == null ? "" : buscar);
        // Pasar el período actual a la vista
        model.addAttribute("periodo", periodo == null ? "semana" : periodo);
        
        // Información de paginación
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clasesPaginadas.getTotalPages());
        model.addAttribute("totalElements", clasesPaginadas.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", clasesPaginadas.hasPrevious());
        model.addAttribute("hasNext", clasesPaginadas.hasNext());
        
        // Pasar fechas de la semana actual para navegación
        if (periodo != null && periodo.equals("semana") && inicio != null) {
            model.addAttribute("fechaInicio", inicio);
            model.addAttribute("fechaFin", fin);
            model.addAttribute("siguienteSemana", inicio.plusWeeks(1));
            model.addAttribute("semanaAnterior", inicio.minusWeeks(1));
        } else {
            // Valores por defecto para evitar errores en la vista
            model.addAttribute("fechaInicio", hoy);
            model.addAttribute("fechaFin", hoy);
            model.addAttribute("siguienteSemana", hoy.plusWeeks(1));
            model.addAttribute("semanaAnterior", hoy.minusWeeks(1));
        }
        // Pasar instructores activos (rol ENTRENADOR) para poblar el selector en el modal
        List<Usuario> instructores = usuarioRepository.findActiveEntrenadores();
        if (instructores != null) {
            instructores.sort(Comparator.comparing(Usuario::getNombre).thenComparing(Usuario::getApellido));
        }
        model.addAttribute("instructores", instructores);
        // Pasar tipos de clase activos para el selector
        model.addAttribute("tiposClase", tipoClaseRepository.findByActivoTrueOrderByNombreAsc());
        return "clases";
    }

    /**
     * POST /clases/crear - API REST: Crea nueva clase
     * Content-Type: application/json
     * Body: {nombre, duracion, cuposPremium, cuposElite, fecha, hora, instructor}
     */
    @PostMapping(value = "/clases/crear", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Map<String, Object> crear(@RequestBody Map<String, Object> body) {
        Clase c = new Clase();
        applyFromBody(c, body);
        claseRepository.save(c);
        return toJson(c);
    }

    /**
     * PUT /clases/{id} - API REST: Actualiza clase existente
     * Content-Type: application/json
     */
    @PutMapping(value = "/clases/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Map<String, Object> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Clase c = claseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
        applyFromBody(c, body);
        claseRepository.save(c);
        return toJson(c);
    }

    /**
     * DELETE /clases/{id} - API REST: Elimina clase
     * Elimina primero las reservas asociadas
     */
    @DeleteMapping("/clases/{id}")
    @ResponseBody
    public void eliminar(@PathVariable Long id) {
        reservaClaseRepository.deleteByClase_Id(id);
        claseRepository.deleteById(id);
    }

    // Mapea datos del JSON al objeto Clase
private void applyFromBody(Clase c, Map<String, Object> body) {
    c.setDescripcion(null);
    c.setDuracionMinutos(parseInt(body.get("duracion"), 60));
    c.setCapacidad(parseInt(body.get("cuposPremium"), 0) + parseInt(body.get("cuposElite"), 0));
    // Tipo de clase (obligatorio en BD)
    Long tipoId = null;
    try { Object t = body.get("tipoClaseId"); if (t != null) tipoId = Long.parseLong(String.valueOf(t)); } catch (Exception ignored) {}
    TipoClase tipo = null;
    if (tipoId != null) {
        tipo = tipoClaseRepository.findById(tipoId).orElse(null);
    }
    if (tipo == null) {
        // fallback: si viene nombre de tipo nuevo
        String tipoNombre = String.valueOf(body.getOrDefault("tipoClaseNombre", "")).trim();
        if (!tipoNombre.isBlank()) {
            tipo = tipoClaseRepository.findByNombreIgnoreCase(tipoNombre).orElseGet(() -> {
                TipoClase nt = new TipoClase();
                nt.setNombre(tipoNombre);
                nt.setActivo(true);
                return tipoClaseRepository.save(nt);
            });
        }
    }
    if (tipo == null) {
        throw new IllegalArgumentException("Tipo de clase requerido");
    }
    c.setTipoClase(tipo);
    // El nombre de la clase se deriva del tipo seleccionado
    c.setNombre(tipo.getNombre());
    
    String fechaStr = String.valueOf(body.getOrDefault("fecha", ""));
    String horaStr = String.valueOf(body.getOrDefault("hora", "00:00"));
    
    if (!fechaStr.isBlank()) {
        try {
            // Parsear fecha y hora en la zona horaria local
            LocalDate ld = LocalDate.parse(fechaStr);
            LocalTime lt = horaStr.isBlank() ? LocalTime.of(0, 0) : LocalTime.parse(horaStr);
            
            // Crear OffsetDateTime usando la zona horaria del sistema
            ZoneId zone = ZoneId.systemDefault();
            ZonedDateTime zdt = ZonedDateTime.of(ld, lt, zone);
            OffsetDateTime odt = zdt.toOffsetDateTime();
            
            c.setFecha(odt);
            System.out.println("Hora guardada (UTC): " + odt);
            System.out.println("Hora local: " + zdt);
        } catch (Exception e) {
            System.err.println("Error al parsear fecha/hora: " + e.getMessage());
        }
    }
    
    c.setEstado("Programada");
    
    // Buscar entrenador por nombre y apellido en tabla usuarios
    String instructor = String.valueOf(body.getOrDefault("instructor", ""));
    if (!instructor.isBlank()) {
        String[] parts = instructor.trim().split(" ", 2);
        String nombre = parts[0];
        String apellido = parts.length > 1 ? parts[1] : "";
        // Buscar en tabla usuarios (rol ENTRENADOR)
        Usuario entren = usuarioRepository.findActiveEntrenadores().stream()
                .filter(u -> u.getNombre().equalsIgnoreCase(nombre) && 
                            (apellido.isBlank() || u.getApellido().equalsIgnoreCase(apellido)))
                .findFirst().orElse(null);
        c.setEntrenador(entren);
    } else {
        c.setEntrenador(null);
    }
}

    // Convierte Object a int con valor por defecto
    private int parseInt(Object v, int def) {
        try { return v == null ? def : Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
    }

    // Convierte Clase a JSON para respuesta API
    private Map<String, Object> toJson(Clase c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("nombre", c.getNombre());
        m.put("duracion", c.getDuracionMinutos());
        m.put("capacidad", c.getCapacidad());
        OffsetDateTime odt = c.getFecha();
        if (odt != null) {
            m.put("fecha", odt.toLocalDate().toString());
            m.put("hora", odt.toLocalTime().toString());
        }
        m.put("estado", c.getEstado());
        return m;
    }
}
