package com.integradorii.gimnasiov1.controller.api;

import com.integradorii.gimnasiov1.dto.ReporteAsistenciaResponse;
import com.integradorii.gimnasiov1.service.ReporteAsistenciaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ReportesApiController {

    private final ReporteAsistenciaService reporteAsistenciaService;

    public ReportesApiController(ReporteAsistenciaService reporteAsistenciaService) {
        this.reporteAsistenciaService = reporteAsistenciaService;
    }

    @GetMapping("/asistencia-clases")
    public ResponseEntity<ReporteAsistenciaResponse> reporteAsistencia(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(value = "tipo", required = false) String tipo
    ) {
        ZoneId zone = ZoneId.systemDefault();
        // Normalizamos el rango de búsqueda a la zona horaria del servidor
        OffsetDateTime desde = inicio.atStartOfDay(zone).toOffsetDateTime();
        // Sumamos un día para incluir todas las asistencias del día fin (intervalo [inicio, fin])
        OffsetDateTime hasta = fin.plusDays(1).atStartOfDay(zone).toOffsetDateTime(); // [inicio, fin]
        // Delegamos la generación del reporte al servicio de dominio
        ReporteAsistenciaResponse resp = reporteAsistenciaService.generar(desde, hasta, tipo);
        return ResponseEntity.ok(resp);
    }
}
