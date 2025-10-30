package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.VerificationToken;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerificationTokenService {
    
    private final VerificationTokenRepository tokenRepository;
    private final PersonaRepository personaRepository;
    private final EmailService emailService;
    
    public VerificationTokenService(VerificationTokenRepository tokenRepository,
                                   PersonaRepository personaRepository,
                                   EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.personaRepository = personaRepository;
        this.emailService = emailService;
    }
    
    /**
     * Crea un token de verificación para una persona y envía el email
     * @param persona Persona que necesita verificación
     * @return Token creado
     */
    @Transactional
    public VerificationToken crearTokenYEnviarEmail(Persona persona) {
        // Eliminar tokens anteriores si existen
        tokenRepository.findByPersona(persona).ifPresent(tokenRepository::delete);
        
        // Crear nuevo token
        VerificationToken token = new VerificationToken(persona);
        VerificationToken tokenGuardado = tokenRepository.save(token);
        
        // Enviar email de verificación
        try {
            emailService.enviarEmailVerificacion(
                persona.getEmail(),
                persona.getNombreCompleto(),
                tokenGuardado.getToken()
            );
        } catch (Exception e) {
            // Log error pero no fallar el registro
            System.err.println("Error al enviar email de verificación: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tokenGuardado;
    }
    
    /**
     * Verifica un token y activa la cuenta si es válido
     * @param tokenString Token UUID a verificar
     * @return Mensaje de resultado
     */
    @Transactional
    public String verificarToken(String tokenString) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(tokenString);
        
        if (optionalToken.isEmpty()) {
            return "Token inválido o no encontrado";
        }
        
        VerificationToken token = optionalToken.get();
        
        if (token.getUsado()) {
            return "Este token ya fue utilizado";
        }
        
        if (token.isExpirado()) {
            return "Este token ha expirado. Por favor, solicita un nuevo enlace de verificación";
        }
        
        // Activar la cuenta
        Persona persona = token.getPersona();
        persona.setActivo(Boolean.TRUE);
        persona.setEmailVerificado(Boolean.TRUE);
        personaRepository.save(persona);
        
        // Marcar token como usado
        token.setUsado(Boolean.TRUE);
        tokenRepository.save(token);
        
        return "¡Cuenta verificada con éxito! Ya puedes iniciar sesión";
    }
    
    /**
     * Reenvía el email de verificación
     * @param email Email del usuario
     * @return true si se envió correctamente
     */
    @Transactional
    public boolean reenviarEmailVerificacion(String email) {
        Optional<Persona> optionalPersona = personaRepository.findByEmail(email);
        
        if (optionalPersona.isEmpty()) {
            return false;
        }
        
        Persona persona = optionalPersona.get();
        
        // Si ya está verificado, no reenviar
        if (Boolean.TRUE.equals(persona.getEmailVerificado())) {
            return false;
        }
        
        crearTokenYEnviarEmail(persona);
        return true;
    }
}
