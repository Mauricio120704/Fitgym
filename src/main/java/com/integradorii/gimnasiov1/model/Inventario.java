package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String codigoProducto;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer stockMinimo;

    @Column(nullable = false)
    private Integer stockMaximo;

    @Column(nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private String proveedor;

    @Column(nullable = false)
    private String ubicacion;

    @Column(nullable = false)
    private LocalDate fechaIngreso;

    @Column(nullable = false)
    private String estado; // DISPONIBLE, BAJO_STOCK, AGOTADO, DESCONTINUADO

    @Column(nullable = false)
    private LocalDate ultimaActualizacion;

    // Constructors
    public Inventario() {
        this.fechaIngreso = LocalDate.now();
        this.ultimaActualizacion = LocalDate.now();
        this.estado = "DISPONIBLE";
    }

    public Inventario(String nombre, String categoria, String codigoProducto, String descripcion, 
                     Integer cantidad, Integer stockMinimo, Integer stockMaximo, 
                     Double precioUnitario, String proveedor, String ubicacion) {
        this();
        this.nombre = nombre;
        this.categoria = categoria;
        this.codigoProducto = codigoProducto;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
        this.precioUnitario = precioUnitario;
        this.proveedor = proveedor;
        this.ubicacion = ubicacion;
        actualizarEstado();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        actualizarEstado();
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
        actualizarEstado();
    }

    public Integer getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(Integer stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDate ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    // Métodos de negocio
    public void actualizarEstado() {
        this.ultimaActualizacion = LocalDate.now();
        
        if (cantidad == null || cantidad <= 0) {
            this.estado = "AGOTADO";
        } else if (stockMinimo != null && cantidad <= stockMinimo) {
            this.estado = "BAJO_STOCK";
        } else {
            this.estado = "DISPONIBLE";
        }
    }

    public boolean necesitaReorden() {
        return stockMinimo != null && cantidad != null && cantidad <= stockMinimo;
    }

    public boolean puedeSolicitar(Integer cantidadSolicitada) {
        return cantidad >= cantidadSolicitada && "DISPONIBLE".equals(estado);
    }

    public void reducirStock(Integer cantidadReducir) {
        if (cantidadReducir > 0 && cantidad >= cantidadReducir) {
            this.cantidad -= cantidadReducir;
            actualizarEstado();
        }
    }

    public void aumentarStock(Integer cantidadAumentar) {
        if (cantidadAumentar > 0) {
            this.cantidad += cantidadAumentar;
            actualizarEstado();
        }
    }

    public Double getValorTotal() {
        if (cantidad == null || precioUnitario == null) {
            return 0.0;
        }
        return cantidad * precioUnitario;
    }
}
