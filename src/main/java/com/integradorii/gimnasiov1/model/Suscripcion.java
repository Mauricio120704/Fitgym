package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "suscripciones")
public class Suscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deportista_id", nullable = false)
    private Persona deportista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false, length = 20)
    private String estado; // Activa / Cancelada / Pausada

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "proximo_pago")
    private LocalDate proximoPago;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Persona getDeportista() { return deportista; }
    public void setDeportista(Persona deportista) { this.deportista = deportista; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public LocalDate getProximoPago() { return proximoPago; }
    public void setProximoPago(LocalDate proximoPago) { this.proximoPago = proximoPago; }
}
