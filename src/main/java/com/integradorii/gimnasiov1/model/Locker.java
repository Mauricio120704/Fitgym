package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lockers")
public class Locker {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLocker estado = EstadoLocker.DISPONIBLE;

    @ManyToOne
    @JoinColumn(name = "persona_id")
    private Persona personaAsignada;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public EstadoLocker getEstado() { return estado; }
    public void setEstado(EstadoLocker estado) { this.estado = estado; }
    
    public Persona getPersonaAsignada() { return personaAsignada; }
    public void setPersonaAsignada(Persona personaAsignada) { this.personaAsignada = personaAsignada; }
    
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
}
