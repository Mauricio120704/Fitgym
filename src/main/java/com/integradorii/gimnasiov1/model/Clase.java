package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "clases")
public class Clase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id")
    private Usuario entrenador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_clase_id", nullable = false)
    private TipoClase tipoClase;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(name = "cupos_basico", nullable = false)
    private Integer cuposBasico = 0;

    @Column(name = "cupos_premium", nullable = false)
    private Integer cuposPremium = 0;

    @Column(name = "cupos_elite", nullable = false)
    private Integer cuposElite = 0;

    @Column(nullable = false)
    private OffsetDateTime fecha; // fecha/hora de la clase

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(nullable = false, length = 20)
    private String estado; // Programada, Cancelada, Finalizada

    @Column(name = "es_pago", nullable = false)
    private Boolean esPago = Boolean.FALSE;

    @Column(name = "para_todos", nullable = false)
    private Boolean paraTodos = Boolean.FALSE;

    @Column(name = "precio", precision = 12, scale = 2)
    private BigDecimal precio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Usuario getEntrenador() { return entrenador; }
    public void setEntrenador(Usuario entrenador) { this.entrenador = entrenador; }
    public TipoClase getTipoClase() { return tipoClase; }
    public void setTipoClase(TipoClase tipoClase) { this.tipoClase = tipoClase; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public Integer getCuposBasico() { return cuposBasico; }
    public void setCuposBasico(Integer cuposBasico) { this.cuposBasico = cuposBasico; }
    public Integer getCuposPremium() { return cuposPremium; }
    public void setCuposPremium(Integer cuposPremium) { this.cuposPremium = cuposPremium; }
    public Integer getCuposElite() { return cuposElite; }
    public void setCuposElite(Integer cuposElite) { this.cuposElite = cuposElite; }
    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Boolean getEsPago() { return esPago; }
    public void setEsPago(Boolean esPago) { this.esPago = esPago; }
    public Boolean getParaTodos() { return paraTodos; }
    public void setParaTodos(Boolean paraTodos) { this.paraTodos = paraTodos; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
