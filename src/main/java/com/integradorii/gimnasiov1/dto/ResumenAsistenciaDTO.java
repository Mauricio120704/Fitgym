package com.integradorii.gimnasiov1.dto;

public class ResumenAsistenciaDTO {
    private int totalClases;
    private int asistenciaPromedio; // porcentaje entero 0-100
    private String clasePopular;

    public ResumenAsistenciaDTO() {}

    public ResumenAsistenciaDTO(int totalClases, int asistenciaPromedio, String clasePopular) {
        this.totalClases = totalClases;
        this.asistenciaPromedio = asistenciaPromedio;
        this.clasePopular = clasePopular;
    }

    public int getTotalClases() { return totalClases; }
    public void setTotalClases(int totalClases) { this.totalClases = totalClases; }
    public int getAsistenciaPromedio() { return asistenciaPromedio; }
    public void setAsistenciaPromedio(int asistenciaPromedio) { this.asistenciaPromedio = asistenciaPromedio; }
    public String getClasePopular() { return clasePopular; }
    public void setClasePopular(String clasePopular) { this.clasePopular = clasePopular; }
}
