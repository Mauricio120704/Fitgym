package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pagos")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_pago", nullable = false, unique = true, length = 30)
    private String codigoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deportista_id", nullable = false)
    private Persona deportista;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false, length = 20)
    private String estado; // Completado | Pendiente | Fallido

    @Column(name = "plan_servicio", nullable = false, length = 150)
    private String planServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por")
    private Persona registradoPor; // opcional

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoPago() { return codigoPago; }
    public void setCodigoPago(String codigoPago) { this.codigoPago = codigoPago; }
    public Persona getDeportista() { return deportista; }
    public void setDeportista(Persona deportista) { this.deportista = deportista; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getPlanServicio() { return planServicio; }
    public void setPlanServicio(String planServicio) { this.planServicio = planServicio; }
    public Persona getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(Persona registradoPor) { this.registradoPor = registradoPor; }
}
