package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.service.PasswordRecoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * CONTROLADOR DE RECUPERACIÓN DE CONTRASEÑA
 * 
 * Propósito: Gestionar el proceso completo de recuperación de contraseña para todos los usuarios
 * Ruta base: /password-recovery
 * Acceso: PÚBLICO (no requiere autenticación)
 * Vistas asociadas: recuperacion.html, nueva-contrasena.html
 * 
 * Relación con tablas: password_reset_tokens, personas (deportistas), usuarios (personal)
 * 
 * Flujo completo:
 * 1. Usuario ingresa email → POST /solicitar-codigo
 * 2. Sistema envía código de 6 dígitos por email
 * 3. Usuario ingresa código → POST /verificar-codigo
 * 4. Usuario establece nueva contraseña → POST /restablecer
 * 
 * Seguridad:
 * - Códigos de 6 dígitos aleatorios
 * - Expiración de 15 minutos
 * - Un solo uso por código
 * - Encriptación BCrypt de contraseñas
 */
@Controller
@RequestMapping("/password-recovery")
public class PasswordRecoveryController {

    // Logger para debugging y seguimiento del proceso de recuperación
    private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryController.class);
    
    // Servicio que contiene la lógica de negocio de recuperación
    private final PasswordRecoveryService passwordRecoveryService;

    public PasswordRecoveryController(PasswordRecoveryService passwordRecoveryService) {
        this.passwordRecoveryService = passwordRecoveryService;
    }

    /**
     * Muestra la vista principal de recuperación de contraseña
     * 
     * Ruta: GET /password-recovery
     * Acceso: PÚBLICO (cualquier usuario puede acceder)
     * Vista: recuperacion.html
     * 
     * @return Vista con formulario para ingresar email
     */
    @GetMapping
    public String mostrarFormularioRecuperacion() {
        return "recuperacion";
    }

    /**
     * Muestra la vista para establecer nueva contraseña
     * 
     * Ruta: GET /password-recovery/nueva-contrasena?email=xxx&codigo=yyy
     * Acceso: PÚBLICO (pero requiere parámetros válidos)
     * Vista: nueva-contrasena.html
     * 
     * Solo se accede después de verificar código correctamente
     * 
     * @param email Email del usuario (pasado desde verificación de código)
     * @param codigo Código verificado (pasado desde verificación)
     * @return Vista para establecer nueva contraseña, o redirección si faltan parámetros
     */
    @GetMapping("/nueva-contrasena")
    public String mostrarFormularioNuevaContrasena(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String codigo) {
        // Redireccionar a inicio si no hay email o código (acceso directo no permitido)
        if (email == null || codigo == null) {
            return "redirect:/password-recovery";
        }
        return "nueva-contrasena";
    }

    /**
     * API REST: Solicitar código de recuperación por email
     * 
     * Ruta: POST /password-recovery/solicitar-codigo
     * Acceso: PÚBLICO
     * Formato: application/x-www-form-urlencoded
     * Respuesta: JSON
     * 
     * Flujo:
     * 1. Valida que el email no esté vacío
     * 2. Busca el email en tablas 'personas' (deportistas) y 'usuarios' (personal)
     * 3. Genera código aleatorio de 6 dígitos
     * 4. Guarda token en BD con expiración de 15 minutos
     * 5. Envía código por email usando Gmail SMTP
     * 
     * @param email Email del usuario que solicita recuperación
     * @return JSON con {success: boolean, message: string}
     *         - 200 OK: Código enviado exitosamente
     *         - 404 NOT_FOUND: Email no registrado
     *         - 400 BAD_REQUEST: Email vacío
     *         - 500 ERROR: Error al enviar email
     */
    @PostMapping("/solicitar-codigo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> solicitarCodigo(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Solicitud de recuperación de contraseña para email: {}", email);
            
            // Validar que el email no esté vacío
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Email vacío en solicitud de recuperación");
                response.put("success", false);
                response.put("message", "El correo electrónico es requerido");
                return ResponseEntity.badRequest().body(response);
            }

            // Generar y enviar el código
            boolean enviado = passwordRecoveryService.generarYEnviarCodigo(email.trim());

            if (enviado) {
                logger.info("Código de recuperación procesado para email existente: {}", email);
            } else {
                logger.warn("Solicitud de recuperación para email no encontrado en el sistema: {}", email);
            }

            // Respuesta genérica para evitar revelar si el email existe o no
            response.put("success", true);
            response.put("message", "Se ha enviado un correo con las instrucciones para restablecer tu contraseña. " +
                    "Por favor revisa tu bandeja de entrada y la carpeta de spam.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al procesar solicitud de recuperación para email: {}", email, e);
            response.put("success", false);
            response.put("message", "Error al enviar el código: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API REST: Verificar código de seguridad
     * 
     * Ruta: POST /password-recovery/verificar-codigo
     * Acceso: PÚBLICO
     * Formato: application/x-www-form-urlencoded
     * Respuesta: JSON
     * 
     * Flujo:
     * 1. Valida que email y código no estén vacíos
     * 2. Busca token en BD que coincida con email y código
     * 3. Verifica que no esté expirado (15 minutos)
     * 4. Verifica que no haya sido usado previamente
     * 
     * Si válido, el frontend redirige a /nueva-contrasena
     * 
     * @param email Email del usuario
     * @param codigo Código de 6 dígitos recibido por email
     * @return JSON con {success: boolean, message: string}
     *         - 200 OK: Código válido
     *         - 401 UNAUTHORIZED: Código inválido o expirado
     *         - 400 BAD_REQUEST: Parámetros vacíos
     *         - 500 ERROR: Error interno
     */
    @PostMapping("/verificar-codigo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarCodigo(
            @RequestParam String email,
            @RequestParam String codigo) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar parámetros
            if (email == null || email.trim().isEmpty() || 
                codigo == null || codigo.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email y código son requeridos");
                return ResponseEntity.badRequest().body(response);
            }

            // Verificar el código
            boolean valido = passwordRecoveryService.verificarCodigo(email.trim(), codigo.trim());

            if (valido) {
                response.put("success", true);
                response.put("message", "Código verificado correctamente");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "El código es inválido o ha expirado. Por favor solicita uno nuevo");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar el código. Por favor intenta de nuevo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API REST: Restablecer contraseña con código verificado
     * 
     * Ruta: POST /password-recovery/restablecer
     * Acceso: PÚBLICO
     * Formato: application/x-www-form-urlencoded
     * Respuesta: JSON
     * 
     * Flujo:
     * 1. Valida que todos los campos estén presentes
     * 2. Valida que las contraseñas coincidan
     * 3. Valida longitud mínima (6 caracteres)
     * 4. Verifica que el código sea válido y no expirado
     * 5. Actualiza contraseña en la tabla correcta (personas o usuarios)
     * 6. Encripta con BCrypt antes de guardar
     * 7. Marca el token como usado e invalida otros tokens del usuario
     * 
     * Después de éxito, usuario puede iniciar sesión con nueva contraseña
     * 
     * @param email Email del usuario
     * @param codigo Código de verificación de 6 dígitos
     * @param nuevaContrasena Nueva contraseña (mínimo 6 caracteres)
     * @param confirmarContrasena Confirmación de nueva contraseña
     * @return JSON con {success: boolean, message: string}
     *         - 200 OK: Contraseña restablecida exitosamente
     *         - 401 UNAUTHORIZED: Código inválido o expirado
     *         - 400 BAD_REQUEST: Validaciones fallidas
     *         - 500 ERROR: Error al actualizar contraseña
     */
    @PostMapping("/restablecer")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> restablecerContrasena(
            @RequestParam String email,
            @RequestParam String codigo,
            @RequestParam String nuevaContrasena,
            @RequestParam String confirmarContrasena) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar parámetros
            if (email == null || email.trim().isEmpty() ||
                codigo == null || codigo.trim().isEmpty() ||
                nuevaContrasena == null || nuevaContrasena.isEmpty() ||
                confirmarContrasena == null || confirmarContrasena.isEmpty()) {
                response.put("success", false);
                response.put("message", "Todos los campos son requeridos");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar que las contraseñas coincidan
            if (!nuevaContrasena.equals(confirmarContrasena)) {
                response.put("success", false);
                response.put("message", "Las contraseñas no coinciden");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar longitud mínima de contraseña
            if (nuevaContrasena.length() < 6) {
                response.put("success", false);
                response.put("message", "La contraseña debe tener al menos 6 caracteres");
                return ResponseEntity.badRequest().body(response);
            }

            // Restablecer la contraseña
            boolean restablecida = passwordRecoveryService.restablecerContrasena(
                    email.trim(), 
                    codigo.trim(), 
                    nuevaContrasena);

            if (restablecida) {
                response.put("success", true);
                response.put("message", "Tu contraseña ha sido restablecida correctamente. " +
                        "Ahora puedes iniciar sesión con tu nueva contraseña");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "El código es inválido o ha expirado. Por favor solicita uno nuevo");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al restablecer la contraseña. Por favor intenta de nuevo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
