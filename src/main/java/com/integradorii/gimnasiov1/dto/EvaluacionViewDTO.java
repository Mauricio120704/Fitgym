package com.integradorii.gimnasiov1.dto;

import java.time.OffsetDateTime;

public class EvaluacionViewDTO {
    private Long id;
    private OffsetDateTime fecha;
    private Double peso;
    private Double grasaCorporal;
    private Double masaMuscular; // puede no existir en BD; usamos 0.0 si no aplica
    private Double imc;
    private String notas;

    public EvaluacionViewDTO() {}

    public EvaluacionViewDTO(Long id, OffsetDateTime fecha, Double peso, Double grasaCorporal,
                              Double masaMuscular, Double imc, String notas) {
        this.id = id;
        this.fecha = fecha;
        this.peso = peso;
        this.grasaCorporal = grasaCorporal;
        this.masaMuscular = masaMuscular;
        this.imc = imc;
        this.notas = notas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public Double getGrasaCorporal() { return grasaCorporal; }
    public void setGrasaCorporal(Double grasaCorporal) { this.grasaCorporal = grasaCorporal; }
    public Double getMasaMuscular() { return masaMuscular; }
    public void setMasaMuscular(Double masaMuscular) { this.masaMuscular = masaMuscular; }
    public Double getImc() { return imc; }
    public void setImc(Double imc) { this.imc = imc; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
