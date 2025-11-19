package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private String numeroSerie;

    @Column(nullable = false)
    private String ubicacion;

    @Column(nullable = false)
    private LocalDate fechaAdquisicion;

    @Column(nullable = false)
    private String estado; // ACTIVO, MANTENIMIENTO, DAÑADO, FUERA_DE_SERVICIO

    @Column
    private String descripcion;

    @Column(nullable = false)
    private LocalDate ultimoMantenimiento;

    @Column
    private LocalDate proximoMantenimiento;

    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Mantenimiento> mantenimientos;

    // Constructores
    public Equipo() {}

    public Equipo(String nombre, String tipo, String marca, String modelo, String numeroSerie, 
                  String ubicacion, LocalDate fechaAdquisicion, String estado, String descripcion,
                  LocalDate ultimoMantenimiento, LocalDate proximoMantenimiento) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
        this.ubicacion = ubicacion;
        this.fechaAdquisicion = fechaAdquisicion;
        this.estado = estado;
        this.descripcion = descripcion;
        this.ultimoMantenimiento = ultimoMantenimiento;
        this.proximoMantenimiento = proximoMantenimiento;
    }

    // Getters y Setters
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDate getFechaAdquisicion() {
        return fechaAdquisicion;
    }

    public void setFechaAdquisicion(LocalDate fechaAdquisicion) {
        this.fechaAdquisicion = fechaAdquisicion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getUltimoMantenimiento() {
        return ultimoMantenimiento;
    }

    public void setUltimoMantenimiento(LocalDate ultimoMantenimiento) {
        this.ultimoMantenimiento = ultimoMantenimiento;
    }

    public LocalDate getProximoMantenimiento() {
        return proximoMantenimiento;
    }

    public void setProximoMantenimiento(LocalDate proximoMantenimiento) {
        this.proximoMantenimiento = proximoMantenimiento;
    }

    public List<Mantenimiento> getMantenimientos() {
        return mantenimientos;
    }

    public void setMantenimientos(List<Mantenimiento> mantenimientos) {
        this.mantenimientos = mantenimientos;
    }
}
