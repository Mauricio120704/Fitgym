package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.NotificacionMasiva;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.NotificacionMasivaRepository;
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
    private final NotificacionMasivaRepository notificacionMasivaRepository;

    public NotificacionMasivaService(PersonaRepository personaRepository,
                                     EmailService emailService,
                                     NotificacionMasivaRepository notificacionMasivaRepository) {
        this.personaRepository = personaRepository;
        this.emailService = emailService;
        this.notificacionMasivaRepository = notificacionMasivaRepository;
    }

    @Transactional
    public int enviarNotificacionATodosDeportistas(String asunto, String mensaje, String filtroPlan) throws MessagingException {
        List<Persona> destinatarios = Collections.emptyList();

        String filtroPlanNormalizado = (filtroPlan == null || filtroPlan.isBlank()) ? "TODOS" : filtroPlan.trim();

        if (filtroPlanNormalizado.equalsIgnoreCase("TODOS")) {
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
                Integer planId = Integer.valueOf(filtroPlanNormalizado);
                destinatarios = personaRepository.findDeportistasActivosPorPlanId(planId);
            } catch (NumberFormatException ex) {
                destinatarios = Collections.emptyList();
            }
        }

        NotificacionMasiva registro = new NotificacionMasiva();
        registro.setAsunto(asunto);
        registro.setMensaje(mensaje);
        registro.setFiltroPlan(filtroPlanNormalizado);
        notificacionMasivaRepository.save(registro);

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
