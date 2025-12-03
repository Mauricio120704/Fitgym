package com.integradorii.gimnasiov1.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de ayuda para trabajar con el {@link PasswordEncoder} de Spring Security.
 *
 * En lugar de inyectar directamente el {@code PasswordEncoder} en todos los
 * controladores y servicios, se centraliza aquí la lógica básica de:
 *  - Encriptar contraseñas en texto plano antes de guardarlas en la base de datos.
 *  - Verificar si una contraseña en texto plano coincide con el hash almacenado.
 *
 * El bean concreto de {@link PasswordEncoder} se define en {@code SecurityConfig}
 * (actualmente BCryptPasswordEncoder), por lo que este servicio hereda esas
 * características de seguridad (salt aleatorio, algoritmo lento, etc.).
 */
@Service
public class PasswordEncoderService {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordEncoderService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
