package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.PasswordResetToken;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.PasswordResetTokenRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordRecoveryService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryService.class);
    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    // Duración de validez del token: 15 minutos
    private static final int TOKEN_EXPIRATION_MINUTES = 15;

    public PasswordRecoveryService(
            PasswordResetTokenRepository tokenRepository,
            UsuarioRepository usuarioRepository,
            PersonaRepository personaRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Genera y envía un código de recuperación al email proporcionado
     * Busca primero en la tabla personas (deportistas) y luego en usuarios (personal)
     * @param email Email del usuario
     * @return true si se envió correctamente, false si el email no existe
     */
    @Transactional
    public boolean generarYEnviarCodigo(String email) {
        logger.info("Iniciando generación de código para email: {}", email);
        String nombreCompleto = null;
        
        // Buscar primero en la tabla de personas (deportistas)
        Optional<Persona> personaOpt = personaRepository.findByEmail(email);
        if (personaOpt.isPresent()) {
            Persona persona = personaOpt.get();
            nombreCompleto = persona.getNombre() + " " + persona.getApellido();
            logger.info("Usuario encontrado en tabla 'personas': {} ({})", nombreCompleto, email);
        } else {
            // Si no existe en personas, buscar en usuarios (personal administrativo)
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                nombreCompleto = usuario.getNombreCompleto();
                logger.info("Usuario encontrado en tabla 'usuarios': {} ({})", nombreCompleto, email);
            } else {
                // El email no existe en ninguna de las dos tablas
                logger.warn("Email no encontrado en ninguna tabla: {}", email);
                return false;
            }
        }
        
        // Invalidar todos los tokens previos de este email
        try {
            logger.debug("Invalidando tokens previos para: {}", email);
            tokenRepository.invalidateAllTokensByEmail(email);
            logger.debug("Tokens previos invalidados exitosamente");
        } catch (Exception e) {
            logger.warn("No se pudieron invalidar tokens previos (puede ser primera vez): {}", e.getMessage());
            // No es crítico si falla, continuar
        }

        // Generar código de 6 dígitos
        String codigo = generarCodigoSeguro();
        logger.debug("Código generado: {} para {}", codigo, email);

        // Crear el token
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusMinutes(TOKEN_EXPIRATION_MINUTES);
        
        PasswordResetToken token = new PasswordResetToken(email, codigo, ahora, expiracion);
        tokenRepository.save(token);
        logger.info("Token guardado en BD para: {}", email);

        // Enviar el código por correo
        try {
            logger.info("Intentando enviar email a: {}", email);
            emailService.enviarCodigoRecuperacion(email, nombreCompleto, codigo);
            logger.info("Email enviado exitosamente a: {}", email);
            return true;
        } catch (MessagingException e) {
            logger.error("Error al enviar email a: {}", email, e);
            // Si falla el envío del correo, eliminar el token
            tokenRepository.delete(token);
            logger.warn("Token eliminado debido a fallo en envío de email");
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error inesperado al enviar email a: {}", email, e);
            tokenRepository.delete(token);
            throw new RuntimeException("Error inesperado: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si un código es válido para un email
     * @param email Email del usuario
     * @param codigo Código de 6 dígitos
     * @return true si el código es válido, false en caso contrario
     */
    public boolean verificarCodigo(String email, String codigo) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByEmailAndToken(email, codigo);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOpt.get();
        return token.isValido();
    }

    /**
     * Restablece la contraseña del usuario
     * Busca en personas (deportistas) y usuarios (personal) para actualizar la contraseña correcta
     * @param email Email del usuario
     * @param codigo Código de verificación
     * @param nuevaContrasena Nueva contraseña
     * @return true si se actualizó correctamente, false en caso contrario
     */
    @Transactional
    public boolean restablecerContrasena(String email, String codigo, String nuevaContrasena) {
        // Verificar el código
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByEmailAndToken(email, codigo);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOpt.get();
        
        // Verificar que el token sea válido
        if (!token.isValido()) {
            return false;
        }

        // Buscar en personas (deportistas) primero
        Optional<Persona> personaOpt = personaRepository.findByEmail(email);
        if (personaOpt.isPresent()) {
            Persona persona = personaOpt.get();
            // Actualizar la contraseña (encriptada)
            persona.setContraseña(passwordEncoder.encode(nuevaContrasena));
            personaRepository.save(persona);
        } else {
            // Si no está en personas, buscar en usuarios (personal)
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                // Actualizar la contraseña (encriptada)
                usuario.setContraseña(passwordEncoder.encode(nuevaContrasena));
                usuarioRepository.save(usuario);
            } else {
                // El email no existe en ninguna tabla
                return false;
            }
        }

        // Marcar el token como usado
        token.marcarComoUsado();
        tokenRepository.save(token);

        // Invalidar todos los demás tokens del usuario
        tokenRepository.invalidateAllTokensByEmail(email);

        return true;
    }

    /**
     * Genera un código aleatorio de 6 dígitos
     */
    private String generarCodigoSeguro() {
        int codigo = secureRandom.nextInt(900000) + 100000; // Entre 100000 y 999999
        return String.valueOf(codigo);
    }

    /**
     * Limpia los tokens expirados de la base de datos
     * Este método puede ser llamado por un scheduler periódicamente
     */
    @Transactional
    public void limpiarTokensExpirados() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
