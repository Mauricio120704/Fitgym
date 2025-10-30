package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad Usuario - Para personal administrativo del gimnasio
 * (ADMINISTRADOR, RECEPCIONISTA, ENTRENADOR)
 */
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Role rol; // ADMINISTRADOR, RECEPCIONISTA o ENTRENADOR

    @Column(name = "activo")
    private Boolean activo = Boolean.TRUE;

    @Column(nullable = false, length = 255, name = "contraseña")
    private String contraseña;

    @Column(length = 200)
    private String departamento; // Ej: "Departamento de Clases Grupales"

    @Column(length = 100)
    private String puesto; // Ej: "Instructor de Yoga y Pilates"

    @Column(name = "fecha_inicio_laboral")
    private LocalDate fechaInicioLaboral;

    @Column(length = 150)
    private String horario; // Ej: "Lunes a Viernes, 16:00 - 22:00"

    // Constructors
    public Usuario() {}

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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public LocalDate getFechaInicioLaboral() {
        return fechaInicioLaboral;
    }

    public void setFechaInicioLaboral(LocalDate fechaInicioLaboral) {
        this.fechaInicioLaboral = fechaInicioLaboral;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    // Helper method
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
