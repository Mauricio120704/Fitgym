package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "plan_deportista")
public class PlanDeportista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deportista_id", nullable = false)
    private Persona deportista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenamiento_id", nullable = false)
    private Entrenamiento entrenamiento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false, length = 20)
    private String estado; // Activo, Completado, Cancelado

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Persona getDeportista() { return deportista; }
    public void setDeportista(Persona deportista) { this.deportista = deportista; }
    public Entrenamiento getEntrenamiento() { return entrenamiento; }
    public void setEntrenamiento(Entrenamiento entrenamiento) { this.entrenamiento = entrenamiento; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
