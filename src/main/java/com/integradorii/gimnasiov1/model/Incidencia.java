package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias")
public class Incidencia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "reportado_por", nullable = false)
    private String reportadoPor;
    
    @Column(nullable = false, length = 50)
    private String prioridad; // BAJA, MEDIA, ALTA
    
    @Column(nullable = false, length = 50)
    private String estado = "ABIERTO"; // ABIERTO, RESUELTO
    
    @Column(name = "asignado_a")
    private String asignadoA;
    
    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;
    
    @Column(name = "imagenes", columnDefinition = "TEXT")
    private String imagenes; // JSON array de URLs de imágenes
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
    
    @ManyToOne
    @JoinColumn(name = "miembro_id")
    private Miembro miembro;
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
    
    // Constructors
    public Incidencia() {
    }
    
    public Incidencia(String titulo, String descripcion, String reportadoPor, 
                      String prioridad, String asignadoA) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.reportadoPor = reportadoPor;
        this.prioridad = prioridad;
        this.asignadoA = asignadoA;
        this.estado = "ABIERTO";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getReportadoPor() {
        return reportadoPor;
    }
    
    public void setReportadoPor(String reportadoPor) {
        this.reportadoPor = reportadoPor;
    }
    
    public String getPrioridad() {
        return prioridad;
    }
    
    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getAsignadoA() {
        return asignadoA;
    }
    
    public void setAsignadoA(String asignadoA) {
        this.asignadoA = asignadoA;
    }
    
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
    
    public String getImagenes() {
        return imagenes;
    }
    
    public void setImagenes(String imagenes) {
        this.imagenes = imagenes;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public Miembro getMiembro() {
        return miembro;
    }
    
    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }
}
