package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.dto.EntrenamientoViewDTO;
import com.integradorii.gimnasiov1.model.Entrenamiento;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EntrenamientoViewService {

    public EntrenamientoViewDTO toView(Entrenamiento e) {
        Map<String, String> parsed = parse(e.getDescripcion());
        String tipo = e.getNombre() == null ? "" : e.getNombre();
        return new EntrenamientoViewDTO(
                e.getId(),
                tipo,
                emoji(tipo),
                parsed.getOrDefault("dia", ""),
                parsed.getOrDefault("hora", ""),
                parsed.getOrDefault("dur", "0"),
                parsed.getOrDefault("notas", "")
        );
    }

    public String serialize(String diaSemana, String horaInicio, Integer duracion, String notas) {
        return String.format("dia=%s;hora=%s;dur=%d;notas=%s",
                safe(diaSemana), safe(horaInicio), duracion == null ? 0 : duracion, safe(notas));
    }

    private String emoji(String tipo) {
        if (tipo == null) return "";
        switch (tipo.toLowerCase()) {
            case "fuerza": return "💪";
            case "cardio": return "🏃";
            case "yoga": return "🧘";
            case "pilates": return "🤸";
            case "funcional": return "⚡";
            case "flexibilidad": return "🤲";
            default: return "🏋️";
        }
    }

    private String safe(String s) { return s == null ? "" : s.replace(";", ","); }

    private Map<String, String> parse(String desc) {
        Map<String, String> map = new HashMap<>();
        if (desc == null || desc.isEmpty()) return map;
        String[] parts = desc.split(";");
        for (String p : parts) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }
}
