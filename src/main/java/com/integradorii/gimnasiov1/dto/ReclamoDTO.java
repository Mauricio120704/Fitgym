package com.integradorii.gimnasiov1.dto;

public class ReclamoDTO {
    
    private String categoria;
    private String asunto;
    private String descripcion;
    private String prioridad;
    
    public ReclamoDTO() {
    }
    
    public ReclamoDTO(String categoria, String asunto, String descripcion, String prioridad) {
        this.categoria = categoria;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
    }
    
    // Getters and Setters
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public String getAsunto() {
        return asunto;
    }
    
    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getPrioridad() {
        return prioridad;
    }
    
    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
}
