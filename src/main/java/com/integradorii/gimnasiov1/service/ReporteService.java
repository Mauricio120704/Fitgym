package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.dto.MembresiaIngresoDTO;
import com.integradorii.gimnasiov1.dto.PaymentSummaryDTO;
import com.integradorii.gimnasiov1.model.Pago;
import com.integradorii.gimnasiov1.repository.PagoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    @Autowired
    private PagoRepository pagoRepository;

    /**
     * Generate membership income report
     */
    public Map<String, Object> generarReporteIngresosMembresias(LocalDate fechaInicio, LocalDate fechaFin) {
        // Get all membership payments within the date range
        List<MembresiaIngresoDTO> ingresosMembresias = pagoRepository.findMembresiaPaymentsByDateRange(fechaInicio, fechaFin);
        
        // Calculate totals
        double totalIngresos = ingresosMembresias.stream()
            .mapToDouble(MembresiaIngresoDTO::getMonto)
            .sum();
        
        // Group by membership type
        Map<String, Double> ingresosPorMembresia = ingresosMembresias.stream()
            .collect(Collectors.groupingBy(
                MembresiaIngresoDTO::getTipoMembresia,
                Collectors.summingDouble(MembresiaIngresoDTO::getMonto)
            ));
        
        // Get membership count by type
        Map<String, Long> cantidadMembresias = ingresosMembresias.stream()
            .collect(Collectors.groupingBy(
                MembresiaIngresoDTO::getTipoMembresia,
                Collectors.counting()
            ));
        
        // Prepare response
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("fechaInicio", fechaInicio);
        reporte.put("fechaFin", fechaFin);
        reporte.put("totalIngresos", totalIngresos);
        reporte.put("totalMembresias", ingresosMembresias.size());
        reporte.put("ingresosPorMembresia", ingresosPorMembresia);
        reporte.put("cantidadMembresias", cantidadMembresias);
        reporte.put("detalleIngresos", ingresosMembresias);
        
        return reporte;
    }
    
    /**
     * Export membership income report to Excel
     */
    public byte[] exportarReporteIngresosMembresiasExcel(LocalDate fechaInicio, LocalDate fechaFin) throws IOException {
        // Get report data
        Map<String, Object> reporte = generarReporteIngresosMembresias(fechaInicio, fechaFin);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create summary sheet
            Sheet sheet = workbook.createSheet("Ingresos por Membresías");
            
            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            // Title
            Row titleRow = sheet.createRow(0);
            createCell(titleRow, 0, "Reporte de Ingresos por Membresías", headerStyle);
            
            // Period
            Row periodRow = sheet.createRow(1);
            createCell(periodRow, 0, "Período:");
            createCell(periodRow, 1, String.format("%s a %s", fechaInicio, fechaFin));
            
            // Summary
            Row totalRow = sheet.createRow(3);
            createCell(totalRow, 0, "Total de Ingresos:", headerStyle);
            createCell(totalRow, 1, (Double) reporte.get("totalIngresos"), currencyStyle);
            
            Row countRow = sheet.createRow(4);
            createCell(countRow, 0, "Total de Membresías:", headerStyle);
            createCell(countRow, 1, (Integer) reporte.get("totalMembresias"));
            
            // Income by membership type
            @SuppressWarnings("unchecked")
            Map<String, Double> ingresosPorMembresia = (Map<String, Double>) reporte.get("ingresosPorMembresia");
            
            Row headerRow = sheet.createRow(6);
            createCell(headerRow, 0, "Tipo de Membresía", headerStyle);
            createCell(headerRow, 1, "Cantidad", headerStyle);
            createCell(headerRow, 2, "Total Ingresos", headerStyle);
            
            int rowNum = 7;
            for (Map.Entry<String, Double> entry : ingresosPorMembresia.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, entry.getKey());
                @SuppressWarnings("unchecked")
                Map<String, Long> cantidadMembresias = (Map<String, Long>) reporte.get("cantidadMembresias");
                createCell(row, 1, cantidadMembresias.get(entry.getKey()));
                createCell(row, 2, entry.getValue(), currencyStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }
    
    /**
     * Exporta el reporte a formato Excel
     */
    public byte[] exportarReporteExcel(LocalDate fechaInicio, LocalDate fechaFin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Crear hoja de resumen
            createSummarySheet(workbook, fechaInicio, fechaFin);
            
            // Crear hoja de transacciones
            createTransactionsSheet(workbook, fechaInicio, fechaFin);
            
            // Escribir el libro de trabajo a un ByteArrayOutputStream
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }
    
    /**
     * Get payment summary by date range
     */
    public Map<String, Object> generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Pago> pagos = pagoRepository.findCompletedPaymentsByDateRange(fechaInicio, fechaFin);
        List<PaymentSummaryDTO> resumen = pagoRepository.getPaymentSummaryByDateRange(fechaInicio, fechaFin);
        
        // Calculate totals
        double totalIngresos = pagos.stream().mapToDouble(Pago::getMonto).sum();
        long totalTransacciones = pagos.size();
        
        // Group by payment method
        Map<String, Double> ingresosPorMetodo = new HashMap<>();
        for (PaymentSummaryDTO item : resumen) {
            ingresosPorMetodo.merge(
                item.getMetodoPago(), 
                item.getTotalAmount(), 
                Double::sum
            );
        }
        
        // Prepare response
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("fechaInicio", fechaInicio);
        reporte.put("fechaFin", fechaFin);
        reporte.put("totalIngresos", totalIngresos);
        reporte.put("totalTransacciones", totalTransacciones);
        reporte.put("ingresosPorMetodo", ingresosPorMetodo);
        reporte.put("detallePorPlan", resumen);
        reporte.put("transacciones", pagos);
        
        return reporte;
    }
    
    /**
     * Helper method to create a cell with a value
     */
    private void createCell(Row row, int column, Object value) {
        createCell(row, column, value, null);
    }
    
    /**
     * Helper method to create a cell with a value and style
     */
    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
            } else if (value instanceof LocalDate) {
                cell.setCellValue(LocalDate.class.cast(value).toString());
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            } else {
                cell.setCellValue(value.toString());
            }
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
    
    /**
     * Crea la hoja de resumen del reporte
     */
    private void createSummarySheet(Workbook workbook, LocalDate fechaInicio, LocalDate fechaFin) {
        Sheet sheet = workbook.createSheet("Resumen");
        
        // Estilos
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);
        
        // Título del reporte
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reporte de Ingresos");
        titleCell.setCellStyle(boldStyle);
        
        // Período
        Row periodRow = sheet.createRow(1);
        periodRow.createCell(0).setCellValue("Período:");
        periodRow.createCell(1).setCellValue(String.format("%s - %s", 
            fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
            fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        
        // Obtener datos del reporte
        Map<String, Object> reporte = generarReporteIngresos(fechaInicio, fechaFin);
        
        // Resumen general
        int rowNum = 3;
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        summaryHeaderRow.createCell(0).setCellValue("Resumen General");
        
        Row totalIngresosRow = sheet.createRow(rowNum++);
        totalIngresosRow.createCell(0).setCellValue("Ingresos Totales:");
        Cell totalIngresosCell = totalIngresosRow.createCell(1);
        totalIngresosCell.setCellValue((Double) reporte.get("totalIngresos"));
        totalIngresosCell.setCellStyle(currencyStyle);
        
        Row totalTransaccionesRow = sheet.createRow(rowNum++);
        totalTransaccionesRow.createCell(0).setCellValue("Total Transacciones:");
        totalTransaccionesRow.createCell(1).setCellValue((Integer) reporte.get("totalTransacciones"));
        
        // Ingresos por método de pago
        rowNum += 2;
        Row metodoPagoHeaderRow = sheet.createRow(rowNum++);
        metodoPagoHeaderRow.createCell(0).setCellValue("Ingresos por Método de Pago");
        
        Row metodoPagoColumnsRow = sheet.createRow(rowNum++);
        metodoPagoColumnsRow.createCell(0).setCellValue("Método de Pago");
        metodoPagoColumnsRow.createCell(1).setCellValue("Monto Total");
        
        @SuppressWarnings("unchecked")
        Map<String, Double> ingresosPorMetodo = (Map<String, Double>) reporte.get("ingresosPorMetodo");
        for (Map.Entry<String, Double> entry : ingresosPorMetodo.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            Cell cell = row.createCell(1);
            cell.setCellValue(entry.getValue());
            cell.setCellStyle(currencyStyle);
        }
        
        // Detalle por plan
        rowNum += 2;
        Row planHeaderRow = sheet.createRow(rowNum++);
        planHeaderRow.createCell(0).setCellValue("Detalle por Plan");
        
        Row planColumnsRow = sheet.createRow(rowNum++);
        planColumnsRow.createCell(0).setCellValue("Plan");
        planColumnsRow.createCell(1).setCellValue("Cantidad");
        planColumnsRow.createCell(2).setCellValue("Método de Pago");
        planColumnsRow.createCell(3).setCellValue("Total");
        
        @SuppressWarnings("unchecked")
        List<PaymentSummaryDTO> detallePorPlan = (List<PaymentSummaryDTO>) reporte.get("detallePorPlan");
        for (PaymentSummaryDTO item : detallePorPlan) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getPlanServicio());
            row.createCell(1).setCellValue(item.getCount());
            row.createCell(2).setCellValue(item.getMetodoPago());
            Cell totalCell = row.createCell(3);
            totalCell.setCellValue(item.getTotalAmount());
            totalCell.setCellStyle(currencyStyle);
        }
        
        // Ajustar el ancho de las columnas
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    /**
     * Crea la hoja de transacciones detalladas
     */
    private void createTransactionsSheet(Workbook workbook, LocalDate fechaInicio, LocalDate fechaFin) {
        Sheet sheet = workbook.createSheet("Transacciones");
        
        // Estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Fecha", "Código", "Deportista", "Plan", "Método de Pago", "Monto"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Datos de transacciones
        List<Pago> transacciones = pagoRepository.findCompletedPaymentsByDateRange(fechaInicio, fechaFin);
        int rowNum = 1;
        
        for (Pago pago : transacciones) {
            Row row = sheet.createRow(rowNum++);
            
            // Fecha
            Cell fechaCell = row.createCell(0);
            fechaCell.setCellValue(java.sql.Date.valueOf(pago.getFecha()));
            fechaCell.setCellStyle(dateStyle);
            
            // Código
            row.createCell(1).setCellValue(pago.getCodigoPago());
            
            // Deportista
            String nombreCompleto = pago.getDeportista() != null ? 
                pago.getDeportista().getNombre() + " " + pago.getDeportista().getApellido() : "";
            row.createCell(2).setCellValue(nombreCompleto);
            
            // Plan
            row.createCell(3).setCellValue(pago.getPlanServicio());
            
            // Método de pago
            row.createCell(4).setCellValue(pago.getMetodoPago());
            
            // Monto
            Cell montoCell = row.createCell(5);
            montoCell.setCellValue(pago.getMonto());
            montoCell.setCellStyle(currencyStyle);
        }
        
        // Ajustar el ancho de las columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    /**
     * Crea un estilo para los encabezados
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Crea un estilo para fechas
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }
    
    /**
     * Crea un estilo para moneda
     */
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }
}
