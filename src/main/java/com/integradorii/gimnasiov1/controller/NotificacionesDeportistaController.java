package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.model.NotificacionMasiva;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Promocion;
import com.integradorii.gimnasiov1.model.ReservaClase;
import com.integradorii.gimnasiov1.model.Suscripcion;
import com.integradorii.gimnasiov1.repository.ClaseRepository;
import com.integradorii.gimnasiov1.repository.NotificacionMasivaRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.PromocionRepository;
import com.integradorii.gimnasiov1.repository.ReservaClaseRepository;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * API de notificaciones para el panel del deportista.
 *
 * Genera notificaciones "on the fly" a partir del estado actual de la base de datos,
 * sin almacenar una tabla de notificaciones persistentes.
 *
 * Tipos de notificación generados:
 * - Estado de la suscripción: vencida, próxima a vencer, próximo pago.
 * - Falta de suscripción activa.
 * - Nuevas promociones relevantes creadas recientemente.
 * - Nuevas clases disponibles en los próximos días.
 * - Próximas clases reservadas por el deportista.
 */
@RestController
@RequestMapping("/api/deportista")
public class NotificacionesDeportistaController {

    private final PersonaRepository personaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final ReservaClaseRepository reservaClaseRepository;
    private final PromocionRepository promocionRepository;
    private final ClaseRepository claseRepository;
    private final NotificacionMasivaRepository notificacionMasivaRepository;

    public NotificacionesDeportistaController(PersonaRepository personaRepository,
                                              SuscripcionRepository suscripcionRepository,
                                              ReservaClaseRepository reservaClaseRepository,
                                              PromocionRepository promocionRepository,
                                              ClaseRepository claseRepository,
                                              NotificacionMasivaRepository notificacionMasivaRepository) {
        this.personaRepository = personaRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.reservaClaseRepository = reservaClaseRepository;
        this.promocionRepository = promocionRepository;
        this.claseRepository = claseRepository;
        this.notificacionMasivaRepository = notificacionMasivaRepository;
    }

    /**
     * GET /api/deportista/notificaciones
     *
     * Construye un listado de notificaciones para el deportista autenticado.
     *
     * Estructura de respuesta:
     * - `total`: número total de notificaciones calculadas.
     * - `items`: lista de mapas con los campos:
     *   - `tipo`: código de tipo (SUSCRIPCION_VENCIDA, PAGO_PROXIMO, etc.).
     *   - `id`: identificador estable del evento.
     *   - `titulo` y `mensaje`: texto para mostrar en la UI.
     *   - `fechaReferencia`: fecha/hora relevante para ordenar.
     *   - `url`: enlace sugerido para llevar al usuario al flujo adecuado.
     */
    @GetMapping("/notificaciones")
    public ResponseEntity<?> obtenerNotificaciones(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }

        Optional<Persona> personaOpt = personaRepository.findByEmail(principal.getName());
        if (personaOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Deportista no encontrado"));
        }

        Persona persona = personaOpt.get();
        LocalDate hoy = LocalDate.now();
        List<Map<String, Object>> items = new ArrayList<>();

        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(new Locale("es"));
        DateTimeFormatter fechaHoraFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withLocale(new Locale("es"));

        Optional<Suscripcion> suscripcionOpt = suscripcionRepository.findActiveByDeportistaId(persona.getId());

        LocalDateTime haceTreintaDias = LocalDateTime.now().minusDays(30);
        List<NotificacionMasiva> anunciosRecientes = notificacionMasivaRepository
                .findByFechaEnvioAfterOrderByFechaEnvioDesc(haceTreintaDias);

        Integer planActualId = null;
        if (suscripcionOpt.isPresent() && suscripcionOpt.get().getPlan() != null) {
            planActualId = suscripcionOpt.get().getPlan().getId();
        }

        int maxAnuncios = 3;
        int contadorAnuncios = 0;
        for (NotificacionMasiva anuncio : anunciosRecientes) {
            if (contadorAnuncios >= maxAnuncios) {
                break;
            }

            String filtroPlan = anuncio.getFiltroPlan();
            boolean aplica = false;

            if (filtroPlan == null || filtroPlan.isBlank() || "TODOS".equalsIgnoreCase(filtroPlan)) {
                aplica = true;
            } else if (planActualId != null) {
                try {
                    Integer planFiltro = Integer.valueOf(filtroPlan.trim());
                    aplica = planFiltro.equals(planActualId);
                } catch (NumberFormatException ignored) {
                }
            }

            if (!aplica) {
                continue;
            }

            Map<String, Object> n = new LinkedHashMap<>();
            n.put("tipo", "ANUNCIO_GENERAL");
            n.put("id", "ANUNCIO_GENERAL_" + anuncio.getId());
            n.put("titulo", anuncio.getAsunto());
            n.put("mensaje", anuncio.getMensaje());
            n.put("fechaReferencia", anuncio.getFechaEnvio());
            n.put("url", "/perfil");
            items.add(n);
            contadorAnuncios++;
        }

        // Bloque 1: notificaciones relacionadas a la suscripción actual
        if (suscripcionOpt.isPresent()) {
            Suscripcion s = suscripcionOpt.get();
            if (s.getFechaFin() != null) {
                long diasRestantes = ChronoUnit.DAYS.between(hoy, s.getFechaFin());
                if (diasRestantes < 0) {
                    Map<String, Object> n = new LinkedHashMap<>();
                    n.put("tipo", "SUSCRIPCION_VENCIDA");
                    n.put("id", "SUSCRIPCION_VENCIDA_" + s.getId());
                    n.put("titulo", "Tu suscripción ha vencido");
                    n.put("mensaje", "Tu suscripción venció el " + s.getFechaFin().format(fechaFormatter) + ".");
                    n.put("fechaReferencia", s.getFechaFin());
                    n.put("url", "/planes?usuario=" + persona.getEmail());
                    items.add(n);
                } else if (diasRestantes <= 7) {
                    Map<String, Object> n = new LinkedHashMap<>();
                    n.put("tipo", "SUSCRIPCION_PROXIMA_VENCER");
                    n.put("id", "SUSCRIPCION_PROXIMA_VENCER_" + s.getId());
                    n.put("titulo", "Tu suscripción vence pronto");
                    n.put("mensaje", "Tu suscripción vence el " + s.getFechaFin().format(fechaFormatter) + " (en " + diasRestantes + " días).");
                    n.put("fechaReferencia", s.getFechaFin());
                    n.put("url", "/planes?usuario=" + persona.getEmail());
                    items.add(n);
                }
            }
            if (s.getProximoPago() != null) {
                long diasProximoPago = ChronoUnit.DAYS.between(hoy, s.getProximoPago());
                if (diasProximoPago >= 0 && diasProximoPago <= 7) {
                    Map<String, Object> n = new LinkedHashMap<>();
                    n.put("tipo", "PAGO_PROXIMO");
                    n.put("id", "PAGO_PROXIMO_" + s.getId());
                    n.put("titulo", "Próximo pago de suscripción");
                    n.put("mensaje", "Tu próximo pago es el " + s.getProximoPago().format(fechaFormatter) + " (en " + diasProximoPago + " días).");
                    n.put("fechaReferencia", s.getProximoPago());
                    n.put("url", "/planes?usuario=" + persona.getEmail());
                    items.add(n);
                }
            }
        } else {
            // Si no hay suscripción activa, se genera una invitación a contratar un plan
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("tipo", "SIN_SUSCRIPCION");
            n.put("id", "SIN_SUSCRIPCION_" + persona.getId());
            n.put("titulo", "No tienes una suscripción activa");
            n.put("mensaje", "Contrata un plan de membresía para acceder a todas las funcionalidades.");
            n.put("fechaReferencia", hoy);
            n.put("url", "/planes?usuario=" + persona.getEmail());
            items.add(n);
        }

        // Bloque 2: nuevas promociones creadas recientemente (últimos 7 días) y vigentes
        LocalDateTime haceSieteDias = LocalDateTime.now().minusDays(7);
        List<Promocion> promocionesActivas = promocionRepository.findByEstado(Promocion.Estado.ACTIVE);
        int maxPromos = 3;
        int contadorPromos = 0;
        for (Promocion p : promocionesActivas) {
            if (p.getCreadoEn() == null || p.getCreadoEn().isBefore(haceSieteDias)) {
                continue;
            }
            if (p.getFechaInicio() != null && p.getFechaInicio().isAfter(hoy)) {
                continue;
            }
            if (p.getFechaFin() != null && p.getFechaFin().isBefore(hoy)) {
                continue;
            }
            if (contadorPromos >= maxPromos) {
                break;
            }
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("tipo", "PROMOCION_NUEVA");
            n.put("id", "PROMOCION_NUEVA_" + p.getId());
            n.put("titulo", "Nueva promoción disponible");
            String mensajePromo = "Aprovecha la promoción \"" + p.getNombre() + "\"";
            if (p.getFechaFin() != null) {
                mensajePromo += " disponible hasta el " + p.getFechaFin().format(fechaFormatter) + ".";
            } else {
                mensajePromo += ".";
            }
            n.put("mensaje", mensajePromo);
            n.put("fechaReferencia", p.getCreadoEn());
            // Para promociones nuevas, llevar siempre a la pantalla de promociones del deportista
            n.put("url", "/deportista/promociones");
            items.add(n);
            contadorPromos++;
        }

        // Bloque 3: clases nuevas disponibles en los próximos 7 días que el deportista aún no reservó
        OffsetDateTime ahora = OffsetDateTime.now(ZoneId.systemDefault());

        OffsetDateTime limiteClases = ahora.plusDays(7);
        List<Clase> clasesProximasDisponibles = claseRepository.findByFechaAfterOrderByFechaAsc(ahora);
        int maxClasesNuevas = 3;
        int contadorClasesNuevas = 0;
        for (Clase c : clasesProximasDisponibles) {
            if (c.getFecha() == null || c.getFecha().isAfter(limiteClases)) {
                continue;
            }
            if (c.getEstado() != null && !"Programada".equalsIgnoreCase(c.getEstado())) {
                continue;
            }
            if (reservaClaseRepository.existsByClase_IdAndDeportista_IdAndEstadoNot(c.getId(), persona.getId(), "Cancelado")) {
                // Ya tiene una reserva activa para esta clase, no la mostramos como "nueva disponible"
                continue;
            }
            if (contadorClasesNuevas >= maxClasesNuevas) {
                break;
            }
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("tipo", "CLASE_NUEVA");
            n.put("id", "CLASE_NUEVA_" + c.getId());
            n.put("titulo", "Nueva clase disponible");
            String nombreClaseDisponible = c.getNombre();
            String fechaClaseTexto = c.getFecha().format(fechaHoraFormatter);
            n.put("mensaje", "Se ha agregado la clase \"" + nombreClaseDisponible + "\" para el " + fechaClaseTexto + ".");
            n.put("fechaReferencia", c.getFecha());
            n.put("url", "/reservas?fecha=" + c.getFecha().toLocalDate());
            items.add(n);
            contadorClasesNuevas++;
        }

        // Bloque 4: próximas clases ya reservadas por el deportista
        List<ReservaClase> proximas = reservaClaseRepository.findProximasByDeportista(persona.getId(), ahora);
        int maxClases = 3;
        int contador = 0;
        for (ReservaClase r : proximas) {
            if (r.getClase() == null || r.getClase().getFecha() == null) {
                continue;
            }
            if (contador >= maxClases) {
                break;
            }
            OffsetDateTime fechaClase = r.getClase().getFecha();
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("tipo", "CLASE_PROXIMA");
            n.put("id", "CLASE_PROXIMA_" + r.getId());
            n.put("titulo", "Próxima clase reservada");
            String nombreClase = r.getClase().getNombre();
            String textoFecha = fechaClase.format(fechaHoraFormatter);
            n.put("mensaje", "Tienes la clase \"" + nombreClase + "\" el " + textoFecha + ".");
            n.put("fechaReferencia", fechaClase);
            n.put("url", "/reservas?fecha=" + fechaClase.toLocalDate());
            items.add(n);
            contador++;
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("total", items.size());
        respuesta.put("items", items);
        return ResponseEntity.ok(respuesta);
    }
}
