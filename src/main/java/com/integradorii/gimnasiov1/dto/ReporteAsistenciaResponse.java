package com.integradorii.gimnasiov1.dto;

import java.util.List;

public class ReporteAsistenciaResponse {
    private ResumenAsistenciaDTO resumen;
    private List<ClaseAsistenciaItemDTO> items;
    private ChartSerieDTO barras;
    private ChartSerieDTO tendencia;

    public ReporteAsistenciaResponse() {}

    public ReporteAsistenciaResponse(ResumenAsistenciaDTO resumen,
                                     List<ClaseAsistenciaItemDTO> items,
                                     ChartSerieDTO barras,
                                     ChartSerieDTO tendencia) {
        this.resumen = resumen;
        this.items = items;
        this.barras = barras;
        this.tendencia = tendencia;
    }

    public ResumenAsistenciaDTO getResumen() { return resumen; }
    public void setResumen(ResumenAsistenciaDTO resumen) { this.resumen = resumen; }

    public List<ClaseAsistenciaItemDTO> getItems() { return items; }
    public void setItems(List<ClaseAsistenciaItemDTO> items) { this.items = items; }

    public ChartSerieDTO getBarras() { return barras; }
    public void setBarras(ChartSerieDTO barras) { this.barras = barras; }

    public ChartSerieDTO getTendencia() { return tendencia; }
    public void setTendencia(ChartSerieDTO tendencia) { this.tendencia = tendencia; }
}
