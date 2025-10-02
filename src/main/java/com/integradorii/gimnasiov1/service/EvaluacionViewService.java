package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.dto.EvaluacionViewDTO;
import com.integradorii.gimnasiov1.model.Evaluacion;
import org.springframework.stereotype.Service;

@Service
public class EvaluacionViewService {

    public EvaluacionViewDTO toView(Evaluacion e) {
        Double masaMuscular = 0.0; // campo no presente en entidad; mantenemos 0.0 para UI
        return new EvaluacionViewDTO(
                e.getId(),
                e.getFecha(),
                e.getPesoKg(),
                e.getGrasaCorporalPct(),
                masaMuscular,
                e.getImc(),
                e.getObservaciones()
        );
    }
}
