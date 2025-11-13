package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Promocion;
import com.integradorii.gimnasiov1.model.PromocionHistorial;
import com.integradorii.gimnasiov1.model.PromocionMembresia;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.repository.PromocionRepository;
import com.integradorii.gimnasiov1.repository.PromocionHistorialRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PromocionController(PromocionRepository promocionRepository,
                               PromocionHistorialRepository historialRepository,
                               UsuarioRepository usuarioRepository) {
        this.promocionRepository = promocionRepository;
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private void logHistorial(Promocion promo,
                              PromocionHistorial.Accion accion,
                              Promocion.Estado estadoAnterior,
                              Promocion.Estado estadoNuevo,
                              String detalle) {
        String usuarioStr = "Sistema";
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String username = auth.getName();
                Usuario usuario = usuarioRepository.findByEmail(username).orElse(null);
                usuarioStr = (usuario != null) ? usuario.getEmail() : ("Usuario no encontrado (" + username + ")");
            }
        } catch (Exception e) {
            usuarioStr = "Error al obtener usuario: " + e.getMessage();
        }

        // Para la acción ELIMINAR, insertar con SQL nativo para evitar referencias transientes
        if (accion == PromocionHistorial.Accion.ELIMINAR) {
            entityManager.createNativeQuery(
                    "insert into promocion_historial (promocion_id, accion, estado_anterior, estado_nuevo, detalle, realizado_en, usuario) " +
                    "values (:pid, :accion, :ea, :en, :detalle, now(), :usuario)")
                .setParameter("pid", promo.getId())
                .setParameter("accion", "ELIMINAR")
                .setParameter("ea", estadoAnterior != null ? estadoAnterior.name() : null)
                .setParameter("en", estadoNuevo != null ? estadoNuevo.name() : null)
                .setParameter("detalle", detalle)
                .setParameter("usuario", usuarioStr)
                .executeUpdate();
            return;
        }

        // Para otras acciones, usar JPA normalmente
        PromocionHistorial h = new PromocionHistorial();
        h.setPromocion(entityManager.getReference(Promocion.class, promo.getId()));
        h.setAccion(accion);
        h.setEstadoAnterior(estadoAnterior);
        h.setEstadoNuevo(estadoNuevo);
        h.setDetalle(detalle);
        h.setUsuario(usuarioStr);
        historialRepository.saveAndFlush(h);
    }

    /**
     * GET /promociones - Lista todas las promociones
     * Permite búsqueda por nombre/descripción
     * Separa activas/expiradas de inactivas
     */
    @GetMapping("/promociones")
    public String listarPromociones(@RequestParam(name = "q", required = false) String q,
                                    @RequestParam(name = "estado", required = false) String estado,
                                    Model model) {
        List<Promocion> activasYExpiradas;
        List<Promocion> activas;
        List<Promocion> expiradas;
        List<Promocion> inactivas;
        if (q != null && q.isBlank()) {
            q = null; // Tratar búsqueda vacía como no aplicada para no perder el filtro de estado
        }
        if (q != null) {
            List<Promocion> filtradas = promocionRepository
                    .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(q, q);
            // Separar resultados buscando mantener juntas las promociones visibles en el tablero principal
            activasYExpiradas = filtradas.stream()
                    .filter(p -> p.getEstado() == Promocion.Estado.ACTIVE || p.getEstado() == Promocion.Estado.EXPIRED)
                    .collect(Collectors.toList());
            inactivas = filtradas.stream()
                    .filter(p -> p.getEstado() == Promocion.Estado.INACTIVE)
                    .collect(Collectors.toList());
            model.addAttribute("q", q);
        } else {
            activasYExpiradas = promocionRepository.findByEstadoIn(
                    Arrays.asList(Promocion.Estado.ACTIVE, Promocion.Estado.EXPIRED)
            );
            inactivas = promocionRepository.findByEstado(Promocion.Estado.INACTIVE);
        }

        // Separar activas y expiradas considerando estado y fechaFin
        java.time.LocalDate hoy = java.time.LocalDate.now();
        expiradas = activasYExpiradas.stream()
                .filter(p -> p.getEstado() == Promocion.Estado.EXPIRED
                        || (p.getFechaFin() != null && p.getFechaFin().isBefore(hoy)))
                .collect(Collectors.toList());
        activas = activasYExpiradas.stream()
                .filter(p -> p.getEstado() == Promocion.Estado.ACTIVE
                        && !(p.getFechaFin() != null && p.getFechaFin().isBefore(hoy)))
                .collect(Collectors.toList());

        // Filtro por estado (ACTIVE, INACTIVE, EXPIRED)
        if (estado != null && !estado.isBlank()) {
            try {
                Promocion.Estado estadoEnum = Promocion.Estado.valueOf(estado);
                if (estadoEnum == Promocion.Estado.INACTIVE) {
                    activas = Collections.emptyList();
                    expiradas = Collections.emptyList();
                    inactivas = inactivas.stream()
                            .filter(p -> p.getEstado() == Promocion.Estado.INACTIVE)
                            .collect(Collectors.toList());
                } else if (estadoEnum == Promocion.Estado.ACTIVE) {
                    expiradas = Collections.emptyList();
                    inactivas = Collections.emptyList();
                    activas = activas.stream()
                            .filter(p -> p.getEstado() == Promocion.Estado.ACTIVE)
                            .collect(Collectors.toList());
                } else if (estadoEnum == Promocion.Estado.EXPIRED) {
                    activas = Collections.emptyList();
                    inactivas = Collections.emptyList();
                    expiradas = expiradas.stream()
                            .filter(p -> p.getEstado() == Promocion.Estado.EXPIRED || (p.getFechaFin() != null && p.getFechaFin().isBefore(hoy)))
                            .collect(Collectors.toList());
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
        
        return "promociones";
    }

    /**
     * POST /promociones - Crea nueva promoción
     * Tipos: AMOUNT (monto fijo) o PERCENTAGE (porcentaje)
     * Estado auto: EXPIRED si fecha fin < hoy, sino ACTIVE
     */
    @PostMapping("/promociones")
    public String crearPromocion(@RequestParam String nombre,
                                 @RequestParam String tipo,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam BigDecimal valor,
                                 @RequestParam Integer maxUsos,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                 @RequestParam(name = "membership", required = false) List<String> memberships) {

        Promocion p = new Promocion();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setTipo("AMOUNT".equalsIgnoreCase(tipo) ? Promocion.TipoDescuento.AMOUNT : Promocion.TipoDescuento.PERCENTAGE);
        p.setValor(valor);
        p.setMaxUsos(maxUsos);
        p.setFechaInicio(fechaInicio);
        p.setFechaFin(fechaFin);

        // Calcular estado según fecha de fin
        if (fechaFin != null && fechaFin.isBefore(LocalDate.now())) {
            p.setEstado(Promocion.Estado.EXPIRED);
        } else {
            p.setEstado(Promocion.Estado.ACTIVE);
        }

        if (memberships != null) {
            // Asociamos la promoción con cada membresía seleccionada
            for (String m : memberships) {
                PromocionMembresia pm = new PromocionMembresia();
                pm.setPromocion(p);
                pm.setMembresia(m);
                p.getMembresias().add(pm);
            }
        }

        promocionRepository.save(p);
        logHistorial(p, PromocionHistorial.Accion.CREAR, null, p.getEstado(),
                "Creación de promoción");
        return "redirect:/promociones";
    }

    /**
     * POST /promociones/{id}/toggle - Activa/Desactiva promoción
     * Solo alterna entre ACTIVE e INACTIVE (no afecta EXPIRED)
     */
    @PostMapping("/promociones/{id}/toggle")
    public String toggle(@PathVariable long id) {
        Optional<Promocion> opt = promocionRepository.findById(id);
        if (opt.isPresent()) {
            Promocion p = opt.get();
            Promocion.Estado anterior = p.getEstado();
            if (p.getEstado() == Promocion.Estado.INACTIVE) {
                p.setEstado(Promocion.Estado.ACTIVE);
            } else if (p.getEstado() == Promocion.Estado.ACTIVE) {
                p.setEstado(Promocion.Estado.INACTIVE);
            }
            promocionRepository.save(p);
            logHistorial(p, PromocionHistorial.Accion.TOGGLE, anterior, p.getEstado(),
                    "Alternar estado");
        }
        return "redirect:/promociones";
    }

    /**
     * POST /promociones/{id}/delete - Elimina promoción por ID
     */
    @Transactional
    @PostMapping("/promociones/{id}/delete")
    public String delete(@PathVariable long id) {
        Optional<Promocion> opt = promocionRepository.findById(id);
        if (opt.isPresent()) {
            Promocion p = opt.get();
            logHistorial(p, PromocionHistorial.Accion.ELIMINAR, p.getEstado(), null,
                    "Eliminación de promoción");
            // Eliminar por ID para evitar gestionar estados de la entidad en el contexto
            promocionRepository.deleteById(id);
        } else {
            promocionRepository.deleteById(id);
        }
        return "redirect:/promociones";
    }

    /**
     * POST /promociones/{id}/editar - Actualiza promoción existente
     * Reemplaza completamente las membresías asociadas
     */
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
                         @RequestParam(name = "reactivar", required = false, defaultValue = "false") boolean reactivar) {
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
        } else {
            // Si no está expirada por fecha, activar a menos que estuviera INACTIVE explícitamente
            if (anterior == Promocion.Estado.INACTIVE) {
                p.setEstado(Promocion.Estado.INACTIVE);
            } else {
                p.setEstado(Promocion.Estado.ACTIVE);
            }
        }
        // Reemplazar membresías
        // La asociación se limpia para evitar duplicados y luego se reconstruye desde la solicitud
        p.getMembresias().clear();
        if (memberships != null) {
            for (String m : memberships) {
                PromocionMembresia pm = new PromocionMembresia();
                pm.setPromocion(p);
                pm.setMembresia(m);
                p.getMembresias().add(pm);
            }
        }
        promocionRepository.save(p);
        PromocionHistorial.Accion accion = (reactivar && anterior == Promocion.Estado.EXPIRED && p.getEstado() == Promocion.Estado.ACTIVE)
                ? PromocionHistorial.Accion.REACTIVAR
                : PromocionHistorial.Accion.EDITAR;
        logHistorial(p, accion, anterior, p.getEstado(),
                accion == PromocionHistorial.Accion.REACTIVAR ? "Reactivación de promoción" : "Edición de promoción");
        return "redirect:/promociones";
    }

    @GetMapping("/promociones/{id}/historial")
    public String verHistorial(@PathVariable long id, Model model) {
        Promocion promo = promocionRepository.findById(id).orElseThrow();
        model.addAttribute("promocion", promo);
        model.addAttribute("historial", historialRepository.findByPromocionIdOrderByRealizadoEnDesc(id));
        return "promocion_historial";
    }

    @GetMapping("/promociones/historial")
    public String verHistorialGlobal(Model model) {
        model.addAttribute("historial", historialRepository.findAllWithPromocionOrderByRealizadoEnDesc());
        return "promociones_historial";
    }
}
