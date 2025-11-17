package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promociones")
public class Promocion {

    public enum TipoDescuento { PERCENTAGE, AMOUNT }
    public enum Estado { ACTIVE, INACTIVE, EXPIRED }
    public enum TipoPromocion { MEMBRESIA, CLASE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDescuento tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "codigo_stripe", length = 50)
    private String codigoStripe;

    @Column(name = "max_usos", nullable = false)
    private Integer maxUsos;

    @Column(name = "usados", nullable = false)
    private Integer usados = 0;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Estado estado = Estado.ACTIVE;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PromocionMembresia> membresias = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_promocion", nullable = false, length = 20)
    private TipoPromocion tipoPromocion = TipoPromocion.MEMBRESIA;

    @Column(name = "tipo_plan", length = 50)
    private String tipoPlan; // Ej: Básico, Premium, Elite. Opcional, solo para MEMBRESIA

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_id")
    private Clase clase; // Opcional, solo para promociones de CLASE

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoDescuento getTipo() { return tipo; }
    public void setTipo(TipoDescuento tipo) { this.tipo = tipo; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getCodigoStripe() { return codigoStripe; }
    public void setCodigoStripe(String codigoStripe) { this.codigoStripe = codigoStripe; }

    public Integer getMaxUsos() { return maxUsos; }
    public void setMaxUsos(Integer maxUsos) { this.maxUsos = maxUsos; }

    public Integer getUsados() { return usados; }
    public void setUsados(Integer usados) { this.usados = usados; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public List<PromocionMembresia> getMembresias() { return membresias; }
    public void setMembresias(List<PromocionMembresia> membresias) { this.membresias = membresias; }

    public TipoPromocion getTipoPromocion() { return tipoPromocion; }
    public void setTipoPromocion(TipoPromocion tipoPromocion) { this.tipoPromocion = tipoPromocion; }

    public String getTipoPlan() { return tipoPlan; }
    public void setTipoPlan(String tipoPlan) { this.tipoPlan = tipoPlan; }

    public Clase getClase() { return clase; }
    public void setClase(Clase clase) { this.clase = clase; }

    public Integer getUsosDisponibles() {
        if (maxUsos == null) {
            return null;
        }
        int usadosSeguros = (usados != null) ? usados : 0;
        return maxUsos - usadosSeguros;
    }
}
