package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clase_calificaciones")
public class ClaseCalificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @Column(name = "clase_id", nullable = false)
    private Long claseId;

    @Column(name = "deportista_id", nullable = false)
    private Long deportistaId;

    @Column(name = "rating_general")
    private Short ratingGeneral;

    @Column(name = "rating_instructor")
    private Short ratingInstructor;

    @Column(name = "rating_instalaciones")
    private Short ratingInstalaciones;

    @Column(name = "rating_musica")
    private Short ratingMusica;

    @Column(name = "rating_dificultad")
    private Short ratingDificultad;

    @Column(name = "comentario", columnDefinition = "text")
    private String comentario;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }

    public Long getClaseId() { return claseId; }
    public void setClaseId(Long claseId) { this.claseId = claseId; }

    public Long getDeportistaId() { return deportistaId; }
    public void setDeportistaId(Long deportistaId) { this.deportistaId = deportistaId; }

    public Short getRatingGeneral() { return ratingGeneral; }
    public void setRatingGeneral(Short ratingGeneral) { this.ratingGeneral = ratingGeneral; }

    public Short getRatingInstructor() { return ratingInstructor; }
    public void setRatingInstructor(Short ratingInstructor) { this.ratingInstructor = ratingInstructor; }

    public Short getRatingInstalaciones() { return ratingInstalaciones; }
    public void setRatingInstalaciones(Short ratingInstalaciones) { this.ratingInstalaciones = ratingInstalaciones; }

    public Short getRatingMusica() { return ratingMusica; }
    public void setRatingMusica(Short ratingMusica) { this.ratingMusica = ratingMusica; }

    public Short getRatingDificultad() { return ratingDificultad; }
    public void setRatingDificultad(Short ratingDificultad) { this.ratingDificultad = ratingDificultad; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
