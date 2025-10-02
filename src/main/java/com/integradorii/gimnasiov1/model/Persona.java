package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "personas")
public class Persona {
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

    @Column(nullable = false, length = 20)
    private String tipo; // PERSONAL | DEPORTISTA

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id")
    private Role rol; // solo para PERSONAL

    @Column(name = "membresia_activa")
    private Boolean membresiaActiva = Boolean.TRUE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Role getRol() { return rol; }
    public void setRol(Role rol) { this.rol = rol; }
    public Boolean getMembresiaActiva() { return membresiaActiva; }
    public void setMembresiaActiva(Boolean membresiaActiva) { this.membresiaActiva = membresiaActiva; }
}
