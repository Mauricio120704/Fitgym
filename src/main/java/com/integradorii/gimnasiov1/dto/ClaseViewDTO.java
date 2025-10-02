package com.integradorii.gimnasiov1.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ClaseViewDTO {
    private Long id;
    private String nombre;
    private String instructor;
    private LocalDate fecha;
    private LocalTime hora;
    private Integer duracion;
    private Integer cuposPremium;
    private Integer cuposElite;
    private Long ocupadosPremium;
    private Long ocupadosElite;

    public ClaseViewDTO() {}

    public ClaseViewDTO(Long id, String nombre, String instructor, LocalDate fecha, LocalTime hora,
                         Integer duracion, Integer cuposPremium, Integer cuposElite,
                         Long ocupadosPremium, Long ocupadosElite) {
        this.id = id;
        this.nombre = nombre;
        this.instructor = instructor;
        this.fecha = fecha;
        this.hora = hora;
        this.duracion = duracion;
        this.cuposPremium = cuposPremium;
        this.cuposElite = cuposElite;
        this.ocupadosPremium = ocupadosPremium;
        this.ocupadosElite = ocupadosElite;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }
    public Integer getCuposPremium() { return cuposPremium; }
    public void setCuposPremium(Integer cuposPremium) { this.cuposPremium = cuposPremium; }
    public Integer getCuposElite() { return cuposElite; }
    public void setCuposElite(Integer cuposElite) { this.cuposElite = cuposElite; }
    public Long getOcupadosPremium() { return ocupadosPremium; }
    public void setOcupadosPremium(Long ocupadosPremium) { this.ocupadosPremium = ocupadosPremium; }
    public Long getOcupadosElite() { return ocupadosElite; }
    public void setOcupadosElite(Long ocupadosElite) { this.ocupadosElite = ocupadosElite; }
}
