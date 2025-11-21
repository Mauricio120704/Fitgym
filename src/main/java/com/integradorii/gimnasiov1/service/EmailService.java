package com.integradorii.gimnasiov1.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Envía un correo de verificación con el token
     * @param toEmail Email del destinatario
     * @param nombreCompleto Nombre completo del usuario
     * @param token Token de verificación UUID
     * @throws MessagingException Si hay error al enviar el correo
     */
    public void enviarEmailVerificacion(String toEmail, String nombreCompleto, String token) 
            throws MessagingException {
        
        String verificationUrl = baseUrl + "/verificar-email?token=" + token;
        
        String htmlContent = construirEmailVerificacion(nombreCompleto, verificationUrl);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(Objects.requireNonNull(fromEmail));
        helper.setTo(Objects.requireNonNull(toEmail));
        helper.setSubject("Verifica tu cuenta - FitGym");
        helper.setText(Objects.requireNonNull(htmlContent), true);
        
        mailSender.send(message);
    }
    
    public void enviarNotificacionGeneral(String toEmail, String nombreCompleto, String asunto, String mensaje)
            throws MessagingException {
        String htmlContent = construirEmailNotificacion(nombreCompleto, mensaje);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String safeFrom = fromEmail != null ? fromEmail.replaceAll("[\\r\\n]", "").trim() : null;
        String safeTo = toEmail != null ? toEmail.replaceAll("[\\r\\n]", "").trim() : null;
        String safeSubject = asunto != null ? asunto.replaceAll("[\\r\\n]", " ").trim() : null;

        helper.setFrom(Objects.requireNonNull(safeFrom));
        helper.setTo(Objects.requireNonNull(safeTo));
        helper.setSubject(Objects.requireNonNull(safeSubject));
        helper.setText(Objects.requireNonNull(htmlContent), true);

        mailSender.send(message);
    }

    /**
     * Construye el contenido HTML del email de verificación
     */
    private String construirEmailVerificacion(String nombreCompleto, String verificationUrl) {
        return "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Verificación de Email</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 20px auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                "        .header { text-align: center; padding-bottom: 20px; border-bottom: 3px solid #4CAF50; }" +
                "        .header h1 { color: #4CAF50; margin: 0; font-size: 28px; }" +
                "        .content { padding: 20px 0; }" +
                "        .button { display: inline-block; padding: 15px 30px; background-color: #4CAF50; color: white !important; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }" +
                "        .button:hover { background-color: #45a049; }" +
                "        .footer { text-align: center; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }" +
                "        .warning { background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🏋️ FitGym</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2>¡Bienvenido/a, " + nombreCompleto + "!</h2>" +
                "            <p>Gracias por registrarte en FitGym. Para activar tu cuenta y comenzar a disfrutar de nuestros servicios, necesitamos verificar tu dirección de correo electrónico.</p>" +
                "            <p>Haz clic en el botón de abajo para verificar tu cuenta:</p>" +
                "            <div style='text-align: center;'>" +
                "                <a href='" + verificationUrl + "' class='button'>Verificar mi cuenta</a>" +
                "            </div>" +
                "            <div class='warning'>" +
                "                <strong>⚠️ Importante:</strong> Este enlace expirará en 24 horas." +
                "            </div>" +
                "            <p>Si no puedes hacer clic en el botón, copia y pega el siguiente enlace en tu navegador:</p>" +
                "            <p style='word-break: break-all; color: #666; font-size: 12px;'>" + verificationUrl + "</p>" +
                "            <p>Si no creaste esta cuenta, puedes ignorar este mensaje.</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2024 FitGym. Todos los derechos reservados.</p>" +
                "            <p>Este es un correo automático, por favor no respondas a este mensaje.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String construirEmailNotificacion(String nombreCompleto, String mensaje) {
        return "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Notificación FitGym</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 20px auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                "        .header { text-align: center; padding-bottom: 20px; border-bottom: 3px solid #f97316; }" +
                "        .header h1 { color: #f97316; margin: 0; font-size: 26px; }" +
                "        .content { padding: 20px 0; }" +
                "        .footer { text-align: center; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }" +
                "        .message-box { background-color: #f9fafb; border-radius: 8px; padding: 16px; border: 1px solid #e5e7eb; white-space: pre-line; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>FitGym</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2>Hola, " + nombreCompleto + "</h2>" +
                "            <p>Tenemos un anuncio importante para ti:</p>" +
                "            <div class='message-box'>" + mensaje + "</div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2024 FitGym. Este es un mensaje informativo enviado a los deportistas del gimnasio.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Envía un correo con el código de recuperación de contraseña
     * @param toEmail Email del destinatario
     * @param nombreCompleto Nombre completo del usuario
     * @param codigo Código de verificación de 6 dígitos
     * @throws MessagingException Si hay error al enviar el correo
     */
    public void enviarCodigoRecuperacion(String toEmail, String nombreCompleto, String codigo) 
            throws MessagingException {
        
        String htmlContent = construirEmailRecuperacion(nombreCompleto, codigo);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(Objects.requireNonNull(fromEmail));
        helper.setTo(Objects.requireNonNull(toEmail));
        helper.setSubject("Recuperación de Contraseña - FitGym");
        helper.setText(Objects.requireNonNull(htmlContent), true);
        
        mailSender.send(message);
    }
    
    /**
     * Construye el contenido HTML del email de recuperación de contraseña
     */
    private String construirEmailRecuperacion(String nombreCompleto, String codigo) {
        return "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Recuperación de Contraseña</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 20px auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                "        .header { text-align: center; padding-bottom: 20px; border-bottom: 3px solid #2196F3; }" +
                "        .header h1 { color: #2196F3; margin: 0; font-size: 28px; }" +
                "        .content { padding: 20px 0; }" +
                "        .code-box { background-color: #f8f9fa; border: 2px dashed #2196F3; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center; }" +
                "        .code { font-size: 32px; font-weight: bold; color: #2196F3; letter-spacing: 8px; font-family: 'Courier New', monospace; }" +
                "        .warning { background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; }" +
                "        .footer { text-align: center; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }" +
                "        .alert { background-color: #f8d7da; padding: 15px; border-left: 4px solid #dc3545; margin: 20px 0; color: #721c24; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🔐 FitGym</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2>Recuperación de Contraseña</h2>" +
                "            <p>Hola, " + nombreCompleto + "</p>" +
                "            <p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en FitGym.</p>" +
                "            <p>Utiliza el siguiente código de seguridad para continuar con el proceso:</p>" +
                "            <div class='code-box'>" +
                "                <div class='code'>" + codigo + "</div>" +
                "                <p style='margin: 10px 0 0 0; font-size: 14px; color: #666;'>Código de verificación</p>" +
                "            </div>" +
                "            <div class='warning'>" +
                "                <strong>⏰ Importante:</strong> Este código expirará en 15 minutos por seguridad." +
                "            </div>" +
                "            <div class='alert'>" +
                "                <strong>⚠️ Seguridad:</strong> Si no solicitaste este cambio, ignora este correo. Tu contraseña actual permanecerá segura." +
                "            </div>" +
                "            <p style='margin-top: 20px;'><strong>Instrucciones:</strong></p>" +
                "            <ol>" +
                "                <li>Ingresa el código de 6 dígitos en la página de recuperación</li>" +
                "                <li>Establece tu nueva contraseña</li>" +
                "                <li>Inicia sesión con tus nuevas credenciales</li>" +
                "            </ol>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2024 FitGym. Todos los derechos reservados.</p>" +
                "            <p>Este es un correo automático, por favor no respondas a este mensaje.</p>" +
                "            <p style='margin-top: 10px; font-size: 10px;'>Este código es confidencial. No lo compartas con nadie.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
