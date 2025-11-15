package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.model.Role;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import com.integradorii.gimnasiov1.repository.RoleRepository;
import com.integradorii.gimnasiov1.dto.RegistroUsuarioDTO;
import com.integradorii.gimnasiov1.security.PasswordEncoderService;
import com.integradorii.gimnasiov1.service.VerificationTokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador Principal - Páginas generales y gestión de usuarios
 * Rutas: /, /inicio, /login, /registro, /miembros, /perfil
 * Acceso: Público (registro, login) y autenticado (perfil, miembros)
 * Tablas: personas (deportistas), usuarios (personal), roles
 */
@Controller
public class HomeController {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderService passwordEncoderService;
    private final VerificationTokenService verificationTokenService;

    public HomeController(PersonaRepository personaRepository,
                         UsuarioRepository usuarioRepository,
                         RoleRepository roleRepository,
                         PasswordEncoderService passwordEncoderService,
                         VerificationTokenService verificationTokenService) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoderService = passwordEncoderService;
        this.verificationTokenService = verificationTokenService;
    }

    // GET / - Página principal pública
    @GetMapping("/")
    public String home() { return "index"; }

    // GET /inicio - Dashboard general (redirección automática desde login)
    @GetMapping("/inicio")
    public String inicio() { return "index"; }

    /**
     * GET /login - Página de inicio de sesión
     * Parámetros opcionales: ?error=true, ?logout=true
     */
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                       @RequestParam(required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "Email o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("success", "Has cerrado sesión correctamente");
        }
        return "login";
    }

    // GET /registro - Formulario de registro de deportistas
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new RegistroUsuarioDTO());
        return "registro";
    }

    // GET /recuperacion - Página de recuperación de contraseña
    @GetMapping("/recuperacion")
    public String recuperacion() {
        return "recuperacion";
    }

    // GET /planes - Página de planes de membresía (acceso público)
    @GetMapping("/planes")
    public String planes(@RequestParam(required = false) String usuario, Model model) {
        model.addAttribute("usuario", usuario);
        return "planes";
    }

    // GET /miembros/debug - Endpoint de debug (redirige a /miembros)
    @GetMapping("/miembros/debug")
    public String debugMiembros(Model model, HttpServletRequest request) {
        return "redirect:/miembros";
    }

    /**
     * GET /miembros - Lista personal administrativo (usuarios)
     * Filtros: activos/inactivos, rol, búsqueda por texto
     * Acceso: ADMIN, RECEPCIONISTA, ENTRENADOR
     */
    @GetMapping("/miembros")
    public String listarMiembros(Model model,
                                @RequestParam(required = false) String filtro,
                                @RequestParam(required = false) String buscar,
                                @RequestParam(required = false) String rol,
                                HttpServletResponse response,
                                HttpServletRequest request) {
        // Configurar headers para evitar errores de chunked encoding
        response.setContentType("text/html;charset=UTF-8");
        response.setBufferSize(8192);
        response.setHeader("Connection", "close");

        // Agregar CSRF token para formularios
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);
        try {
            // Seleccionar la fuente inicial de miembros en función del filtro para evitar problemas
            List<Usuario> miembros;
            String filtroNormalized = filtro != null ? filtro.trim().toLowerCase() : "todos";

            if ("activos".equals(filtroNormalized)) {
                miembros = usuarioRepository.findByActivoTrue();
            } else if ("inactivos".equals(filtroNormalized)) {
                miembros = usuarioRepository.findByActivoFalse();
            } else {
                miembros = usuarioRepository.findAll();
            }

            // Filtro por rol (si se especifica y no es 'todos')
            if (rol != null && !rol.isBlank() && !"todos".equalsIgnoreCase(rol)) {
                String rolFiltro = rol.trim().toUpperCase();
                // Si ya venimos de una consulta por estado, filtrar en memoria por rol
        miembros = miembros.stream()
            .filter(u -> u.getRol() != null && rolFiltro.equalsIgnoreCase(u.getRol().getCodigo()))
            .collect(Collectors.toList());
            }

            // Búsqueda texto: filtrar sobre la lista ya obtenida
            if (buscar != null && !buscar.trim().isEmpty()) {
                final String q = buscar.trim().toLowerCase();
        miembros = miembros.stream()
            .filter(u -> (u.getNombre() != null && u.getNombre().toLowerCase().contains(q))
                || (u.getApellido() != null && u.getApellido().toLowerCase().contains(q))
                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                || (u.getDni() != null && u.getDni().toLowerCase().contains(q)))
            .collect(Collectors.toList());
            }

            // Calcular totales usando los datos en memoria
            long totalMiembrosDB = usuarioRepository.count();
            List<Usuario> todosLosMiembros = usuarioRepository.findAll();
            long totalActivosDB = todosLosMiembros.stream()
                .filter(u -> u.getActivo() != null && u.getActivo())
                .count();
            long totalInactivosDB = todosLosMiembros.stream()
                .filter(u -> u.getActivo() == null || !u.getActivo())
                .count();

            // Totales calculados (puestos en el modelo)

            // Actualizar el modelo
            model.addAttribute("miembros", miembros);
            model.addAttribute("filtroActual", filtro != null ? filtro : "todos");
            model.addAttribute("buscarActual", buscar != null ? buscar : "");
            model.addAttribute("rolActual", rol != null ? rol : "todos");
            model.addAttribute("totalMiembros", totalMiembrosDB);
            model.addAttribute("totalActivos", totalActivosDB);
            model.addAttribute("totalInactivos", totalInactivosDB);

            return "miembros";
        } catch (Exception ex) {
            // Log the exception and show a lightweight error message in the same view so the response
            // isn't interrupted by an uncaught exception originating inside the controller.
            ex.printStackTrace();
            model.addAttribute("miembros", java.util.Collections.emptyList());
            model.addAttribute("filtroActual", "todos");
            model.addAttribute("buscarActual", "");
            model.addAttribute("rolActual", "todos");
            model.addAttribute("totalMiembros", 0);
            model.addAttribute("totalActivos", 0);
            model.addAttribute("totalInactivos", 0);
            model.addAttribute("error", "Error al listar miembros: " + ex.getMessage());
            return "miembros";
        }
    }

    /**
     * GET /miembros/nuevo - Formulario para crear nuevo miembro del personal
     * Solo se crea personal (ADMIN, RECEPCIONISTA, ENTRENADOR)
     */
    @GetMapping("/miembros/nuevo")
    public String nuevoMiembroForm(Model model) {
        model.addAttribute("miembro", new Usuario());
        return "nuevo-miembro";
    }

    /**
     * POST /miembros - Guarda nuevo miembro del personal
     * Email se normaliza a @fitgym.com
     * Asigna departamento y puesto según rol
     */
    @PostMapping("/miembros")
    public String guardarMiembro(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String dni,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            Model model) {

        // Normalizar email a dominio @fitgym.com para personal
        String emailTrim = email != null ? email.trim().toLowerCase() : "";
        String local = emailTrim;
        int atIdx = emailTrim.lastIndexOf('@');
        if (atIdx > -1) {
            local = emailTrim.substring(0, atIdx);
        }
        String emailNorm = local + "@fitgym.com";

        if (password == null || confirmPassword == null || password.isBlank() || confirmPassword.isBlank()) {
            model.addAttribute("miembro", new Usuario());
            model.addAttribute("error", "La contraseña y su confirmación son obligatorias");
            return "nuevo-miembro";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("miembro", new Usuario());
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "nuevo-miembro";
        }
        if (usuarioRepository.existsByEmail(emailNorm)) {
            model.addAttribute("miembro", new Usuario());
            model.addAttribute("error", "El email ya está registrado");
            return "nuevo-miembro";
        }
        if (usuarioRepository.existsByDni(dni)) {
            model.addAttribute("miembro", new Usuario());
            model.addAttribute("error", "El DNI ya está registrado");
            return "nuevo-miembro";
        }

        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(emailNorm);
        u.setTelefono(telefono);
        u.setDni(dni);
        u.setFechaRegistro(LocalDate.now());
        u.setActivo(activo == null ? Boolean.TRUE : activo);
        u.setFechaInicioLaboral(LocalDate.now());

        // Determinar rol (por defecto RECEPCIONISTA, nunca DEPORTISTA)
        String rolCodigo = (rol == null || rol.isBlank() || "DEPORTISTA".equalsIgnoreCase(rol)) ? "RECEPCIONISTA" : rol.toUpperCase();
        Role r = roleRepository.findByCodigo(rolCodigo).orElse(null);
        u.setRol(r);
        
        // Asignar departamento y puesto según rol
        if (r != null) {
            switch (r.getCodigo()) {
                case "ADMINISTRADOR": u.setDepartamento("Administración General"); u.setPuesto("Administrador"); break;
                case "RECEPCIONISTA": u.setDepartamento("Recepción y Atención al Cliente"); u.setPuesto("Recepcionista"); break;
                case "ENTRENADOR": u.setDepartamento("Departamento de Entrenamiento"); u.setPuesto("Entrenador"); break;
            }
        }

        String hashedPassword = passwordEncoderService.encode(password);
        u.setContraseña(hashedPassword);

        usuarioRepository.save(u);
        return "redirect:/miembros";
    }

    /**
     * GET /miembros/editar/{id} - Formulario para editar miembro del personal
     */
    @GetMapping("/miembros/editar/{id}")
    public String editarMiembroForm(@PathVariable Long id, Model model, HttpServletResponse response) {
        // Configurar headers para evitar errores de respuesta
        response.setContentType("text/html;charset=UTF-8");
        response.setBufferSize(8192); // Aumentar el tamaño del buffer
        response.setHeader("Connection", "close"); // Evitar keep-alive
        response.setHeader("Transfer-Encoding", "identity"); // Evitar chunked encoding

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ID de usuario inválido: " + id));
            model.addAttribute("miembro", usuario);
            return "editar-miembro";
        } catch (Exception ex) {
            model.addAttribute("error", "Error al cargar el usuario: " + ex.getMessage());
            return "redirect:/miembros";
        }
    }

    /**
     * POST /miembros/eliminar/{id} - Elimina miembro del personal
     * CUIDADO: Eliminación permanente de la BD
     */
    @PostMapping("/miembros/eliminar/{id}")
    public String eliminarMiembro(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/miembros";
    }

    /**
     * GET /perfil - Perfil de deportista autenticado
     * Acceso: Solo ROLE_CLIENTE (deportistas)
     * Tabla: personas
     */
    @GetMapping("/perfil")
    public String perfilUsuario(Model model,
                               @org.springframework.security.core.annotation.AuthenticationPrincipal
                               org.springframework.security.core.userdetails.UserDetails userDetails,
                               HttpServletResponse response,
                               HttpServletRequest request) {
        // Configurar headers para evitar errores de respuesta
        response.setContentType("text/html;charset=UTF-8");
        response.setBufferSize(8192);
        response.setHeader("Connection", "close");
        response.setHeader("Transfer-Encoding", "identity");

        if (userDetails == null) {
            return "redirect:/login";
        }

        String email = userDetails.getUsername();
        Persona persona = personaRepository.findByEmail(email).orElse(null);

        if (persona == null) {
            return "redirect:/login";
        }

        try {
            // Añadir al modelo los objetos esperados por la vista
            model.addAttribute("usuario", persona);
            model.addAttribute("miembro", persona);

            // Muchas plantillas (originalmente pensadas para 'Usuario') esperan propiedades como 'tipo' o
            // 'rol.nombre' que no existen en Persona; proporcionar valores por defecto evita excepciones en Thymeleaf.
            model.addAttribute("tipo", "Deportista");
            model.addAttribute("rolNombre", "Deportista");

            // Añadir CSRF token si la vista lo necesita
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            if (csrfToken != null) {
                model.addAttribute("_csrf", csrfToken);
            }

            return "perfil-usuario";
        } catch (Exception ex) {
            ex.printStackTrace();
            // En caso de error, devolver una página ligera o redirigir para evitar que el servidor cierre la conexión abruptamente
            model.addAttribute("error", "No se pudo cargar el perfil: " + ex.getMessage());
            return "perfil-usuario";
        }
    }

    /**
     * POST /registro - Registra nuevo deportista/cliente
     * Validaciones: email único, DNI único, contraseñas coinciden
     * Envía email de verificación, cuenta inactiva hasta verificar
     */
    @PostMapping("/registro")
    public String registrarDeportista(@Valid @ModelAttribute("usuario") RegistroUsuarioDTO dto,
                                   BindingResult result,
                                   Model model,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {

        // Validar errores de validación
        if (result.hasErrors()) {
            model.addAttribute("usuario", dto);
            return "registro";
        }

        // Verificar si las contraseñas coinciden
        if (dto.getPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("usuario", dto);
            return "registro";
        }

        // Verificar si el email ya existe
        if (personaRepository.existsByEmail(dto.getEmail())) {
            model.addAttribute("error", "El email ya está registrado");
            model.addAttribute("usuario", dto);
            return "registro";
        }

        // Verificar si el DNI ya existe
        if (personaRepository.existsByDni(dto.getDni())) {
            model.addAttribute("error", "El DNI ya está registrado");
            model.addAttribute("usuario", dto);
            return "registro";
        }

        try {
            Persona p = new Persona();
            p.setNombre(dto.getNombre());
            p.setApellido(dto.getApellido());
            p.setEmail(dto.getEmail());
            p.setTelefono(dto.getTelefono());
            p.setDni(dto.getDni());
            p.setFechaRegistro(LocalDate.now());
            // La membresía inicia como INACTIVA hasta que el deportista contrate un plan
            p.setMembresiaActiva(Boolean.FALSE);
            
            // Cuenta inactiva hasta verificar email
            p.setActivo(Boolean.FALSE);
            p.setEmailVerificado(Boolean.FALSE);

            // IMPORTANTE: Registro público solo crea deportistas
            // Personal se crea desde /miembros por administrador
            
            // Encriptar la contraseña antes de guardarla
            String hashedPassword = passwordEncoderService.encode(dto.getPassword());
            p.setContraseña(hashedPassword);

            Persona personaGuardada = personaRepository.save(p);

            // Crear token y enviar email de verificación
            verificationTokenService.crearTokenYEnviarEmail(personaGuardada);

            // Redirigir a página de confirmación en lugar de auto-login
            model.addAttribute("success", "Registro exitoso. Por favor, revisa tu correo electrónico para verificar tu cuenta.");
            model.addAttribute("email", personaGuardada.getEmail());
            return "registro-exitoso";

        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("error", "Error al procesar el registro: " + ex.getMessage());
            model.addAttribute("usuario", dto);
            return "registro";
        }
    }

    /**
     * POST /miembros/editar/{id} - Actualiza datos de miembro del personal
     * Actualiza departamento y puesto si cambia el rol
     */
    @PostMapping("/miembros/editar/{id}")
    public String editarMiembro(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String dni,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) Boolean activo,
            @RequestParam String rol,
            Model model,
            HttpServletResponse response) {
        
        // Configurar headers para evitar chunking y buffering inadecuado
        response.setContentType("text/html;charset=UTF-8");
        response.setBufferSize(8192);
        response.setHeader("Connection", "close");
        response.setHeader("Transfer-Encoding", "identity");

        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ID de usuario inválido: " + id));

            // Validar DNI si cambió
            if (!usuario.getDni().equals(dni) && usuarioRepository.existsByDni(dni)) {
                model.addAttribute("miembro", usuario);
                model.addAttribute("error", "El DNI ya está registrado");
                return "editar-miembro";
            }

            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setDni(dni);
            usuario.setTelefono(telefono);
            usuario.setActivo(activo != null ? activo : false);

            // Actualizar rol si cambió
            if (!usuario.getRol().getCodigo().equals(rol)) {
                Role nuevoRol = roleRepository.findByCodigo(rol)
                        .orElseThrow(() -> new IllegalArgumentException("Rol inválido: " + rol));
                usuario.setRol(nuevoRol);
                
                // Actualizar departamento y puesto según el rol
                switch (nuevoRol.getCodigo()) {
                    case "ADMINISTRADOR":
                        usuario.setDepartamento("Administración General");
                        usuario.setPuesto("Administrador");
                        break;
                    case "RECEPCIONISTA":
                        usuario.setDepartamento("Recepción y Atención al Cliente");
                        usuario.setPuesto("Recepcionista");
                        break;
                    case "ENTRENADOR":
                        usuario.setDepartamento("Departamento de Entrenamiento");
                        usuario.setPuesto("Entrenador");
                        break;
                }
            }

            usuarioRepository.save(usuario);
            return "redirect:/miembros?success=true";
        } catch (Exception ex) {
            model.addAttribute("error", "Error al actualizar el usuario: " + ex.getMessage());
            return "editar-miembro";
        }
    }

    // GET /configuracion - Página de configuración del sistema
    @GetMapping("/configuracion")
    public String configuracion() { return "configuracion"; }
}
