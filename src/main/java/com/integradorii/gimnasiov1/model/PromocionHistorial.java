package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "promocion_historial")
public class PromocionHistorial {

    public enum Accion { CREAR, EDITAR, TOGGLE, REACTIVAR, ELIMINAR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promocion_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Promocion promocion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Accion accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 15)
    private Promocion.Estado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", length = 15)
    private Promocion.Estado estadoNuevo;

    @Column(columnDefinition = "text")
    private String detalle;

    @Column(name = "realizado_en", nullable = false)
    private LocalDateTime realizadoEn = LocalDateTime.now();

    @Column(name = "usuario", length = 120)
    private String usuario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Promocion getPromocion() { return promocion; }
    public void setPromocion(Promocion promocion) { this.promocion = promocion; }

    public Accion getAccion() { return accion; }
    public void setAccion(Accion accion) { this.accion = accion; }

    public Promocion.Estado getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(Promocion.Estado estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public Promocion.Estado getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(Promocion.Estado estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public LocalDateTime getRealizadoEn() { return realizadoEn; }
    public void setRealizadoEn(LocalDateTime realizadoEn) { this.realizadoEn = realizadoEn; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
