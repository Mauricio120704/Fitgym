package com.integradorii.gimnasiov1.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class ClaseService {

    @PersistenceContext
    private EntityManager em;

    public Map<String, Object> obtenerClasesDeportista(Long deportistaId) {
        String sql = """
            SELECT r.id                                   AS reserva_id,
                   c.id                                   AS clase_id,
                   c.nombre                               AS clase_nombre,
                   c.fecha                                AS clase_fecha,
                   c.duracion_minutos                     AS duracion_minutos,
                   instr.nombre || ' ' || COALESCE(instr.apellido,'') AS instructor_nombre,
                   cal.id                                 AS calificacion_id,
                   cal.rating_general,
                   cal.rating_instructor,
                   cal.rating_instalaciones,
                   cal.rating_musica,
                   cal.rating_dificultad,
                   cal.comentario
            FROM reservas_clase r
            JOIN clases c            ON c.id = r.clase_id
            LEFT JOIN personas instr ON instr.id = c.entrenador_id
            LEFT JOIN clase_calificaciones cal ON cal.reserva_id = r.id
            WHERE r.deportista_id = :dep
            ORDER BY c.fecha DESC
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("dep", deportistaId)
                .getResultList();

        return procesarClases(rows);
    }

    private Map<String, Object> procesarClases(List<Object[]> rows) {
        List<Map<String, Object>> proximas = new ArrayList<>();
        List<Map<String, Object>> pendientes = new ArrayList<>();
        List<Map<String, Object>> calificadas = new ArrayList<>();

        Instant now = Instant.now();
        System.out.println("=== DEBUG: Hora actual del servidor: " + now);

        for (Object[] row : rows) {
            Long reservaId = ((Number) row[0]).longValue();
            Long claseId = ((Number) row[1]).longValue();
            String nombre = (String) row[2];
            Object fechaObj = row[3];

            System.out.println("DEBUG: Clase=" + nombre + ", fechaObj tipo=" + (fechaObj != null ? fechaObj.getClass().getName() : "null") + ", valor=" + fechaObj);

            Instant fechaInstant = convertirFecha(fechaObj, now);
            Integer duracion = row[4] == null ? null : ((Number) row[4]).intValue();
            String instructor = row[5] == null ? "" : row[5].toString();
            Number calIdNum = (Number) row[6];
            Number rGeneral = (Number) row[7];
            Number rInstr = (Number) row[8];
            Number rInst = (Number) row[9];
            Number rMusic = (Number) row[10];
            Number rDif = (Number) row[11];
            String comentario = (String) row[12];

            Map<String, Object> item = crearItemClase(reservaId, claseId, nombre, fechaInstant, duracion, instructor);

            boolean tieneCal = calIdNum != null;
            boolean esFutura = fechaInstant.isAfter(now);
            System.out.println("  -> Comparación: fechaClase.isAfter(now)=" + esFutura + ", tieneCal=" + tieneCal);

            clasificarClase(item, esFutura, tieneCal, proximas, pendientes, calificadas,
                    rGeneral, rInstr, rInst, rMusic, rDif, comentario);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("proximas", proximas);
        resp.put("pendientes", pendientes);
        resp.put("calificadas", calificadas);
        return resp;
    }

    private Instant convertirFecha(Object fechaObj, Instant fallback) {
        if (fechaObj instanceof Instant inst) {
            System.out.println("  -> Ya es Instant: " + inst);
            return inst;
        } else if (fechaObj instanceof java.sql.Timestamp ts) {
            Instant instant = ts.toInstant();
            System.out.println("  -> Timestamp convertido a Instant: " + instant);
            return instant;
        } else if (fechaObj instanceof java.time.OffsetDateTime odt) {
            return odt.toInstant();
        } else if (fechaObj instanceof java.time.LocalDateTime ldt) {
            return ldt.atZone(ZoneId.of("America/Bogota")).toInstant();
        } else if (fechaObj instanceof java.util.Date d) {
            return d.toInstant();
        } else {
            System.out.println("  -> FALLBACK: tipo desconocido " + (fechaObj != null ? fechaObj.getClass().getName() : "null"));
            return fallback;
        }
    }

    private Map<String, Object> crearItemClase(Long reservaId, Long claseId, String nombre,
                                                Instant fechaInstant, Integer duracion, String instructor) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("reservaId", reservaId);
        item.put("claseId", claseId);
        item.put("titulo", nombre);
        item.put("fechaIso", fechaInstant.toString());
        item.put("fechaTexto", DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy")
                .withLocale(new Locale("es")).withZone(ZoneId.systemDefault()).format(fechaInstant));
        item.put("duracion", duracion == null ? "" : (duracion + " min"));
        item.put("instructor", instructor);
        return item;
    }

    private void clasificarClase(Map<String, Object> item, boolean esFutura, boolean tieneCal,
                                  List<Map<String, Object>> proximas,
                                  List<Map<String, Object>> pendientes,
                                  List<Map<String, Object>> calificadas,
                                  Number rGeneral, Number rInstr, Number rInst,
                                  Number rMusic, Number rDif, String comentario) {
        if (esFutura) {
            item.put("estado", "proxima");
            proximas.add(item);
            System.out.println("  -> Clasificada como: PRÓXIMA");
        } else if (!tieneCal) {
            item.put("estado", "pendiente");
            pendientes.add(item);
            System.out.println("  -> Clasificada como: PENDIENTE");
        } else {
            item.put("estado", "calificada");
            System.out.println("  -> Clasificada como: CALIFICADA");
            item.put("rating", rGeneral == null ? null : rGeneral.intValue());
            item.put("ratingInstructor", rInstr == null ? null : rInstr.intValue());
            item.put("ratingInstalaciones", rInst == null ? null : rInst.intValue());
            item.put("ratingMusica", rMusic == null ? null : rMusic.intValue());
            item.put("ratingDificultad", rDif == null ? null : rDif.intValue());
            item.put("review", comentario);
            calificadas.add(item);
        }
    }
}