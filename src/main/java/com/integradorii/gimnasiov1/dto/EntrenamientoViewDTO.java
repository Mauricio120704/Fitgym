package com.integradorii.gimnasiov1.dto;

public class EntrenamientoViewDTO {
    private Long id;
    private String tipo;
    private String emoji;
    private String diaSemana;
    private String horaInicio;
    private String duracion; // mantener como string para simplicidad en la vista
    private String notas;

    public EntrenamientoViewDTO() {}

    public EntrenamientoViewDTO(Long id, String tipo, String emoji, String diaSemana, String horaInicio, String duracion, String notas) {
        this.id = id;
        this.tipo = tipo;
        this.emoji = emoji;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.duracion = duracion;
        this.notas = notas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
