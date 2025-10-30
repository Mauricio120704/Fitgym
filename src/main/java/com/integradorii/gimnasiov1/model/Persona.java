package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad Persona - Solo para DEPORTISTAS (clientes del gimnasio)
 * El personal administrativo se gestiona en la tabla usuarios
 */
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

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "membresia_activa")
    private Boolean membresiaActiva = Boolean.TRUE;

    @Column(name = "activo")
    private Boolean activo = Boolean.FALSE; // Inactivo hasta verificar email

    @Column(name = "email_verificado")
    private Boolean emailVerificado = Boolean.FALSE;

    @Column(nullable = false, length = 255, name = "contraseña")
    private String contraseña;

    @Column(length = 10)
    private String genero; // M, F, Otro

    @Column(length = 200)
    private String direccion;

    @Column(length = 200)
    private String emergenciaContacto; // Nombre del contacto de emergencia

    @Column(length = 30)
    private String emergenciaTelefono; // Teléfono del contacto de emergencia

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
    
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    
    public Boolean getMembresiaActiva() { return membresiaActiva; }
    public void setMembresiaActiva(Boolean membresiaActiva) { this.membresiaActiva = membresiaActiva; }

    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }
    
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getEmergenciaContacto() { return emergenciaContacto; }
    public void setEmergenciaContacto(String emergenciaContacto) { this.emergenciaContacto = emergenciaContacto; }
    
    public String getEmergenciaTelefono() { return emergenciaTelefono; }
    public void setEmergenciaTelefono(String emergenciaTelefono) { this.emergenciaTelefono = emergenciaTelefono; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    public Boolean getEmailVerificado() { return emailVerificado; }
    public void setEmailVerificado(Boolean emailVerificado) { this.emailVerificado = emailVerificado; }
    
    // Helper method
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
