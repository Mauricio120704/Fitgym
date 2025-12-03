package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Suscripcion;
import com.integradorii.gimnasiov1.model.SuspensionMembresia;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import com.integradorii.gimnasiov1.repository.SuspensionMembresiaRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Controlador para la suspensión temporal de una membresía activa.
 *
 * Reglas principales:
 * - Solo permite suspender si el deportista tiene una suscripción activa.
 * - Valida el rango de fechas (inicio en el futuro, fin posterior al inicio).
 * - Enforce un mínimo de 3 días por suspensión y un máximo de 30 días acumulados por año.
 * - Extiende automáticamente la fecha de fin de la suscripción y el próximo pago
 *   según la cantidad de días suspendidos.
 */
@Controller
@RequestMapping("/suspension")
public class SuspensionController {

    private final PersonaRepository personaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final SuspensionMembresiaRepository suspensionRepository;

    public SuspensionController(PersonaRepository personaRepository,
                                SuscripcionRepository suscripcionRepository,
                                SuspensionMembresiaRepository suspensionRepository) {
        this.personaRepository = personaRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.suspensionRepository = suspensionRepository;
    }

    /**
     * Muestra el formulario de solicitud de suspensión para el deportista autenticado.
     *
     * Si el usuario no está autenticado o no tiene una suscripción activa, se redirige
     * a login o al perfil, ya que no tiene sentido suspender una membresía inexistente.
     */
    @GetMapping
    public String mostrarFormulario(Model model,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String email = userDetails.getUsername();
        Persona persona = personaRepository.findByEmail(email).orElse(null);
        if (persona == null) {
            return "redirect:/login";
        }

        Suscripcion suscripcion = suscripcionRepository
                .findActiveByDeportistaId(persona.getId())
                .orElse(null);
        if (suscripcion == null) {
            // Sin suscripción activa no tiene sentido suspender
            return "redirect:/perfil";
        }
        LocalDate hoy = LocalDate.now();
        model.addAttribute("hoy", hoy);

        model.addAttribute("activeMenu", "suspension");

        return "suspension";
    }

    /**
     * Procesa la solicitud de suspensión temporal.
     *
     * Pasos principales:
     * - Obtiene al deportista autenticado y su suscripción activa.
     * - Parsea y valida el rango de fechas ingresado.
     * - Verifica que no exista otra suspensión activa en el período actual.
     * - Calcula los días de suspensión y controla que no superen los 30 días
     *   acumulados por año.
     * - Registra la suspensión como aprobada y extiende las fechas de la suscripción
     *   (vencimiento y próximo pago) en la misma cantidad de días.
     */
    @PostMapping
    @Transactional
    public String solicitarSuspension(@RequestParam("motivo") String motivo,
                                      @RequestParam("fecha_inicio") String fechaInicioStr,
                                      @RequestParam("fecha_fin") String fechaFinStr,
                                      @RequestParam(name = "detalles", required = false) String detalles,
                                      @RequestParam(name = "archivo", required = false) MultipartFile archivo,
                                      Model model,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String email = userDetails.getUsername();
        Persona persona = personaRepository.findByEmail(email).orElse(null);
        if (persona == null) {
            return "redirect:/login";
        }

        Suscripcion suscripcion = suscripcionRepository
                .findActiveByDeportistaId(persona.getId())
                .orElse(null);
        if (suscripcion == null) {
            model.addAttribute("error", "No tienes una suscripción activa para suspender.");
            LocalDate hoy = LocalDate.now();
            model.addAttribute("hoy", hoy);
            model.addAttribute("activeMenu", "suspension");
            return "suspension";
        }

        LocalDate hoy = LocalDate.now();
        model.addAttribute("hoy", hoy);
        model.addAttribute("activeMenu", "suspension");
        LocalDate fechaInicio;
        LocalDate fechaFin;
        try {
            fechaInicio = LocalDate.parse(fechaInicioStr);
            fechaFin = LocalDate.parse(fechaFinStr);
        } catch (Exception e) {
            model.addAttribute("error", "Las fechas ingresadas no son válidas.");
            return "suspension";
        }

        // Validaciones básicas de fechas
        if (fechaInicio.isBefore(hoy)) {
            model.addAttribute("error", "La fecha de inicio no puede ser anterior a hoy.");
            return "suspension";
        }

        if (!fechaFin.isAfter(fechaInicio)) {
            model.addAttribute("error", "La fecha de fin debe ser posterior a la fecha de inicio.");
            return "suspension";
        }

        // Cantidad total de días de suspensión (incluye ambos extremos del rango).
        long diasSuspension = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        if (diasSuspension < 3) {
            model.addAttribute("error", "El rango mínimo de suspensión es de 3 días.");
            return "suspension";
        }

        // Buscar suspensiones previas del usuario
        List<SuspensionMembresia> suspensionesUsuario = suspensionRepository.findByUsuario_Id(persona.getId());

        // Validación de superposición de fechas entre la nueva suspensión y
        // cualquier otra suspensión ya aprobada/activa del mismo usuario.
        // Dos rangos se consideran solapados si comparten al menos un día en común.
        boolean haySuperposicion = suspensionesUsuario.stream().anyMatch(s -> {
            String estado = s.getEstado();
            if (estado == null) {
                return false;
            }

            // Solo se consideran suspensiones que ya están aprobadas o activas.
            if (!"aprobada".equalsIgnoreCase(estado)
                    && !"activa".equalsIgnoreCase(estado)) {
                return false;
            }

            LocalDate ini = s.getFechaInicio();
            LocalDate fin = s.getFechaFin();
            if (ini == null || fin == null) {
                return false;
            }

            // Se solapan si [fechaInicio, fechaFin] y [ini, fin] comparten al menos un día.
            // Equivale a: fechaInicio <= fin && fechaFin >= ini (rangos cerrados).
            boolean seSolapan = !fechaInicio.isAfter(fin) && !fechaFin.isBefore(ini);
            return seSolapan;
        });

        if (haySuperposicion) {
            model.addAttribute("error", "Ya tienes una suspensión registrada en esas fechas. Por favor selecciona un nuevo rango.");
            return "suspension";
        }

        // Regla: no debe tener una suspensión actualmente activa
        boolean tieneActiva = suspensionesUsuario.stream().anyMatch(s -> {
            LocalDate ini = s.getFechaInicio();
            LocalDate fin = s.getFechaFin();
            return !hoy.isBefore(ini) && !hoy.isAfter(fin)
                    && ("aprobada".equalsIgnoreCase(s.getEstado())
                        || "activa".equalsIgnoreCase(s.getEstado()));
        });

        if (tieneActiva) {
            model.addAttribute("error", "Ya tienes una suspensión activa de tu membresía.");
            return "suspension";
        }

        // Regla: máximo 30 días de suspensión acumulados por año para evitar abusos.
        int year = fechaInicio.getYear();
        long diasUsadosAnio = suspensionesUsuario.stream()
                .filter(s -> {
                    LocalDate ini = s.getFechaInicio();
                    return ini != null && ini.getYear() == year
                            && ("aprobada".equalsIgnoreCase(s.getEstado())
                                || "activa".equalsIgnoreCase(s.getEstado()));
                })
                .mapToLong(s -> ChronoUnit.DAYS.between(s.getFechaInicio(), s.getFechaFin()) + 1)
                .sum();

        if (diasUsadosAnio + diasSuspension > 30) {
            model.addAttribute("error", "No puedes superar los 30 días de suspensión acumulados por año.");
            return "suspension";
        }

        String storedFileName = null;
        if (archivo != null && !archivo.isEmpty()) {
            String originalFilename = archivo.getOriginalFilename();
            String lowerName = originalFilename != null ? originalFilename.toLowerCase() : "";
            String extension = "";
            int dotIndex = lowerName.lastIndexOf('.');
            if (dotIndex > -1 && dotIndex < lowerName.length() - 1) {
                extension = lowerName.substring(dotIndex + 1);
            }

            boolean extensionValida = "pdf".equals(extension)
                    || "doc".equals(extension)
                    || "docx".equals(extension)
                    || "jpg".equals(extension)
                    || "jpeg".equals(extension)
                    || "png".equals(extension);

            if (!extensionValida) {
                model.addAttribute("error", "El archivo adjunto debe ser PDF, DOC, DOCX, JPG o PNG.");
                return "suspension";
            }

            try {
                String safeBaseName = originalFilename != null
                        ? originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_")
                        : "adjunto";
                String uuid = UUID.randomUUID().toString();
                storedFileName = uuid + "_" + safeBaseName;

                Path uploadDir = Paths.get("uploads", "suspensiones").toAbsolutePath().normalize();
                Files.createDirectories(uploadDir);
                Path targetPath = uploadDir.resolve(storedFileName);
                archivo.transferTo(java.util.Objects.requireNonNull(targetPath.toFile()));
            } catch (IOException ex) {
                model.addAttribute("error", "No se pudo guardar el archivo adjunto. Inténtalo nuevamente más tarde.");
                return "suspension";
            }
        }

        // Motivo completo (tipo + detalles)
        String motivoFinal = motivo;
        if (detalles != null && !detalles.isBlank()) {
            motivoFinal = motivo + " - " + detalles.trim();
        }

        // Crear suspensión (se registra inicialmente como pendiente)
        SuspensionMembresia suspension = new SuspensionMembresia();
        suspension.setSuscripcion(suscripcion);
        suspension.setUsuario(persona);
        suspension.setFechaInicio(fechaInicio);
        suspension.setFechaFin(fechaFin);
        suspension.setMotivo(motivoFinal);
        suspension.setEstado("pendiente");
        if (storedFileName != null) {
            suspension.setArchivoAdjunto(storedFileName);
        }
        suspensionRepository.save(suspension);

        // La extensión de fechas de la suscripción se realizará cuando un administrador apruebe la suspensión.

        // Redirigir al perfil del deportista
        return "redirect:/perfil";
    }
}
