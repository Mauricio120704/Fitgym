package com.integradorii.gimnasiov1.dto;

import java.time.LocalDateTime;

public class AsistenciaDTO {
    private Long id;
    private String nombreCompleto;
    private String dni;
    private String estadoMembresia;
    private String tipoMembresia;
    private Long diasRestantes;
    private String fotoUrl;
    private boolean tieneCheckinActivo;
    private LocalDateTime fechaIngreso;

    // Constructor vacío
    public AsistenciaDTO() {}

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

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEstadoMembresia() {
        return estadoMembresia;
    }

    public void setEstadoMembresia(String estadoMembresia) {
        this.estadoMembresia = estadoMembresia;
    }

    public String getTipoMembresia() {
        return tipoMembresia;
    }

    public void setTipoMembresia(String tipoMembresia) {
        this.tipoMembresia = tipoMembresia;
    }

    public Long getDiasRestantes() {
        return diasRestantes;
    }

    public void setDiasRestantes(Long diasRestantes) {
        this.diasRestantes = diasRestantes;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public boolean isTieneCheckinActivo() {
        return tieneCheckinActivo;
    }

    public void setTieneCheckinActivo(boolean tieneCheckinActivo) {
        this.tieneCheckinActivo = tieneCheckinActivo;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}
