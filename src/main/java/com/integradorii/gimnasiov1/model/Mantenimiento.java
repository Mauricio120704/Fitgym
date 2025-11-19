package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mantenimientos")
public class Mantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(nullable = false)
    private LocalDate fechaServicio;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private String tipoServicio; // PREVENTIVO, CORRECTIVO, EMERGENCIA

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String costo;

    @Column(nullable = false)
    private String tecnicoResponsable;

    @Column(nullable = false)
    private String estado; // PROGRAMADO, EN_PROGRESO, COMPLETADO, CANCELADO

    @Column
    private String observaciones;

    @Column
    private String piezasReemplazadas;

    @Column
    private String proximaRevision;

    // Constructores
    public Mantenimiento() {}

    public Mantenimiento(Equipo equipo, LocalDate fechaServicio, LocalDateTime fechaRegistro,
                         String tipoServicio, String descripcion, String costo,
                         String tecnicoResponsable, String estado, String observaciones,
                         String piezasReemplazadas, String proximaRevision) {
        this.equipo = equipo;
        this.fechaServicio = fechaServicio;
        this.fechaRegistro = fechaRegistro;
        this.tipoServicio = tipoServicio;
        this.descripcion = descripcion;
        this.costo = costo;
        this.tecnicoResponsable = tecnicoResponsable;
        this.estado = estado;
        this.observaciones = observaciones;
        this.piezasReemplazadas = piezasReemplazadas;
        this.proximaRevision = proximaRevision;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public LocalDate getFechaServicio() {
        return fechaServicio;
    }

    public void setFechaServicio(LocalDate fechaServicio) {
        this.fechaServicio = fechaServicio;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCosto() {
        return costo;
    }

    public void setCosto(String costo) {
        this.costo = costo;
    }

    public String getTecnicoResponsable() {
        return tecnicoResponsable;
    }

    public void setTecnicoResponsable(String tecnicoResponsable) {
        this.tecnicoResponsable = tecnicoResponsable;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getPiezasReemplazadas() {
        return piezasReemplazadas;
    }

    public void setPiezasReemplazadas(String piezasReemplazadas) {
        this.piezasReemplazadas = piezasReemplazadas;
    }

    public String getProximaRevision() {
        return proximaRevision;
    }

    public void setProximaRevision(String proximaRevision) {
        this.proximaRevision = proximaRevision;
    }
}
