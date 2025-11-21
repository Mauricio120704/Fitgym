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

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(PersonaRepository personaRepository, UsuarioRepository usuarioRepository) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
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
