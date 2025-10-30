package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para el registro de visitantes temporales al gimnasio.
 * Permite llevar un control de quiénes ingresan al local sin ser miembros.
 */
@Entity
@Table(name = "visitantes")
public class Visitante {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombreCompleto;

    @Column(nullable = false, length = 15)
    private String documentoIdentidad;

    @Column(length = 15)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private LocalDateTime fechaHoraIngreso;

    private LocalDateTime fechaHoraSalida;

    @Column(length = 20)
    private String codigoPase;

    @Column(length = 500)
    private String motivoVisita;

    @ManyToOne
    @JoinColumn(name = "registrado_por")
    private Usuario registradoPor;

    @ManyToOne
    @JoinColumn(name = "invitado_por_persona_id")
    private Persona invitadoPorPersona; // Miembro/deportista que invita

    @Column(length = 20)
    private String estado; // ACTIVO, FINALIZADO, CANCELADO

    // Constructor por defecto
    public Visitante() {
        this.fechaHoraIngreso = LocalDateTime.now();
        this.estado = "ACTIVO";
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }

    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) {
        this.fechaHoraIngreso = fechaHoraIngreso;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public String getCodigoPase() {
        return codigoPase;
    }

    public void setCodigoPase(String codigoPase) {
        this.codigoPase = codigoPase;
    }

    public String getMotivoVisita() {
        return motivoVisita;
    }

    public void setMotivoVisita(String motivoVisita) {
        this.motivoVisita = motivoVisita;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Persona getInvitadoPorPersona() {
        return invitadoPorPersona;
    }

    public void setInvitadoPorPersona(Persona invitadoPorPersona) {
        this.invitadoPorPersona = invitadoPorPersona;
    }

    // Método para marcar la salida del visitante
    public void registrarSalida() {
        this.fechaHoraSalida = LocalDateTime.now();
        this.estado = "FINALIZADO";
    }
}
