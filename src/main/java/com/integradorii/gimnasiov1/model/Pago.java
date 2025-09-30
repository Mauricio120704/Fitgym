package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pagos")
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo_pago", unique = true, nullable = false)
    private String codigoPago;
    
    @Column(nullable = false)
    private LocalDate fecha;
    
    @Column(name = "plan_servicio", nullable = false)
    private String planServicio;
    
    @Column(name = "metodo_pago", nullable = false)
    private String metodoPago;
    
    @Column(nullable = false)
    private Double monto;
    
    @Column(nullable = false)
    private String estado; // Completado, Pendiente, Fallido
    
    @ManyToOne
    @JoinColumn(name = "miembro_id")
    private Miembro miembro;
    
    // Constructor vacío
    public Pago() {
        this.fecha = LocalDate.now();
        this.estado = "Pendiente";
    }
    
    // Constructor con parámetros
    public Pago(String codigoPago, String planServicio, String metodoPago, Double monto, String estado) {
        this.codigoPago = codigoPago;
        this.fecha = LocalDate.now();
        this.planServicio = planServicio;
        this.metodoPago = metodoPago;
        this.monto = monto;
        this.estado = estado;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigoPago() {
        return codigoPago;
    }
    
    public void setCodigoPago(String codigoPago) {
        this.codigoPago = codigoPago;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public String getPlanServicio() {
        return planServicio;
    }
    
    public void setPlanServicio(String planServicio) {
        this.planServicio = planServicio;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public Double getMonto() {
        return monto;
    }
    
    public void setMonto(Double monto) {
        this.monto = monto;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Miembro getMiembro() {
        return miembro;
    }
    
    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }
}
