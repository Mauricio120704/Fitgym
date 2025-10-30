package com.integradorii.gimnasiov1.dto;

import java.time.LocalDate;

public class MembresiaIngresoDTO {
    private Long idPago;
    private String tipoMembresia;
    private String nombreCliente;
    private LocalDate fechaPago;
    private double monto;
    private String metodoPago;

    // Constructor, getters and setters
    public MembresiaIngresoDTO() {
    }

    public MembresiaIngresoDTO(Long idPago, String tipoMembresia, String nombreCliente, 
                              LocalDate fechaPago, double monto, String metodoPago) {
        this.idPago = idPago;
        this.tipoMembresia = tipoMembresia;
        this.nombreCliente = nombreCliente;
        this.fechaPago = fechaPago;
        this.monto = monto;
        this.metodoPago = metodoPago;
    }

    // Getters and Setters
    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public String getTipoMembresia() {
        return tipoMembresia;
    }

    public void setTipoMembresia(String tipoMembresia) {
        this.tipoMembresia = tipoMembresia;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}
