package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "entrenamientos")
public class Entrenamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre; // usaremos el tipo (Fuerza/Cardio/etc.)

    @Column(columnDefinition = "text")
    private String descripcion; // guardaremos diaSemana|hora|duracion|notas serializado simple

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Persona creadoPor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Persona getCreadoPor() { return creadoPor; }
    public void setCreadoPor(Persona creadoPor) { this.creadoPor = creadoPor; }
}
