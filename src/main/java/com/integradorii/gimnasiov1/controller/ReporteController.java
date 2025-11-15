package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/ingresos-membresias")
    public String mostrarReporteIngresosMembresias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {
        
        // Set default date range to current month if not provided
        if (fechaInicio == null || fechaFin == null) {
            YearMonth currentMonth = YearMonth.now();
            fechaInicio = currentMonth.atDay(1);
            fechaFin = currentMonth.atEndOfMonth();
        }
        
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        
        // Generate the membership income report
        Map<String, Object> reporte = reporteService.generarReporteIngresosMembresias(fechaInicio, fechaFin);
        model.addAllAttributes(Objects.requireNonNull(reporte));
        
        return "reportes/ingresos-membresias";
    }
    
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/ingresos-membresias/exportar")
    public ResponseEntity<byte[]> exportarReporteIngresosMembresiasExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            byte[] excelFile = reporteService.exportarReporteIngresosMembresiasExcel(fechaInicio, fechaFin);
            
            String nombreArchivo = String.format("reporte-ingresos-membresias_%s_a_%s.xlsx",
                fechaInicio.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                fechaFin.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
