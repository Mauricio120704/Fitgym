package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.model.Promocion;
import com.integradorii.gimnasiov1.model.PromocionMembresia;
import com.integradorii.gimnasiov1.repository.ClaseRepository;
import com.integradorii.gimnasiov1.repository.PromocionRepository;
import com.integradorii.gimnasiov1.model.PromocionHistorial;
import com.integradorii.gimnasiov1.repository.PromocionHistorialRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador de Promociones - Gestión de descuentos y ofertas
 * Ruta: /promociones | Acceso: ADMIN, RECEPCIONISTA, ENTRENADOR
 * Tabla: promociones, promocion_membresia
 */
@Controller
public class PromocionController {

    private final PromocionRepository promocionRepository;
    private final PromocionHistorialRepository historialRepository;
    private final ClaseRepository claseRepository;

    public PromocionController(PromocionRepository promocionRepository,
                               PromocionHistorialRepository historialRepository,
                               ClaseRepository claseRepository) {
        this.promocionRepository = promocionRepository;
        this.historialRepository = historialRepository;
        this.claseRepository = claseRepository;
    }

    /**
     * GET /promociones - Lista todas las promociones
     * Permite búsqueda por nombre/descripción
     * Separa activas, expiradas e inactivas
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA','ENTRENADOR')")
    @GetMapping("/promociones")
    public String listarPromociones(@RequestParam(name = "q", required = false) String q,
                                    @RequestParam(name = "estado", required = false) String estado,
                                    Model model) {
        List<Promocion> activas;
        List<Promocion> expiradas;
        List<Promocion> inactivas;
        LocalDate hoy = LocalDate.now();
        if (q != null && q.isBlank()) {
            q = null; // Tratar búsqueda vacía como no aplicada para no perder el filtro de estado
        }
        // Base de datos: o filtradas por texto, o todas
        List<Promocion> base = (q != null)
                ? promocionRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(q, q)
                : promocionRepository.findAll();

        if (q != null) {
            model.addAttribute("q", q);
        }

        // Expiradas por FECHA (siempre): fechaFin < hoy
        expiradas = base.stream()
                .filter(p -> p.getFechaFin() != null && p.getFechaFin().isBefore(hoy))
                .collect(Collectors.toList());

        // Restantes (no expiradas) separadas por ESTADO
        List<Promocion> noExpiradas = base.stream()
                .filter(p -> p.getFechaFin() == null || !p.getFechaFin().isBefore(hoy))
                .collect(Collectors.toList());

        activas = noExpiradas.stream()
                .filter(p -> p.getEstado() == Promocion.Estado.ACTIVE)
                .collect(Collectors.toList());

        inactivas = noExpiradas.stream()
                .filter(p -> p.getEstado() == Promocion.Estado.INACTIVE)
                .collect(Collectors.toList());

        // Filtro por estado (ACTIVE, INACTIVE, EXPIRED)
        if (estado != null && !estado.isBlank()) {
            try {
                Promocion.Estado estadoEnum = Promocion.Estado.valueOf(estado);
                if (estadoEnum == Promocion.Estado.INACTIVE) {
                    activas = Collections.emptyList();
                    expiradas = Collections.emptyList();
                    inactivas = inactivas.stream().filter(p -> p.getEstado() == Promocion.Estado.INACTIVE).collect(Collectors.toList());
                } else if (estadoEnum == Promocion.Estado.ACTIVE) {
                    expiradas = Collections.emptyList();
                    inactivas = Collections.emptyList();
                    activas = activas.stream().filter(p -> p.getEstado() == Promocion.Estado.ACTIVE).collect(Collectors.toList());
                } else if (estadoEnum == Promocion.Estado.EXPIRED) {
                    activas = Collections.emptyList();
                    inactivas = Collections.emptyList();
                    // Ya están determinadas por fecha; no aplicar filtro por estado aquí
                }
                model.addAttribute("estadoFiltro", estadoEnum.name());
            } catch (IllegalArgumentException e) {
                model.addAttribute("estadoFiltro", "");
            }
        } else {
            model.addAttribute("estadoFiltro", "");
        }

        model.addAttribute("activas", activas);
        model.addAttribute("expiradas", expiradas);
        model.addAttribute("inactivas", inactivas);

        // Agregar la lista de estados al modelo
        model.addAttribute("todosEstados", Promocion.Estado.values());
        // Fecha actual para la vista (evita T(java.time.LocalDate).now())
        model.addAttribute("hoy", hoy);
        // Clases disponibles para promociones de tipo CLASE
        model.addAttribute("clasesDisponibles", claseRepository.findAllByOrderByFechaAsc());
        
        return "promociones";
    }

    /**
     * POST /promociones - Crea nueva promoción
     * Tipos: AMOUNT (monto fijo) o PERCENTAGE (porcentaje)
     * Estado auto: EXPIRED si fecha fin < hoy, sino ACTIVE
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA','ENTRENADOR')")
    @PostMapping("/promociones")
    public String crearPromocion(@RequestParam String nombre,
                                 @RequestParam String tipo,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam BigDecimal valor,
                                 @RequestParam Integer maxUsos,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                 @RequestParam(name = "membership", required = false) List<String> memberships,
                                 @RequestParam(name = "tipoPromocion", required = false) String tipoPromocion,
                                 @RequestParam(name = "tipoPlan", required = false) String tipoPlan,
                                 @RequestParam(name = "claseId", required = false) Long claseId) {

        Promocion p = new Promocion();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setTipo("AMOUNT".equalsIgnoreCase(tipo) ? Promocion.TipoDescuento.AMOUNT : Promocion.TipoDescuento.PERCENTAGE);
        p.setValor(valor);
        p.setMaxUsos(maxUsos);
        p.setFechaInicio(fechaInicio);
        p.setFechaFin(fechaFin);

        // Tipo de promoción: por defecto MEMBRESIA si no se especifica
        Promocion.TipoPromocion tipoPromoEnum = Promocion.TipoPromocion.MEMBRESIA;
        if (tipoPromocion != null && !tipoPromocion.isBlank()) {
            try {
                tipoPromoEnum = Promocion.TipoPromocion.valueOf(tipoPromocion);
            } catch (IllegalArgumentException ignored) {
                tipoPromoEnum = Promocion.TipoPromocion.MEMBRESIA;
            }
        }
        p.setTipoPromocion(tipoPromoEnum);

        // Calcular estado según fecha de fin
        if (fechaFin != null && fechaFin.isBefore(LocalDate.now())) {
            p.setEstado(Promocion.Estado.EXPIRED);
        } else {
            p.setEstado(Promocion.Estado.ACTIVE);
        }

        if (tipoPromoEnum == Promocion.TipoPromocion.MEMBRESIA) {
            p.setTipoPlan(tipoPlan);
            if (memberships != null) {
                for (String m : memberships) {
                    PromocionMembresia pm = new PromocionMembresia();
                    pm.setPromocion(p);
                    pm.setMembresia(m);
                    p.getMembresias().add(pm);
                }
            }
            p.setClase(null);
        } else {
            p.setTipoPlan(null);
            p.getMembresias().clear();
            if (claseId != null) {
                claseRepository.findById(claseId).ifPresent(p::setClase);
            } else {
                p.setClase(null);
            }
        }

        promocionRepository.save(p);
        guardarHistorial(p, PromocionHistorial.Accion.CREAR, null, p.getEstado(), "Creación de promoción");
        return "redirect:/promociones";
    }

    /**
     * POST /promociones/{id}/toggle - Activa/Desactiva promoción
     * Solo alterna entre ACTIVE e INACTIVE (no afecta EXPIRED)
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA','ENTRENADOR')")
    @PostMapping("/promociones/{id}/toggle")
    public String toggle(@PathVariable long id) {
        Optional<Promocion> opt = promocionRepository.findById(id);
        if (opt.isPresent()) {
            Promocion p = opt.get();
            Promocion.Estado anterior = p.getEstado();
            if (anterior == Promocion.Estado.INACTIVE) {
                p.setEstado(Promocion.Estado.ACTIVE);
                promocionRepository.save(p);
                guardarHistorial(p, PromocionHistorial.Accion.REACTIVAR, anterior, p.getEstado(), "Reactivación de promoción");
            } else if (anterior == Promocion.Estado.ACTIVE) {
                p.setEstado(Promocion.Estado.INACTIVE);
                promocionRepository.save(p);
                guardarHistorial(p, PromocionHistorial.Accion.TOGGLE, anterior, p.getEstado(), "Cambio de estado");
            }
        }
        return "redirect:/promociones";
    }

    /**
     * POST /promociones/{id}/reactivar - Reactiva una promoción vencida con nuevas fechas
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA','ENTRENADOR')")
    @PostMapping("/promociones/{id}/reactivar")
    public String reactivar(@PathVariable long id,
                            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                            @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
                            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        Promocion p = promocionRepository.findById(id).orElseThrow();
        Promocion.Estado anterior = p.getEstado();
        if (inicio == null) inicio = fechaInicio;
        if (fin == null) fin = fechaFin;
        if (fin != null && inicio != null && fin.isBefore(inicio)) {
            // En caso de rango inválido, solo redirige sin cambios; se podría mejorar con feedback
            return "redirect:/promociones";
        }
        p.setFechaInicio(inicio);
        p.setFechaFin(fin);
        p.setEstado(Promocion.Estado.ACTIVE);
        promocionRepository.save(p);
        guardarHistorial(p, PromocionHistorial.Accion.REACTIVAR, anterior, p.getEstado(),
                "Reactivación con nuevo período: " + inicio + " - " + fin);
        return "redirect:/promociones#promo-" + id;
    }

    /**
     * POST /promociones/{id}/delete - Elimina promoción por ID
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA','ENTRENADOR')")
    @PostMapping("/promociones/{id}/delete")
    public String delete(@PathVariable long id) {
        if (promocionRepository.existsById(id)) {
            promocionRepository.deleteById(id);
        }
        return "redirect:/promociones";
    }

    /**
     * POST /promociones/{id}/editar - Actualiza promoción existente
     * Reemplaza completamente las membresías asociadas
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA','ENTRENADOR')")
    @PostMapping("/promociones/{id}/editar")
    public String editar(@PathVariable long id,
                         @RequestParam String nombre,
                         @RequestParam String tipo,
                         @RequestParam(required = false) String descripcion,
                         @RequestParam BigDecimal valor,
                         @RequestParam Integer maxUsos,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                         @RequestParam(name = "membership", required = false) List<String> memberships,
                         @RequestParam(name = "tipoPromocion", required = false) String tipoPromocion,
                         @RequestParam(name = "tipoPlan", required = false) String tipoPlan,
                         @RequestParam(name = "claseId", required = false) Long claseId) {
        Promocion p = promocionRepository.findById(id).orElseThrow();
        Promocion.Estado anterior = p.getEstado();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setTipo("AMOUNT".equalsIgnoreCase(tipo) ? Promocion.TipoDescuento.AMOUNT : Promocion.TipoDescuento.PERCENTAGE);
        p.setValor(valor);
        p.setMaxUsos(maxUsos);
        p.setFechaInicio(fechaInicio);
        p.setFechaFin(fechaFin);
        if (fechaFin != null && fechaFin.isBefore(LocalDate.now())) {
            p.setEstado(Promocion.Estado.EXPIRED);
        }

        // Tipo de promoción
        Promocion.TipoPromocion tipoPromoEnum = p.getTipoPromocion() != null ? p.getTipoPromocion() : Promocion.TipoPromocion.MEMBRESIA;
        if (tipoPromocion != null && !tipoPromocion.isBlank()) {
            try {
                tipoPromoEnum = Promocion.TipoPromocion.valueOf(tipoPromocion);
            } catch (IllegalArgumentException ignored) {
                // mantener valor anterior
            }
        }
        p.setTipoPromocion(tipoPromoEnum);

        if (tipoPromoEnum == Promocion.TipoPromocion.MEMBRESIA) {
            p.setTipoPlan(tipoPlan);
            p.setClase(null);
            p.getMembresias().clear();
            if (memberships != null) {
                for (String m : memberships) {
                    PromocionMembresia pm = new PromocionMembresia();
                    pm.setPromocion(p);
                    pm.setMembresia(m);
                    p.getMembresias().add(pm);
                }
            }
        } else {
            p.setTipoPlan(null);
            p.getMembresias().clear();
            if (claseId != null) {
                claseRepository.findById(claseId).ifPresent(p::setClase);
            } else {
                p.setClase(null);
            }
        }

        promocionRepository.save(p);
        guardarHistorial(p, PromocionHistorial.Accion.EDITAR, anterior, p.getEstado(), "Edición de promoción");
        return "redirect:/promociones";
    }

    /**
     * GET /promociones/historial - Historial global de promociones
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA','ENTRENADOR')")
    @GetMapping("/promociones/historial")
    public String historialGlobal(Model model) {
        model.addAttribute("historial", historialRepository.findAllWithPromocionOrderByRealizadoEnDesc());
        return "promociones_historial";
    }

    /**
     * GET /promociones/{id}/historial - Historial por promoción
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA','ENTRENADOR')")
    @GetMapping("/promociones/{id}/historial")
    public String historialPorPromocion(@PathVariable long id, Model model) {
        Promocion p = promocionRepository.findById(id).orElseThrow();
        model.addAttribute("promocion", p);
        model.addAttribute("historial", historialRepository.findByPromocionIdOrderByRealizadoEnDesc(id));
        return "promocion_historial";
    }

    @GetMapping("/promociones/clases-disponibles")
    @ResponseBody
    public List<Map<String, Object>> clasesDisponiblesPorRango(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            return Collections.emptyList();
        }

        ZoneId zone = ZoneId.systemDefault();

        return claseRepository.findAllByOrderByFechaAsc()
                .stream()
                .filter(c -> c.getFecha() != null)
                .filter(c -> {
                    java.time.ZonedDateTime zdt = c.getFecha().atZoneSameInstant(zone);
                    LocalDate d = zdt.toLocalDate();
                    return !d.isBefore(inicio) && !d.isAfter(fin);
                })
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    OffsetDateTime fechaClase = c.getFecha();
                    String label;
                    if (fechaClase != null) {
                        // Convertir a la misma zona horaria usada en la vista de clases
                        java.time.ZonedDateTime zdt = fechaClase.atZoneSameInstant(zone);
                        LocalDate d = zdt.toLocalDate();
                        LocalTime t = zdt.toLocalTime();
                        label = String.format("%02d/%02d/%04d %02d:%02d - %s",
                                d.getDayOfMonth(), d.getMonthValue(), d.getYear(),
                                t.getHour(), t.getMinute(), c.getNombre());
                    } else {
                        label = c.getNombre();
                    }
                    m.put("label", label);
                    return m;
                })
                .collect(Collectors.toList());
    }

    private void guardarHistorial(Promocion p, PromocionHistorial.Accion accion,
                                  Promocion.Estado anterior, Promocion.Estado nuevo,
                                  String detalle) {
        PromocionHistorial h = new PromocionHistorial();
        h.setPromocion(p);
        h.setAccion(accion);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(nuevo);
        h.setDetalle(detalle);
        historialRepository.save(h);
    }
}
