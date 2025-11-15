package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Asistencia;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.AsistenciaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/monitoreo")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
public class MonitoreoCapacidadController {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @GetMapping
    public String mostrarMonitoreo() {
        return "monitoreo-capacidad";
    }

    @GetMapping("/api/capacidad")
    @ResponseBody
    public Map<String, Object> obtenerDatosCapacidad() {
        // Obtener la cantidad de personas actualmente en el gimnasio (check-in sin check-out)
        int personasEnGimnasio = asistenciaRepository.countByFechaHoraSalidaIsNull();

        // Obtener los últimos 10 registros de asistencia
        List<Asistencia> ultimasAsistencias = asistenciaRepository.findTop10ByOrderByFechaHoraIngresoDesc();

        // Convertir las asistencias a un formato adecuado para la vista
        List<Map<String, Object>> registrosRecientes = ultimasAsistencias.stream()
            .map(a -> {
                try {
                    return convertirAsistenciaAMapa(a);
                } catch (RuntimeException ex) {
                    System.err.println("Error al convertir asistencia ID " + a.getId() + ": " + ex.getMessage());
                    return null; // se filtrará más adelante si es necesario
                }
            })
            .filter(m -> m != null)
            .collect(Collectors.toList());

        // Obtener datos para el gráfico de uso por hora
        Map<String, Object> datosGrafico = obtenerDatosGraficoUso();

        // Crear la respuesta
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("personasActuales", personasEnGimnasio);
        respuesta.put("registrosRecientes", registrosRecientes);
        respuesta.put("datosGrafico", datosGrafico);
        respuesta.put("fechaActual", LocalDate.now().toString());
        respuesta.put("horaActual", LocalTime.now().toString().substring(0, 5));

        return respuesta;
    }

    @GetMapping("/api/estadisticas")
    @ResponseBody
    public Map<String, Object> obtenerEstadisticas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        if (fecha == null) {
            fecha = LocalDate.now();
        }

        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();

        // Obtener todas las asistencias del día
        List<Asistencia> asistenciasHoy = asistenciaRepository
            .findByFechaHoraIngresoBetweenOrderByFechaHoraIngresoDesc(inicioDia, finDia);

        // Procesar datos para el gráfico por hora
        Map<Integer, Long> conteoPorHora = asistenciasHoy.stream()
            .filter(a -> a.getFechaHoraIngreso() != null)
            .collect(Collectors.groupingBy(
                a -> a.getFechaHoraIngreso().getHour(),
                Collectors.counting()
            ));

        // Asegurarse de que todas las horas del día estén representadas
        Map<Integer, Long> datosGrafico = new HashMap<>();
        for (int hora = 8; hora < 22; hora++) { // Horario del gimnasio: 8am a 10pm
            datosGrafico.put(hora, conteoPorHora.getOrDefault(hora, 0L));
        }

        // Obtener pico de asistencia
        Map.Entry<Integer, Long> picoAsistencia = datosGrafico.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(Map.entry(0, 0L));

        // Calcular total de asistencias del día
        long totalAsistencias = asistenciasHoy.size();

        // Obtener asistencias activas (sin marcar salida)
        int asistenciasActivas = asistenciaRepository.countByFechaHoraSalidaIsNull();

        // Preparar respuesta
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha", fecha.toString());
        respuesta.put("totalAsistencias", totalAsistencias);
        respuesta.put("asistenciasActivas", asistenciasActivas);
        respuesta.put("picoAsistenciaHora", picoAsistencia.getKey());
        respuesta.put("picoAsistenciaCantidad", picoAsistencia.getValue());
        respuesta.put("datosGrafico", datosGrafico);

        return respuesta;
    }

    private Map<String, Object> convertirAsistenciaAMapa(Asistencia asistencia) {
        Map<String, Object> registro = new HashMap<>();
        registro.put("id", asistencia.getId());

        LocalDateTime fechaIngreso = asistencia.getFechaHoraIngreso();
        String horaTexto = "--:--";
        if (fechaIngreso != null) {
            String completa = fechaIngreso.toLocalTime().toString();
            horaTexto = completa.length() >= 5 ? completa.substring(0, 5) : completa;
        }
        registro.put("hora", horaTexto);
        registro.put("tipo", asistencia.getFechaHoraSalida() == null ? "Dentro" : "Salida");

        Persona persona = null;
        try {
            persona = asistencia.getPersona();
        } catch (EntityNotFoundException ex) {
            System.err.println("Asistencia ID " + asistencia.getId() + " referencia una persona inexistente: " + ex.getMessage());
        }

        if (persona != null) {
            // Construir el nombre completo a partir de nombre y apellido
            String nombreCompleto = String.format("%s %s",
                persona.getNombre() != null ? persona.getNombre() : "",
                persona.getApellido() != null ? persona.getApellido() : ""
            ).trim();

            registro.put("nombre", nombreCompleto.isEmpty() ? "Usuario sin nombre" : nombreCompleto);
            registro.put("dni", persona.getDni() != null ? persona.getDni() : "N/A");

            // Usar un avatar generado
            String fotoUrl = String.format("https://ui-avatars.com/api/?name=%s&background=random",
                nombreCompleto.isEmpty() ? "Usuario" : nombreCompleto.replace(" ", "+"));
            registro.put("foto", fotoUrl);
        } else {
            registro.put("nombre", "Usuario no disponible");
            registro.put("dni", "N/A");
            registro.put("foto", "https://ui-avatars.com/api/?name=Usuario&background=random");
        }

        return registro;
    }

    private Map<String, Object> obtenerDatosGraficoUso() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioDia = ahora.toLocalDate().atStartOfDay();

        // Obtener todas las asistencias de hoy
        List<Asistencia> asistenciasHoy = asistenciaRepository
            .findByFechaHoraIngresoBetweenOrderByFechaHoraIngresoDesc(inicioDia, ahora);

        // Agrupar por hora y contar asistencias
        Map<Integer, Long> conteoPorHora = asistenciasHoy.stream()
            .filter(a -> a.getFechaHoraIngreso() != null)
            .collect(Collectors.groupingBy(
                a -> a.getFechaHoraIngreso().getHour(),
                Collectors.counting()
            ));

        // Crear un mapa con todas las horas del día (8am a 10pm)
        Map<String, Object> datosGrafico = new HashMap<>();
        for (int hora = 8; hora < 22; hora++) { // Horario del gimnasio: 8am a 10pm
            datosGrafico.put(String.format("%02d:00", hora), conteoPorHora.getOrDefault(hora, 0L));
        }

        return datosGrafico;
    }
}
