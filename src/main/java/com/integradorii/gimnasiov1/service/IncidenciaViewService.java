package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.dto.IncidenciaViewDTO;
import com.integradorii.gimnasiov1.model.Incidencia;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.nio.charset.StandardCharsets;

@Service
public class IncidenciaViewService {

    public IncidenciaViewDTO toView(Incidencia i) {
        String originalDesc = i.getDescripcion();
        String desc = originalDesc;
        byte[] imagenesBytes = i.getImagenes();
        String imagenes = null;
        if (imagenesBytes != null && imagenesBytes.length > 0) {
            try {
                imagenes = new String(imagenesBytes, StandardCharsets.UTF_8);
            } catch (Exception ignore) {}
        }
        String fallbackReportado = null;
        String fallbackAsignado = null;
        if (originalDesc != null) {
            String metaSegment = originalDesc;
            int ridx = metaSegment.indexOf("\n__REPORTADO__=");
            if (ridx >= 0) {
                int startIdx = ridx + "\n__REPORTADO__=".length();
                int endIdx = metaSegment.indexOf("\n", startIdx);
                if (endIdx < 0) endIdx = metaSegment.length();
                fallbackReportado = metaSegment.substring(startIdx, endIdx).trim();
            }
            int aidx = metaSegment.indexOf("\n__ASIGNADO__=");
            if (aidx >= 0) {
                int startIdx = aidx + "\n__ASIGNADO__=".length();
                int endIdx = metaSegment.indexOf("\n", startIdx);
                if (endIdx < 0) endIdx = metaSegment.length();
                fallbackAsignado = metaSegment.substring(startIdx, endIdx).trim();
            }
            int cut = -1;
            if (ridx >= 0 && aidx >= 0) cut = Math.min(ridx, aidx);
            else if (ridx >= 0) cut = ridx;
            else if (aidx >= 0) cut = aidx;
            if (cut >= 0) desc = metaSegment.substring(0, cut);
        }

        String prioridadUi = i.getPrioridad() == null ? null : i.getPrioridad().toUpperCase();
        String estadoUi = null;
        if ("Abierta".equalsIgnoreCase(i.getEstado())) estadoUi = "ABIERTO";
        else if ("Resuelto".equalsIgnoreCase(i.getEstado())) estadoUi = "RESUELTO";

        OffsetDateTime fr = i.getFechaReporte();
        if (fr != null) {
            try {
                fr = fr.atZoneSameInstant(ZoneId.systemDefault()).toOffsetDateTime();
            } catch (Exception ignore) {}
        }

        String nombreReportado = null;
        if (i.getReportadoPor() != null) {
            nombreReportado = i.getReportadoPor().getEmail();
        } else {
            nombreReportado = fallbackReportado != null ? fallbackReportado : "";
        }

        String nombreAsignado = null;
        if (i.getAsignadoA() != null) {
            nombreAsignado = (i.getAsignadoA().getNombre() == null ? "" : i.getAsignadoA().getNombre()) +
                    (i.getAsignadoA().getApellido() == null ? "" : (" " + i.getAsignadoA().getApellido())).trim();
        } else {
            nombreAsignado = fallbackAsignado != null ? fallbackAsignado : null;
        }

        return new IncidenciaViewDTO(
                i.getId(),
                i.getTitulo(),
                desc,
                i.getCategoria(),
                prioridadUi,
                estadoUi,
                fr,
                nombreReportado,
                nombreAsignado,
                imagenes
        );
    }
}
