package com.integradorii.gimnasiov1.dto;

public class PaymentSummaryDTO {
    private String planServicio;
    private long count;
    private double totalAmount;
    private String metodoPago;

    public PaymentSummaryDTO(String planServicio, long count, double totalAmount, String metodoPago) {
        this.planServicio = planServicio;
        this.count = count;
        this.totalAmount = totalAmount;
        this.metodoPago = metodoPago;
    }

    // Getters and setters
    public String getPlanServicio() {
        return planServicio;
    }

    public void setPlanServicio(String planServicio) {
        this.planServicio = planServicio;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}
