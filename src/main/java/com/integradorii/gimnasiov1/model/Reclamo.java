package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "reclamos")
public class Reclamo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deportista_id", nullable = false)
    private Persona deportista;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, length = 150)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 30)
    private String estado = "En proceso";

    @Column(name = "fecha_creacion", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime fechaActualizacion;

    @Column(length = 20)
    private String prioridad = "Normal";

    @Column(name = "respuesta_admin", columnDefinition = "TEXT")
    private String respuestaAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendido_por")
    private Persona atendidoPor;

    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = ZonedDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Persona getDeportista() {
        return deportista;
    }

    public void setDeportista(Persona deportista) {
        this.deportista = deportista;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public ZonedDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(ZonedDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public ZonedDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(ZonedDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getRespuestaAdmin() {
        return respuestaAdmin;
    }

    public void setRespuestaAdmin(String respuestaAdmin) {
        this.respuestaAdmin = respuestaAdmin;
    }

    public Persona getAtendidoPor() {
        return atendidoPor;
    }

    public void setAtendidoPor(Persona atendidoPor) {
        this.atendidoPor = atendidoPor;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
