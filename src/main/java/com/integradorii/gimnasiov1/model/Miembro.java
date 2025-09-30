package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "miembros")
public class Miembro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String apellido;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String telefono;
    
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDate fechaRegistro;
    
    @Column(name = "membresia_activa")
    private Boolean membresiaActiva;
    
    @Column(name = "tipo_membresia")
    private String tipoMembresia;
    
    // Constructor vacío
    public Miembro() {
        this.membresiaActiva = true;
        this.tipoMembresia = "BASICA";
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDate.now();
        }
    }
    
    // Constructor con parámetros
    public Miembro(String nombre, String apellido, String email, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.fechaRegistro = LocalDate.now();
        this.membresiaActiva = true;
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
    
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public Boolean getMembresiaActiva() {
        return membresiaActiva;
    }
    
    public void setMembresiaActiva(Boolean membresiaActiva) {
        this.membresiaActiva = membresiaActiva;
    }
    
    public String getTipoMembresia() {
        return tipoMembresia;
    }
    
    public void setTipoMembresia(String tipoMembresia) {
        this.tipoMembresia = tipoMembresia;
    }
}
