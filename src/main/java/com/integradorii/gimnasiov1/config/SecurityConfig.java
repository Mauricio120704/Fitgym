package com.integradorii.gimnasiov1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * CONFIGURACIÓN DE SEGURIDAD DEL SISTEMA - SPRING SECURITY
 * 
 * Este archivo es CRÍTICO para el funcionamiento del sistema.
 * Define quién puede acceder a qué recursos y cómo se autentica.
 * 
 * ROLES DEL SISTEMA:
 * ==================
 * 1. ROLE_CLIENTE → Deportistas/Clientes del gimnasio
 *    - Tabla BD: personas
 *    - Login: /login
 *    - Redirect: /perfil
 * 
 * 2. ROLE_ADMINISTRADOR → Administradores del gimnasio
 *    - Tabla BD: usuarios
 *    - Login: /login
 *    - Redirect: /miembros
 *    - Acceso total al sistema
 * 
 * 3. ROLE_RECEPCIONISTA → Personal de recepción
 *    - Tabla BD: usuarios
 *    - Login: /login
 *    - Redirect: /miembros
 *    - Acceso a: asistencia, lockers, monitoreo, gestión general
 * 
 * 4. ROLE_ENTRENADOR → Entrenadores personales
 *    - Tabla BD: usuarios
 *    - Login: /login
 *    - Redirect: /miembros
 *    - Acceso a: gestión de clientes, entrenamientos
 * 
 * AUTENTICACIÓN:
 * ==============
 * - Username: email del usuario
 * - Password: contraseña encriptada con BCrypt
 * - Almacenamiento: HttpSession
 * 
 * IMPORTANTE: 
 * - Los deportistas se autentican desde tabla 'personas'
 * - El personal se autentica desde tabla 'usuarios'
 * - Ambos usan el mismo endpoint /login pero CustomUserDetailsService los diferencia
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Bean de encriptación de contraseñas
     * 
     * Algoritmo: BCrypt con salt automático
     * Uso: Encriptar contraseñas al registrar/actualizar usuarios
     * 
     * BCrypt es seguro porque:
     * - Genera un salt aleatorio para cada contraseña
     * - Es computacionalmente costoso (previene fuerza bruta)
     * - Es unidireccional (no se puede desencriptar)
     * 
     * @return Instancia de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean del gestor de autenticación
     * 
     * Responsabilidad: Validar credenciales de usuarios
     * Uso: Inyectado en CustomAuthenticationService para login manual
     * 
     * Proceso:
     * 1. Recibe email y contraseña
     * 2. CustomUserDetailsService carga usuario de BD
     * 3. Compara contraseñas con BCrypt
     * 4. Retorna Authentication si es válido
     * 
     * @param authConfig Configuración de autenticación de Spring
     * @return AuthenticationManager configurado
     * @throws Exception si hay error en configuración
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Bean del repositorio de contexto de seguridad
     * 
     * Responsabilidad: Almacenar la sesión del usuario autenticado
     * Tipo: HttpSession (almacenamiento en servidor)
     * 
     * Almacena:
     * - Usuario autenticado
     * - Roles y permisos
     * - Tiempo de sesión
     * 
     * Alternativas no usadas:
     * - Cookies
     * - JWT tokens
     * - Redis
     * 
     * @return Repositorio basado en HttpSession
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * CADENA DE FILTROS DE SEGURIDAD - CONFIGURACIÓN PRINCIPAL
     * 
     * Este método define las reglas de acceso a TODAS las rutas del sistema.
     * Es el corazón de la seguridad de la aplicación.
     * 
     * Estructura:
     * 1. authorizeHttpRequests - Define quién puede acceder a qué
     * 2. formLogin - Configura el proceso de login
     * 3. logout - Configura el proceso de logout
     * 4. csrf - Protección contra Cross-Site Request Forgery
     * 
     * IMPORTANTE: El orden de las reglas importa. La primera que coincida se aplica.
     * 
     * @param http Configurador de seguridad HTTP
     * @return Cadena de filtros configurada
     * @throws Exception si hay error en configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ===== RUTAS PÚBLICAS (Acceso sin autenticación) =====
                // Páginas principales, login, registro, recuperación de contraseña
                // Recursos estáticos (CSS, JavaScript, imágenes)
                .requestMatchers("/", "/inicio", "/login", "/registro", "/recuperacion", "/password-recovery/**",
                                "/planes", "/checkout/**", "/verificar-email", "/reenviar-verificacion",
                                "/Estilos/**", "/JavaScript/**", "/images/**", "/css/**", "/js/**",
                                "/*.js", "/*.css", "/dashboard.js", "/sidebar.js").permitAll()
                
                // ===== RUTAS PARA DEPORTISTAS/CLIENTES (ROLE_CLIENTE) =====
                // - /perfil: Ver/editar perfil personal
                // - /cliente/**: Todas las rutas de cliente
                // - /entrenamientos: Ver entrenamientos asignados
                // - /evaluaciones: Ver evaluaciones físicas
                // - /reclamos: Crear y ver reclamos
                // - /reservas: Reservar clases
                .requestMatchers("/perfil", "/cliente/**", "/entrenamientos", "/evaluaciones", "/reclamos", "/reservas", "/suspension", "/api/reservas/**", "/deportista/comunidad", "/api/comunidad/**").hasRole("CLIENTE")
                
                // ===== RUTAS PARA TODO EL PERSONAL (ADMIN, RECEPCIONISTA, ENTRENADOR) =====
                // - /miembros/**: Gestión de miembros
                // - /incidencias/**: Gestión de incidencias
                // - /pagos: Historial de pagos (vista admin)
                // - /clases/**: Gestión de clases
                // - /configuracion: Configuración del sistema
                // - /admin/invitados/**: Gestión de invitados
                // - /admin/visitantes/**: Gestión de visitantes
                // - /api/dashboard/**: API del dashboard de asistencia
                .requestMatchers("/miembros/**", "/incidencias/**", "/pagos", "/clases/**", "/promociones/**", "/configuracion", "/admin/invitados/**", "/admin/visitantes/**", "/api/dashboard/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "ENTRENADOR")
                
                // ===== RUTAS PARA RECEPCIONISTAS, ADMINISTRADORES Y ENTRENADORES =====
                // - /asistencia: Control de asistencia
                .requestMatchers("/asistencia", "/asistencia/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "ENTRENADOR")

                // ===== RUTAS PARA RECEPCIONISTAS Y ADMINISTRADORES =====
                // - /lockers: Gestión de lockers
                // - /monitoreo: Monitoreo de capacidad del gimnasio
                .requestMatchers("/lockers", "/lockers/**", "/monitoreo", "/monitoreo/**", "/api/lockers/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                
                // ===== RUTAS EXCLUSIVAS PARA ENTRENADORES =====
                // - /entrenador/**: Panel y funciones específicas de entrenadores
                .requestMatchers("/entrenador/**").hasRole("ENTRENADOR")
                
                // ===== CUALQUIER OTRA RUTA REQUIERE AUTENTICACIÓN =====
                // Si no coincide con ninguna regla anterior, debe estar autenticado
                .anyRequest().authenticated()
            )
            // ===== CONFIGURACIÓN DE LOGIN =====
            .formLogin(form -> form
                .loginPage("/login")                    // Página personalizada de login
                .loginProcessingUrl("/login")           // URL que procesa el formulario
                .usernameParameter("email")             // Nombre del campo email en el form
                .passwordParameter("password")          // Nombre del campo password en el form
                .successHandler((request, response, authentication) -> {
                    // ===== REDIRECCIÓN SEGÚN ROL =====
                    // Determina a dónde redirigir después de login exitoso
                    String redirectUrl = "/inicio";      // Por defecto
                    
                    // Si es deportista/cliente → redirigir a su perfil
                    if (authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                        redirectUrl = "/perfil";
                    } 
                    // Si es entrenador → redirigir a clases
                    else if (authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ENTRENADOR"))) {
                        redirectUrl = "/clases";
                    }
                    // Si es recepcionista → redirigir a monitoreo
                    else if (authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPCIONISTA"))) {
                        redirectUrl = "/monitoreo";
                    }
                    // Si es administrador → redirigir a miembros
                    else if (authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"))) {
                        redirectUrl = "/miembros";
                    }
                    response.sendRedirect(redirectUrl);
                })
                .failureUrl("/login?error=true")        // Redirigir si login falla
                .permitAll()                            // Todos pueden acceder a /login
            )
            // ===== CONFIGURACIÓN DE LOGOUT =====
            .logout(logout -> logout
                .logoutUrl("/logout")                    // URL para cerrar sesión
                .logoutSuccessUrl("/login?logout=true")  // Redirigir después de logout
                .invalidateHttpSession(true)             // Destruir sesión HTTP
                .deleteCookies("JSESSIONID")             // Eliminar cookie de sesión
                .permitAll()                             // Todos pueden hacer logout
            )
            // ===== CONFIGURACIÓN CSRF =====
            // IMPORTANTE: CSRF está desactivado para facilitar desarrollo y pruebas
            // En producción, considerar activarlo para prevenir ataques CSRF
            // Si se activa, todos los formularios POST necesitarán token CSRF
            .csrf(csrf -> csrf.disable());
            
        return http.build();
    }
}
