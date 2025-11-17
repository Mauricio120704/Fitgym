package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comunidad_respuestas")
public class ComunidadRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private ComunidadPost post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deportista_id", nullable = false)
    private Persona deportista;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean activo = Boolean.TRUE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComunidadPost getPost() {
        return post;
    }

    public void setPost(ComunidadPost post) {
        this.post = post;
    }

    public Persona getDeportista() {
        return deportista;
    }

    public void setDeportista(Persona deportista) {
        this.deportista = deportista;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
