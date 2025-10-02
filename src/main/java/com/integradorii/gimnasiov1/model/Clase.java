package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "clases")
public class Clase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id")
    private Persona entrenador;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    private OffsetDateTime fecha; // fecha/hora de la clase

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(nullable = false, length = 20)
    private String estado; // Programada, Cancelada, Finalizada

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Persona getEntrenador() { return entrenador; }
    public void setEntrenador(Persona entrenador) { this.entrenador = entrenador; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
