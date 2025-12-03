package com.integradorii.gimnasiov1.security;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación personalizada de {@link UserDetailsService} para Spring Security.
 *
 * Responsabilidades principales:
 *  - Cargar credenciales y roles de "personal" desde la tabla {@code usuarios}.
 *  - Cargar credenciales de "deportistas" desde la tabla {@code personas}.
 *  - Construir un {@link UserDetails} con la contraseña encriptada y la lista
 *    de {@link GrantedAuthority} que representan los roles del sistema.
 *
 * Reglas generales:
 *  - Primero intenta autenticar contra la tabla de personal administrativo.
 *  - Si no encuentra el email allí, busca en la tabla de deportistas.
 *  - Para personal, normaliza el código de rol (ADMIN, RECEP, ENTRENADOR, etc.).
 *  - Para deportistas, asigna siempre {@code ROLE_CLIENTE}.
 *  - Marca la cuenta como bloqueada si el usuario/persona no está activo.
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(PersonaRepository personaRepository, UsuarioRepository usuarioRepository) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    /**
     * Normaliza el código de rol proveniente de base de datos.
     *
     * Acepta diferentes variantes (por ejemplo "ADMIN" o "ADMINISTRADOR")
     * y devuelve un valor canónico como "ADMINISTRADOR", "RECEPCIONISTA"
     * o "ENTRENADOR". Esto simplifica el mapeo posterior a "ROLE_XXX".
     */
    private String canonicalRole(String codigo) {
        if (codigo == null) return "USUARIO";
        String c = codigo.trim().toUpperCase();
        switch (c) {
            case "ADMIN":
            case "ADMINISTRADOR":
                return "ADMINISTRADOR";
            case "RECEP":
            case "RECEPCIONISTA":
                return "RECEPCIONISTA";
            case "ENTRENADOR":
            case "COACH":
                return "ENTRENADOR";
            default:
                return c.isEmpty() ? "USUARIO" : c; // fallback: usar tal cual
        }
    }

    @Override
    /**
     * Carga un usuario por su email (username) para el proceso de login.
     *
     * Flujo detallado:
     *  1. Intenta encontrar el email en la tabla {@code usuarios} (personal).
     *     - Si existe, construye un {@link UserDetails} con el rol mapeado
     *       a partir de {@code usuario.getRol().getCodigo()}.
     *  2. Si no lo encuentra, busca en la tabla {@code personas} (deportistas).
     *     - Si existe, asigna siempre la autoridad {@code ROLE_CLIENTE}.
     *  3. En ambos casos:
     *     - Devuelve el hash de contraseña almacenado.
     *     - Marca la cuenta como bloqueada si el registro no está activo
     *       o, en el caso de personas, si está marcada como bloqueada.
     *  4. Si no se encuentra el email en ninguna tabla, lanza
     *     {@link UsernameNotFoundException}.
     */
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=== DEBUG LOGIN ===");
        System.out.println("Email: " + email);
        
        // Primero intentar buscar en usuarios (personal administrativo)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            System.out.println("Usuario encontrado en tabla USUARIOS (Personal Administrativo)");
            System.out.println("Nombre: " + usuario.getNombre() + " " + usuario.getApellido());
            System.out.println("Password (hash): " + usuario.getContraseña());
            System.out.println("Rol: " + (usuario.getRol() != null ? usuario.getRol().getCodigo() : "null"));
            System.out.println("Activo: " + usuario.getActivo());

            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // Asignar rol basado en el rol de la base de datos (normalizado)
            if (usuario.getRol() != null) {
                String rolCodigo = usuario.getRol().getCodigo();
                String rolCanon = canonicalRole(rolCodigo);
                authorities.add(new SimpleGrantedAuthority("ROLE_" + rolCanon));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_USUARIO"));
            }

            System.out.println("Authorities: " + authorities);
            boolean isAccountLocked = usuario.getActivo() == null || !usuario.getActivo();
            System.out.println("Account Locked: " + isAccountLocked);
            System.out.println("===================");

            return User.builder()
                    .username(usuario.getEmail())
                    .password(usuario.getContraseña())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(isAccountLocked)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }
        
        // Si no se encuentra en usuarios, buscar en personas (deportistas)
        Persona persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        System.out.println("Usuario encontrado en tabla PERSONAS (Deportista)");
        System.out.println("Nombre: " + persona.getNombre() + " " + persona.getApellido());
        System.out.println("Password (hash): " + persona.getContraseña());
        System.out.println("Membresia Activa: " + persona.getMembresiaActiva());
        System.out.println("Activo: " + persona.getActivo());
        System.out.println("Email Verificado: " + persona.getEmailVerificado());

        List<GrantedAuthority> authorities = new ArrayList<>();
        // Todos los deportistas tienen ROLE_CLIENTE
        authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));

        System.out.println("Authorities: " + authorities);
        // Bloquear cuenta si no está activa (email no verificado)
        boolean isAccountLocked = persona.getActivo() == null || !persona.getActivo() || Boolean.TRUE.equals(persona.getBloqueado());
        System.out.println("Account Locked: " + isAccountLocked);
        System.out.println("===================");

        return User.builder()
                .username(persona.getEmail())
                .password(persona.getContraseña())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(isAccountLocked)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
