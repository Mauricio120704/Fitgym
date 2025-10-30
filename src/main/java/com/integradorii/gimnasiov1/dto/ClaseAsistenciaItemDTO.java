package com.integradorii.gimnasiov1.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ClaseAsistenciaItemDTO {
    private Long claseId;
    private String clase;
    private String tipo;
    private String instructor;
    private LocalDate fecha;
    private LocalTime hora;
    private int asistentes;
    private int cupo;
    private int ocupacion;

    public ClaseAsistenciaItemDTO() {}

    public ClaseAsistenciaItemDTO(Long claseId, String clase, String tipo, String instructor,
                                  LocalDate fecha, LocalTime hora, int asistentes, int cupo, int ocupacion) {
        this.claseId = claseId;
        this.clase = clase;
        this.tipo = tipo;
        this.instructor = instructor;
        this.fecha = fecha;
        this.hora = hora;
        this.asistentes = asistentes;
        this.cupo = cupo;
        this.ocupacion = ocupacion;
    }

    public Long getClaseId() { return claseId; }
    public void setClaseId(Long claseId) { this.claseId = claseId; }
    public String getClase() { return clase; }
    public void setClase(String clase) { this.clase = clase; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public int getAsistentes() { return asistentes; }
    public void setAsistentes(int asistentes) { this.asistentes = asistentes; }
    public int getCupo() { return cupo; }
    public void setCupo(int cupo) { this.cupo = cupo; }
    public int getOcupacion() { return ocupacion; }
    public void setOcupacion(int ocupacion) { this.ocupacion = ocupacion; }
}
