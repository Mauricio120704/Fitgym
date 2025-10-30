package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.service.VerificationTokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmailVerificationController {
    
    private final VerificationTokenService verificationTokenService;
    
    public EmailVerificationController(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }
    
    /**
     * Endpoint para verificar el email mediante token
     */
    @GetMapping("/verificar-email")
    public String verificarEmail(@RequestParam String token, Model model) {
        String resultado = verificationTokenService.verificarToken(token);
        
        // Determinar si fue exitoso o hubo error
        boolean exito = resultado.contains("éxito");
        
        if (exito) {
            model.addAttribute("success", resultado);
        } else {
            model.addAttribute("error", resultado);
        }
        
        return "verificacion-resultado";
    }
    
    /**
     * Mostrar página para reenviar email de verificación
     */
    @GetMapping("/reenviar-verificacion")
    public String mostrarReenviarVerificacion() {
        return "reenviar-verificacion";
    }
    
    /**
     * Procesar reenvío de email de verificación
     */
    @PostMapping("/reenviar-verificacion")
    public String reenviarVerificacion(@RequestParam String email, Model model) {
        boolean enviado = verificationTokenService.reenviarEmailVerificacion(email);
        
        if (enviado) {
            model.addAttribute("success", "Se ha enviado un nuevo correo de verificación a " + email);
        } else {
            model.addAttribute("error", "No se encontró una cuenta pendiente de verificación con ese email");
        }
        
        return "reenviar-verificacion";
    }
}
