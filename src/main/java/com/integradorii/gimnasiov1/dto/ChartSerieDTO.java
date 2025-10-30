package com.integradorii.gimnasiov1.dto;

import java.util.List;

public class ChartSerieDTO {
    private List<String> labels;
    private List<Integer> values;

    public ChartSerieDTO() {}

    public ChartSerieDTO(List<String> labels, List<Integer> values) {
        this.labels = labels;
        this.values = values;
    }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Integer> getValues() { return values; }
    public void setValues(List<Integer> values) { this.values = values; }
}
