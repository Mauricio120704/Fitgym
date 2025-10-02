package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reservas_clase")
public class ReservaClase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_id", nullable = false)
    private Clase clase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deportista_id", nullable = false)
    private Persona deportista;

    @Column(name = "reservado_en", nullable = false)
    private OffsetDateTime reservadoEn;

    @Column(nullable = false, length = 20)
    private String estado; // Reservado, Asistió, Cancelado

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Clase getClase() { return clase; }
    public void setClase(Clase clase) { this.clase = clase; }
    public Persona getDeportista() { return deportista; }
    public void setDeportista(Persona deportista) { this.deportista = deportista; }
    public OffsetDateTime getReservadoEn() { return reservadoEn; }
    public void setReservadoEn(OffsetDateTime reservadoEn) { this.reservadoEn = reservadoEn; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
