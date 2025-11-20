package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NotificacionMasivaService {

    private final PersonaRepository personaRepository;
    private final EmailService emailService;

    public NotificacionMasivaService(PersonaRepository personaRepository, EmailService emailService) {
        this.personaRepository = personaRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public int enviarNotificacionATodosDeportistas(String asunto, String mensaje, String filtroPlan) throws MessagingException {
        List<Persona> destinatarios = Collections.emptyList();

        if (filtroPlan == null || filtroPlan.isBlank() || "TODOS".equalsIgnoreCase(filtroPlan)) {
            List<Persona> posibles = personaRepository.findByEmailVerificadoTrueAndActivoTrue();
            List<Persona> filtrados = new ArrayList<>();
            for (Persona persona : posibles) {
                if (Boolean.TRUE.equals(persona.getMembresiaActiva())) {
                    filtrados.add(persona);
                }
            }
            destinatarios = filtrados;
        } else {
            try {
                Integer planId = Integer.valueOf(filtroPlan.trim());
                destinatarios = personaRepository.findDeportistasActivosPorPlanId(planId);
            } catch (NumberFormatException ex) {
                destinatarios = Collections.emptyList();
            }
        }

        int enviados = 0;
        for (Persona persona : destinatarios) {
            if (persona.getEmail() == null || persona.getEmail().isBlank()) {
                continue;
            }
            String rawEmail = persona.getEmail();
            String sanitizedEmail = rawEmail.replaceAll("[\\r\\n]", "").trim();
            if (sanitizedEmail.isEmpty() || !sanitizedEmail.contains("@")) {
                continue;
            }
            try {
                emailService.enviarNotificacionGeneral(
                        sanitizedEmail,
                        persona.getNombreCompleto(),
                        asunto,
                        mensaje
                );
                enviados++;
            } catch (MessagingException | IllegalArgumentException ex) {
                continue;
            }
        }
        return enviados;
    }
}
