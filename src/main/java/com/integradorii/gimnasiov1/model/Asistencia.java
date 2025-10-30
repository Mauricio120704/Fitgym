package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asistencias")
public class Asistencia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;
    
    @Column(name = "fecha_hora_ingreso", nullable = false)
    private LocalDateTime fechaHoraIngreso;
    
    @Column(name = "fecha_hora_salida")
    private LocalDateTime fechaHoraSalida;
    
    @Transient
    private Long tiempoEstadiaMinutos; // Tiempo en minutos
    
    // Getters y setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Persona getPersona() {
        return persona;
    }
    
    public void setPersona(Persona persona) {
        this.persona = persona;
    }
    
    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }
    
    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) {
        this.fechaHoraIngreso = fechaHoraIngreso;
    }
    
    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }
    
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }
    
    public Long getTiempoEstadiaMinutos() {
        if (fechaHoraIngreso != null && fechaHoraSalida != null) {
            return java.time.Duration.between(fechaHoraIngreso, fechaHoraSalida).toMinutes();
        }
        return null;
    }
    
    public void setTiempoEstadiaMinutos(Long tiempoEstadiaMinutos) {
        this.tiempoEstadiaMinutos = tiempoEstadiaMinutos;
    }
}
