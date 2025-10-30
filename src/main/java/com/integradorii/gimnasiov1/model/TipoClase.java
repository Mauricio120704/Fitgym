package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_clase")
public class TipoClase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "duracion_predeterminada")
    private Integer duracionPredeterminada;

    @Column(name = "color_codigo", length = 7)
    private String colorCodigo;

    @Column(nullable = false)
    private Boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getDuracionPredeterminada() { return duracionPredeterminada; }
    public void setDuracionPredeterminada(Integer duracionPredeterminada) { this.duracionPredeterminada = duracionPredeterminada; }
    public String getColorCodigo() { return colorCodigo; }
    public void setColorCodigo(String colorCodigo) { this.colorCodigo = colorCodigo; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
