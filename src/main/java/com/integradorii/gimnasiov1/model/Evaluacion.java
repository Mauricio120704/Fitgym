package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "evaluaciones")
public class Evaluacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deportista_id", nullable = false)
    private Persona deportista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluador_id")
    private Persona evaluador;

    @Column(nullable = false)
    private OffsetDateTime fecha;

    @Column(name = "peso_kg")
    private Double pesoKg;

    @Column(name = "estatura_cm")
    private Double estaturaCm;

    @Column(name = "imc")
    private Double imc;

    @Column(name = "grasa_corporal_pct")
    private Double grasaCorporalPct;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Persona getDeportista() { return deportista; }
    public void setDeportista(Persona deportista) { this.deportista = deportista; }
    public Persona getEvaluador() { return evaluador; }
    public void setEvaluador(Persona evaluador) { this.evaluador = evaluador; }
    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
    public Double getPesoKg() { return pesoKg; }
    public void setPesoKg(Double pesoKg) { this.pesoKg = pesoKg; }
    public Double getEstaturaCm() { return estaturaCm; }
    public void setEstaturaCm(Double estaturaCm) { this.estaturaCm = estaturaCm; }
    public Double getImc() { return imc; }
    public void setImc(Double imc) { this.imc = imc; }
    public Double getGrasaCorporalPct() { return grasaCorporalPct; }
    public void setGrasaCorporalPct(Double grasaCorporalPct) { this.grasaCorporalPct = grasaCorporalPct; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
