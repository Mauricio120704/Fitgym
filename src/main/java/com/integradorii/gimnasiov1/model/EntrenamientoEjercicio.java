package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "entrenamiento_ejercicio")
public class EntrenamientoEjercicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenamiento_id", nullable = false)
    private Entrenamiento entrenamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @Column(nullable = false)
    private Integer orden;

    private Integer series;
    private String repeticiones; // ej: "12-10-8"
    @Column(name = "descanso_segundos")
    private Integer descansoSegundos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Entrenamiento getEntrenamiento() { return entrenamiento; }
    public void setEntrenamiento(Entrenamiento entrenamiento) { this.entrenamiento = entrenamiento; }
    public Ejercicio getEjercicio() { return ejercicio; }
    public void setEjercicio(Ejercicio ejercicio) { this.ejercicio = ejercicio; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public Integer getSeries() { return series; }
    public void setSeries(Integer series) { this.series = series; }
    public String getRepeticiones() { return repeticiones; }
    public void setRepeticiones(String repeticiones) { this.repeticiones = repeticiones; }
    public Integer getDescansoSegundos() { return descansoSegundos; }
    public void setDescansoSegundos(Integer descansoSegundos) { this.descansoSegundos = descansoSegundos; }
}
