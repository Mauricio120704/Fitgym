package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.dto.ClaseViewDTO;
import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.model.TipoClase;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.repository.ClaseRepository;
import com.integradorii.gimnasiov1.repository.TipoClaseRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import com.integradorii.gimnasiov1.repository.ReservaClaseRepository;
import com.integradorii.gimnasiov1.repository.ClaseCalificacionRepository;
import com.integradorii.gimnasiov1.repository.PromocionRepository;
import com.integradorii.gimnasiov1.service.ClaseViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

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
    private final ClaseCalificacionRepository claseCalificacionRepository;
    private final PromocionRepository promocionRepository;

    public ClasesController(ClaseRepository claseRepository,
                            ReservaClaseRepository reservaClaseRepository,
                            UsuarioRepository usuarioRepository,
                            ClaseViewService claseViewService,
                            TipoClaseRepository tipoClaseRepository,
                            ClaseCalificacionRepository claseCalificacionRepository,
                            PromocionRepository promocionRepository) {
        this.claseRepository = claseRepository;
        this.reservaClaseRepository = reservaClaseRepository;
        this.usuarioRepository = usuarioRepository;
        this.claseViewService = claseViewService;
        this.tipoClaseRepository = tipoClaseRepository;
        this.claseCalificacionRepository = claseCalificacionRepository;
        this.promocionRepository = promocionRepository;
    }

    /**
     * GET /clases - Lista todas las clases con estadísticas
     * Permite búsqueda por nombre/descripción
     * Muestra: total clases, clases llenas, total cupos
     */
    @GetMapping("/clases")
    public String listar(@RequestParam(required = false) String buscar,
                         @RequestParam(defaultValue = "hoy") String periodo,
                         @RequestParam(required = false) String fechaInicio,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        // Calcular rango de fechas según el periodo
        LocalDate hoy = LocalDate.now();
        if (periodo == null || periodo.isBlank()) {
            periodo = "hoy";
        }
        LocalDate inicio = null;
        LocalDate fin = null;

        // Calcular rango de fechas según el período
        switch (periodo) {
            case "hoy":
                inicio = hoy;
                fin = hoy;
                break;
            case "semana":
                // Si hay fechaInicio, usarla; si no, usar la semana actual
                if (fechaInicio != null && !fechaInicio.isEmpty()) {
                    try {
                        inicio = LocalDate.parse(fechaInicio);
                    } catch (Exception e) {
                        inicio = hoy.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    }
                } else {
                    inicio = hoy.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                }
                fin = inicio.plusDays(6);
                break;
            case "mes":
                inicio = hoy.withDayOfMonth(1);
                fin = hoy.withDayOfMonth(hoy.lengthOfMonth());
                break;
            case "todos":
            default:
                // No se establecen fechas para mostrar todas las clases
                inicio = null;
                fin = null;
                break;
        }

        List<Clase> clases;
        if (inicio != null && fin != null) {
            OffsetDateTime inicioOdt = inicio.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
            OffsetDateTime finOdt = fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();

            clases = (buscar == null || buscar.isBlank())
                    ? claseRepository.findByFechaBetweenOrderByFechaAsc(inicioOdt, finOdt)
                    : claseRepository.findByFechaBetweenAndTipoLike(inicioOdt, finOdt, buscar.trim());
        } else {
            // Para el caso de "todos", no aplicamos filtro de fecha
            clases = (buscar == null || buscar.isBlank())
                    ? claseRepository.findAllByOrderByFechaAsc()
                    : claseRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrderByFechaAsc(
                            buscar.trim(), buscar.trim());
        }

        // Mapear a DTOs para la vista y reforzar el filtro de fechas en memoria
        final LocalDate filtroInicio = inicio;
        final LocalDate filtroFin = fin;
        List<ClaseViewDTO> view = clases.stream()
                .map(claseViewService::toView)
                .filter(v -> {
                    if (filtroInicio == null || filtroFin == null || v.getFecha() == null) {
                        return true; // sin filtro de fecha ("todos")
                    }
                    LocalDate f = v.getFecha();
                    // Incluir fechas que están dentro del rango [filtroInicio, filtroFin]
                    return !f.isBefore(filtroInicio) && !f.isAfter(filtroFin);
                })
                .collect(Collectors.toList());

        // Calcular totales basados en la lista YA FILTRADA
        long totalClases = view.size();

        // Paginación simple en memoria: 25 clases por página
        int pageSize = 25;
        int totalPages = (int) Math.ceil(totalClases / (double) pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (page < 0) {
            page = 0;
        }
        if (page >= totalPages) {
            page = totalPages - 1;
        }
        int fromIndex = page * pageSize;
        int toIndex = (int) Math.min(fromIndex + pageSize, totalClases);
        List<ClaseViewDTO> pageContent = view.subList(fromIndex, toIndex);

        int pageStart = (totalClases == 0) ? 0 : fromIndex + 1;
        int pageEnd = (totalClases == 0) ? 0 : toIndex;
        long clasesLlenas = view.stream()
                .filter(v -> (v.getOcupadosPremium() + v.getOcupadosElite()) >= (v.getCuposPremium() + v.getCuposElite()))
                .count();
        int totalCupos = view.stream().mapToInt(v -> v.getCuposPremium() + v.getCuposElite()).sum();

        model.addAttribute("clases", pageContent);
        model.addAttribute("totalClases", totalClases);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageStart", pageStart);
        model.addAttribute("pageEnd", pageEnd);
        model.addAttribute("clasesLlenas", clasesLlenas);
        model.addAttribute("totalCupos", totalCupos);
        model.addAttribute("buscarActual", buscar == null ? "" : buscar);
        // Pasar el período actual a la vista
        model.addAttribute("periodo", periodo);
        
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
        // Marcar menú activo para el sidebar
        model.addAttribute("activeMenu", "clases");
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
    public Map<String, Object> actualizar(@PathVariable long id, @RequestBody Map<String, Object> body) {
        Clase c = claseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
        applyFromBody(c, body);
        claseRepository.save(java.util.Objects.requireNonNull(c));
        return toJson(c);
    }

    /**
     * DELETE /clases/{id} - API REST: Elimina clase
     * Elimina primero calificaciones, luego reservas y promociones asociadas
     */
    @DeleteMapping("/clases/{id}")
    @ResponseBody
    @Transactional
    public void eliminar(@PathVariable long id) {
        // Eliminar calificaciones vinculadas a esta clase
        claseCalificacionRepository.deleteByClaseId(id);
        // Eliminar reservas asociadas a la clase
        reservaClaseRepository.deleteByClase_Id(id);
        // Eliminar promociones configuradas para esta clase
        promocionRepository.deleteByClase_Id(id);
        // Finalmente eliminar la clase
        claseRepository.deleteById(id);
    }

    // Mapea datos del JSON al objeto Clase
    private void applyFromBody(Clase c, Map<String, Object> body) {
        c.setDescripcion(null);
        c.setDuracionMinutos(parseInt(body.get("duracion"), 60));

        int cuposPremium = parseInt(body.get("cuposPremium"), 0);
        int cuposElite = parseInt(body.get("cuposElite"), 0);
        int cuposBasico = parseInt(body.get("cuposBasico"), 0);

        // Guardar cupos por tipo de membresía
        c.setCuposBasico(cuposBasico);
        c.setCuposPremium(cuposPremium);
        c.setCuposElite(cuposElite);

        // Capacidad total = cupos para todos los tipos de usuario
        c.setCapacidad(cuposPremium + cuposElite + cuposBasico);

        // Tipo de clase (obligatorio en BD)
        Long tipoId = null;
        try {
            Object t = body.get("tipoClaseId");
            if (t != null) tipoId = Long.parseLong(String.valueOf(t));
        } catch (Exception ignored) {}

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
            } catch (Exception e) {
            }
        }

        // Buscar entrenador por nombre y apellido en tabla usuarios
        Usuario entren = null;

        // Intentar primero por ID de instructor (nuevo flujo)
        try {
            Object instructorIdObj = body.get("instructorId");
            if (instructorIdObj != null) {
                Long instructorId = Long.parseLong(String.valueOf(instructorIdObj));
                entren = usuarioRepository.findById(instructorId).orElse(null);
            }
        } catch (Exception e) {
        }

        // Fallback: búsqueda por nombre/apellido como antes
        if (entren == null) {
            String instructor = String.valueOf(body.getOrDefault("instructor", ""));
            if (!instructor.isBlank()) {
                String[] parts = instructor.trim().split(" ", 2);
                String nombre = parts[0];
                String apellido = parts.length > 1 ? parts[1] : "";
                entren = usuarioRepository.findActiveEntrenadores().stream()
                        .filter(u -> u.getNombre().equalsIgnoreCase(nombre) &&
                                     (apellido.isBlank() || u.getApellido().equalsIgnoreCase(apellido)))
                        .findFirst().orElse(null);
            }
        }

        c.setEntrenador(entren);

        // Estado por defecto
        if (c.getEstado() == null || c.getEstado().isBlank()) {
            c.setEstado("Programada");
        }

        // Clases de pago y disponibilidad
        Object esPagoObj = body.get("esPago");
        boolean esPago = esPagoObj != null && Boolean.parseBoolean(String.valueOf(esPagoObj));
        c.setEsPago(esPago);

        Object paraTodosObj = body.get("paraTodos");
        boolean paraTodos = paraTodosObj != null && Boolean.parseBoolean(String.valueOf(paraTodosObj));
        c.setParaTodos(paraTodos);

        // Precio solo aplica si la clase es de pago
        if (esPago) {
            Object precioObj = body.get("precio");
            if (precioObj != null) {
                try {
                    java.math.BigDecimal precio = new java.math.BigDecimal(String.valueOf(precioObj));
                    c.setPrecio(precio);
                } catch (NumberFormatException e) {
                    c.setPrecio(null);
                }
            } else {
                c.setPrecio(null);
            }
        } else {
            c.setPrecio(null);
        }
    }

    // Convierte Object a int con valor por defecto
    private int parseInt(Object v, int def) {
        if (v == null) {
            return def;
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            return def;
        }
        return Integer.parseInt(s);
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
        m.put("esPago", c.getEsPago());
        m.put("paraTodos", c.getParaTodos());
        m.put("precio", c.getPrecio());
        return m;
    }
}
