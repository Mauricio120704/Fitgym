package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Role;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.RoleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final PersonaRepository personaRepository;
    private final RoleRepository roleRepository;

    public HomeController(PersonaRepository personaRepository, RoleRepository roleRepository) {
        this.personaRepository = personaRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/inicio")
    public String inicio() { return "index"; }

    @GetMapping("/registro")
    public String registro() { return "registro"; }

    @GetMapping("/planes")
    public String planes(@RequestParam(required = false) String usuario, Model model) {
        model.addAttribute("usuario", usuario);
        return "planes";
    }

    @GetMapping("/checkout")
    public String checkout(
            @RequestParam(required = false, defaultValue = "Básico") String plan,
            @RequestParam(required = false, defaultValue = "mensual") String periodo,
            @RequestParam(required = false, defaultValue = "49.900") String precio,
            @RequestParam(required = false) String usuario,
            Model model) {
        model.addAttribute("planNombre", plan);
        model.addAttribute("periodo", periodo);
        model.addAttribute("precio", precio);
        model.addAttribute("usuario", usuario);
        return "checkout";
    }

    @GetMapping("/miembros")
    public String listarMiembros(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String rol,
            Model model) {

        // Base: todo el personal
        List<Persona> miembros = personaRepository.findByTipo("PERSONAL");

        // Filtro estado activo/inactivo
        if ("activos".equalsIgnoreCase(filtro)) {
            miembros = miembros.stream().filter(p -> Boolean.TRUE.equals(p.getMembresiaActiva())).collect(Collectors.toList());
        } else if ("inactivos".equalsIgnoreCase(filtro)) {
            miembros = miembros.stream().filter(p -> Boolean.FALSE.equals(p.getMembresiaActiva())).collect(Collectors.toList());
        }

        // Filtro por rol
        if (rol != null && !rol.isBlank() && !"todos".equalsIgnoreCase(rol)) {
            String rolFiltro = rol.trim().toUpperCase();
            miembros = miembros.stream()
                    .filter(p -> p.getRol() != null && rolFiltro.equalsIgnoreCase(p.getRol().getCodigo()))
                    .collect(Collectors.toList());
        }

        // Búsqueda texto
        if (buscar != null && !buscar.trim().isEmpty()) {
            miembros = personaRepository.searchPersonal(buscar.trim());
        }

        long totalMiembros = personaRepository.findByTipo("PERSONAL").size();
        long totalActivos = personaRepository.countPersonalActivos();
        long totalInactivos = personaRepository.countPersonalInactivos();

        model.addAttribute("miembros", miembros);
        model.addAttribute("filtroActual", filtro != null ? filtro : "todos");
        model.addAttribute("buscarActual", buscar != null ? buscar : "");
        model.addAttribute("rolActual", rol != null ? rol : "todos");
        model.addAttribute("totalMiembros", totalMiembros);
        model.addAttribute("totalActivos", totalActivos);
        model.addAttribute("totalInactivos", totalInactivos);

        return "miembros";
    }

    @GetMapping("/miembros/nuevo")
    public String nuevoMiembroForm(Model model) {
        model.addAttribute("miembro", new Persona());
        return "nuevo-miembro";
    }

    @PostMapping("/miembros")
    public String guardarMiembro(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String dni,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) Boolean membresiaActiva,
            @RequestParam(required = false) String rol) {

        Persona p = new Persona();
        p.setNombre(nombre);
        p.setApellido(apellido);
        p.setEmail(email);
        p.setTelefono(telefono);
        p.setDni(dni);
        p.setFechaRegistro(LocalDate.now());
        p.setTipo("PERSONAL");
        p.setMembresiaActiva(membresiaActiva == null ? Boolean.TRUE : membresiaActiva);

        String rolCodigo = (rol == null || rol.isBlank() || "DEPORTISTA".equalsIgnoreCase(rol)) ? "RECEPCIONISTA" : rol.toUpperCase();
        Role r = roleRepository.findByCodigo(rolCodigo).orElse(null);
        p.setRol(r);

        personaRepository.save(p);
        return "redirect:/miembros";
    }

    @GetMapping("/miembros/editar/{id}")
    public String editarMiembroForm(@PathVariable Long id, Model model) {
        Persona p = personaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        model.addAttribute("miembro", p);
        return "editar-miembro";
    }

    @PostMapping("/miembros/{id}")
    public String actualizarMiembro(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String dni,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) Boolean membresiaActiva,
            @RequestParam(required = false) String rol) {

        Persona p = personaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        p.setNombre(nombre);
        p.setApellido(apellido);
        p.setEmail(email);
        p.setTelefono(telefono);
        p.setDni(dni);
        p.setMembresiaActiva(membresiaActiva == null ? Boolean.TRUE : membresiaActiva);
        if (rol != null && !rol.isBlank() && !"DEPORTISTA".equalsIgnoreCase(rol)) {
            roleRepository.findByCodigo(rol.toUpperCase()).ifPresent(p::setRol);
        }
        personaRepository.save(p);
        return "redirect:/miembros";
    }

    @GetMapping("/miembros/eliminar/{id}")
    public String eliminarMiembro(@PathVariable Long id) {
        personaRepository.deleteById(id);
        return "redirect:/miembros";
    }

    @GetMapping("/perfil")
    public String perfilUsuario(@RequestParam(required = false) String usuario, Model model) {
        if (usuario == null || usuario.isBlank()) {
            return "redirect:/registro";
        }
        Persona persona = personaRepository.findByEmail(usuario).orElse(null);
        if (persona == null) {
            return "redirect:/registro";
        }
        model.addAttribute("miembro", persona);
        return "perfil-usuario";
    }

    @PostMapping("/registro")
    public String registrarDeportista(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String dni,
            @RequestParam(required = false) String telefono,
            Model model) {
        // Validaciones de unicidad previas para evitar 500 por restricciones UNIQUE
        if (personaRepository.existsByEmail(email)) {
            model.addAttribute("error", "El email ya está registrado");
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellido", apellido);
            model.addAttribute("email", email);
            model.addAttribute("dni", dni);
            model.addAttribute("telefono", telefono);
            return "registro";
        }
        if (personaRepository.existsByDni(dni)) {
            model.addAttribute("error", "El DNI ya está registrado");
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellido", apellido);
            model.addAttribute("email", email);
            model.addAttribute("dni", dni);
            model.addAttribute("telefono", telefono);
            return "registro";
        }

        try {
            Persona p = new Persona();
            p.setNombre(nombre);
            p.setApellido(apellido);
            p.setEmail(email);
            p.setTelefono(telefono);
            p.setDni(dni);
            p.setFechaRegistro(LocalDate.now());
            p.setTipo("DEPORTISTA");
            p.setMembresiaActiva(Boolean.TRUE);
            personaRepository.save(p);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("error", "No se pudo registrar. Verifica que email y DNI sean únicos.");
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellido", apellido);
            model.addAttribute("email", email);
            model.addAttribute("dni", dni);
            model.addAttribute("telefono", telefono);
            return "registro";
        }
        return "redirect:/planes?usuario=" + email;
    }

    @GetMapping("/configuracion")
    public String configuracion() { return "configuracion"; }
}
