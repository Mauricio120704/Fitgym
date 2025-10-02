package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "incidencias")
public class Incidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false, length = 20)
    private String prioridad; // Baja, Media, Alta, Crítica

    @Column(nullable = false, length = 20)
    private String estado; // Abierta, Resuelto

    @Column(name = "fecha_reporte", nullable = false)
    private OffsetDateTime fechaReporte;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_por")
    private Persona reportadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_a")
    private Persona asignadoA;

    @Column(name = "ultima_actualizacion", nullable = false)
    private OffsetDateTime ultimaActualizacion;

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
    public OffsetDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(OffsetDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public Persona getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(Persona reportadoPor) { this.reportadoPor = reportadoPor; }
    public Persona getAsignadoA() { return asignadoA; }
    public void setAsignadoA(Persona asignadoA) { this.asignadoA = asignadoA; }
    public OffsetDateTime getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(OffsetDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
}
