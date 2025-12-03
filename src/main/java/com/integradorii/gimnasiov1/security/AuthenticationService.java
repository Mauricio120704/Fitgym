package com.integradorii.gimnasiov1.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

/**
 * Servicio auxiliar para realizar un "auto login" programático.
 *
 * Contexto de uso típico:
 *  - Después de ciertas operaciones (por ejemplo, registro de usuario,
 *    cambio de contraseña, etc.) se puede querer autenticar al usuario
 *    automáticamente sin que vuelva a enviar el formulario de login.
 *
 * Este servicio encapsula el proceso estándar de Spring Security:
 *  1. Crear un {@link UsernamePasswordAuthenticationToken} con email y password.
 *  2. Delegar la autenticación en el {@link AuthenticationManager} configurado
 *     (que usa {@code CustomUserDetailsService} y el {@code PasswordEncoder}).
 *  3. Guardar el {@link Authentication} resultante en el {@link SecurityContext}.
 *  4. Persistir el contexto en el {@link SecurityContextRepository}
 *     (normalmente en la HttpSession).
 */
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Realiza un login automático del usuario indicado por su email y contraseña.
     *
     * Flujo detallado:
     *  - Construye un token de autenticación con las credenciales recibidas.
     *  - Llama a {@code authenticationManager.authenticate(...)} para validar
     *    las credenciales contra la base de datos.
     *  - Si la autenticación es exitosa, crea un nuevo {@link SecurityContext},
     *    establece allí el {@link Authentication} y actualiza el
     *    {@link SecurityContextHolder} global.
     *  - Finalmente, persiste el contexto en el {@link SecurityContextRepository}
     *    para que quede asociado a la sesión HTTP del usuario.
     *
     * @param email    email (username) del usuario
     * @param password contraseña en texto plano que se validará contra el hash
     * @param request  petición HTTP actual (necesaria para asociar la sesión)
     * @param response respuesta HTTP donde se puede modificar la cookie de sesión
     */
    public void autoLogin(String email, String password, HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(email, password);
        
        Authentication authentication = authenticationManager.authenticate(authToken);
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        
        securityContextRepository.saveContext(context, request, response);
    }
}
