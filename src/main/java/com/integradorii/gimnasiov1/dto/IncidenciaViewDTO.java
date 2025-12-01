package com.integradorii.gimnasiov1.dto;

import java.time.OffsetDateTime;

public class IncidenciaViewDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String categoria;
    private String prioridad; // UI: ALTA/MEDIA/BAJA
    private String estado;    // UI: ABIERTO/RESUELTO
    private OffsetDateTime fechaReporte;
    private String reportadoPor;
    private String asignadoA; // puede ser null
    private String imagenes;  // Datos binarios de la imagen

    public IncidenciaViewDTO() {}

    public IncidenciaViewDTO(Long id, String titulo, String descripcion, String categoria, String prioridad,
                             String estado, OffsetDateTime fechaReporte, String reportadoPor,
                             String asignadoA, String imagenes) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaReporte = fechaReporte;
        this.reportadoPor = reportadoPor;
        this.asignadoA = asignadoA;
        this.imagenes = imagenes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public OffsetDateTime getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(OffsetDateTime fechaReporte) { this.fechaReporte = fechaReporte; }
    public String getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(String reportadoPor) { this.reportadoPor = reportadoPor; }
    public String getAsignadoA() { return asignadoA; }
    public void setAsignadoA(String asignadoA) { this.asignadoA = asignadoA; }
    public String getImagenes() { return imagenes; }
    public void setImagenes(String imagenes) { this.imagenes = imagenes; }
}
