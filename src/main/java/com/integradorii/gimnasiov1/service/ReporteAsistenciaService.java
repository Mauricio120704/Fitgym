package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.dto.ChartSerieDTO;
import com.integradorii.gimnasiov1.dto.ClaseAsistenciaItemDTO;
import com.integradorii.gimnasiov1.dto.ReporteAsistenciaResponse;
import com.integradorii.gimnasiov1.dto.ResumenAsistenciaDTO;
import com.integradorii.gimnasiov1.model.Clase;
import com.integradorii.gimnasiov1.repository.ClaseRepository;
import com.integradorii.gimnasiov1.repository.ReservaClaseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteAsistenciaService {

    private final ClaseRepository claseRepository;
    private final ReservaClaseRepository reservaClaseRepository;

    public ReporteAsistenciaService(ClaseRepository claseRepository,
                                    ReservaClaseRepository reservaClaseRepository) {
        this.claseRepository = claseRepository;
        this.reservaClaseRepository = reservaClaseRepository;
    }

    public ReporteAsistenciaResponse generar(OffsetDateTime inicio, OffsetDateTime fin, String tipo) {
        // Buscar clases en rango
        List<Clase> clases;
        if (tipo != null && !tipo.isBlank()) {
            clases = claseRepository.findByFechaBetweenAndTipoLike(inicio, fin, tipo.trim());
        } else {
            clases = claseRepository.findByFechaBetweenOrderByFechaAsc(inicio, fin);
        }

        List<ClaseAsistenciaItemDTO> items = new ArrayList<>();
        int totalCupos = 0;
        int totalAsistentes = 0;

        for (Clase c : clases) {
            Long id = c.getId();
            String nombre = Optional.ofNullable(c.getNombre()).orElse("");
            String instructor = c.getEntrenador() == null ? "-" :
                    (Optional.ofNullable(c.getEntrenador().getNombre()).orElse("") + " " +
                     Optional.ofNullable(c.getEntrenador().getApellido()).orElse("")).trim();
            LocalDate fecha = c.getFecha() == null ? null : c.getFecha().toLocalDate();
            LocalTime hora = c.getFecha() == null ? null : c.getFecha().toLocalTime();
            int cupo = Optional.ofNullable(c.getCapacidad()).orElse(0);
            int asistentes = (int) (id == null ? 0 : reservaClaseRepository.countOcupados(id));
            int ocupacion = cupo > 0 ? (int) Math.round((asistentes * 100.0) / cupo) : 0;
            String tipoDerivado = derivarTipo(c.getNombre(), c.getDescripcion());

            items.add(new ClaseAsistenciaItemDTO(id, nombre, tipoDerivado, instructor, fecha, hora, asistentes, cupo, ocupacion));
            totalCupos += cupo;
            totalAsistentes += asistentes;
        }

        // Resumen
        int totalClases = items.size();
        int asistenciaPromedio = totalCupos > 0 ? (int) Math.round((totalAsistentes * 100.0) / totalCupos) : 0;

        // Clase popular (por nombre agregando asistentes)
        Map<String, Integer> asistentesPorClase = new HashMap<>();
        for (ClaseAsistenciaItemDTO it : items) {
            asistentesPorClase.merge(it.getClase(), it.getAsistentes(), Integer::sum);
        }
        String clasePopular = "-";
        int maxAsist = 0;
        for (Map.Entry<String, Integer> e : asistentesPorClase.entrySet()) {
            if (e.getValue() > maxAsist) {
                maxAsist = e.getValue();
                clasePopular = e.getKey();
            }
        }

        ResumenAsistenciaDTO resumen = new ResumenAsistenciaDTO(totalClases, asistenciaPromedio, clasePopular);

        // Serie de barras (asistentes por clase)
        List<String> labelsBarras = new ArrayList<>(asistentesPorClase.keySet());
        Collections.sort(labelsBarras);
        List<Integer> valuesBarras = labelsBarras.stream().map(asistentesPorClase::get).collect(Collectors.toList());
        ChartSerieDTO barras = new ChartSerieDTO(labelsBarras, valuesBarras);

        // Serie de tendencia (asistentes por fecha)
        Map<LocalDate, Integer> asistentesPorFecha = new HashMap<>();
        for (ClaseAsistenciaItemDTO it : items) {
            if (it.getFecha() != null) {
                asistentesPorFecha.merge(it.getFecha(), it.getAsistentes(), Integer::sum);
            }
        }
        List<LocalDate> fechasOrdenadas = new ArrayList<>(asistentesPorFecha.keySet());
        Collections.sort(fechasOrdenadas);
        List<String> labelsTendencia = fechasOrdenadas.stream().map(LocalDate::toString).collect(Collectors.toList());
        List<Integer> valuesTendencia = fechasOrdenadas.stream().map(asistentesPorFecha::get).collect(Collectors.toList());
        ChartSerieDTO tendencia = new ChartSerieDTO(labelsTendencia, valuesTendencia);

        return new ReporteAsistenciaResponse(resumen, items, barras, tendencia);
    }

    private String derivarTipo(String nombre, String descripcion) {
        if (nombre != null && !nombre.isBlank()) {
            String first = nombre.trim().split("\\s+")[0];
            return first.toLowerCase();
        }
        if (descripcion != null && !descripcion.isBlank()) {
            String first = descripcion.trim().split("\\s+")[0];
            return first.toLowerCase();
        }
        return "general";
    }
}
