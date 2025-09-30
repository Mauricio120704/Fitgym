package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "clases")
public class Clase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String instructor;
    
    @Column(nullable = false)
    private LocalDate fecha;
    
    @Column(nullable = false)
    private LocalTime hora;
    
    @Column(nullable = false)
    private Integer duracion;
    
    @Column(name = "cupos_premium", nullable = false)
    private Integer cuposPremium;
    
    @Column(name = "cupos_elite", nullable = false)
    private Integer cuposElite;
    
    @Column(name = "ocupados_premium", nullable = false)
    private Integer ocupadosPremium = 0;
    
    @Column(name = "ocupados_elite", nullable = false)
    private Integer ocupadosElite = 0;
    
    @Column(name = "precio_premium")
    private BigDecimal precioPremium;
    
    @Column(name = "precio_elite")
    private BigDecimal precioElite;
    
    @Column(length = 20)
    private String estado = "ACTIVA";
    
    // Constructors
    public Clase() {
    }
    
    public Clase(String nombre, String instructor, LocalDate fecha, LocalTime hora, Integer duracion, 
                 Integer cuposPremium, Integer cuposElite) {
        this.nombre = nombre;
        this.instructor = instructor;
        this.fecha = fecha;
        this.hora = hora;
        this.duracion = duracion;
        this.cuposPremium = cuposPremium;
        this.cuposElite = cuposElite;
        this.ocupadosPremium = 0;
        this.ocupadosElite = 0;
        this.estado = "ACTIVA";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getInstructor() {
        return instructor;
    }
    
    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public LocalTime getHora() {
        return hora;
    }
    
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
    
    public Integer getDuracion() {
        return duracion;
    }
    
    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }
    
    public Integer getCuposPremium() {
        return cuposPremium;
    }
    
    public void setCuposPremium(Integer cuposPremium) {
        this.cuposPremium = cuposPremium;
    }
    
    public Integer getCuposElite() {
        return cuposElite;
    }
    
    public void setCuposElite(Integer cuposElite) {
        this.cuposElite = cuposElite;
    }
    
    public Integer getOcupadosPremium() {
        return ocupadosPremium;
    }
    
    public void setOcupadosPremium(Integer ocupadosPremium) {
        this.ocupadosPremium = ocupadosPremium;
    }
    
    public Integer getOcupadosElite() {
        return ocupadosElite;
    }
    
    public void setOcupadosElite(Integer ocupadosElite) {
        this.ocupadosElite = ocupadosElite;
    }
    
    public BigDecimal getPrecioPremium() {
        return precioPremium;
    }
    
    public void setPrecioPremium(BigDecimal precioPremium) {
        this.precioPremium = precioPremium;
    }
    
    public BigDecimal getPrecioElite() {
        return precioElite;
    }
    
    public void setPrecioElite(BigDecimal precioElite) {
        this.precioElite = precioElite;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
}
