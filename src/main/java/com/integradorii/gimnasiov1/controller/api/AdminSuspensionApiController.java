package com.integradorii.gimnasiov1.controller.api;

import com.integradorii.gimnasiov1.dto.SuspensionAdminDTO;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Plan;
import com.integradorii.gimnasiov1.model.Suscripcion;
import com.integradorii.gimnasiov1.model.SuspensionMembresia;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.service.EmailService;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import com.integradorii.gimnasiov1.repository.SuspensionMembresiaRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/suspensiones")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminSuspensionApiController {

    private final SuspensionMembresiaRepository suspensionRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AdminSuspensionApiController(SuspensionMembresiaRepository suspensionRepository,
                                        SuscripcionRepository suscripcionRepository,
                                        UsuarioRepository usuarioRepository,
                                        EmailService emailService) {
        this.suspensionRepository = suspensionRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<SuspensionAdminDTO>> listarPendientes() {
        List<SuspensionMembresia> suspensiones = suspensionRepository.findByEstadoIgnoreCase("pendiente");
        List<SuspensionAdminDTO> dtos = suspensiones.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/aprobar")
    @Transactional
    public ResponseEntity<?> aprobarSuspension(@PathVariable Long id, Principal principal) {
        Optional<SuspensionMembresia> opt = suspensionRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SuspensionMembresia suspension = opt.get();
        if (!"pendiente".equalsIgnoreCase(suspension.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of("error", "La solicitud no está en estado pendiente"));
        }

        Suscripcion suscripcion = suspension.getSuscripcion();
        if (suscripcion == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "La suspensión no tiene una suscripción asociada"));
        }

        LocalDate inicio = suspension.getFechaInicio();
        LocalDate fin = suspension.getFechaFin();
        if (inicio == null || fin == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las fechas de la suspensión no son válidas"));
        }

        long diasSuspension = ChronoUnit.DAYS.between(inicio, fin) + 1;
        if (diasSuspension < 0) {
            diasSuspension = 0;
        }

        if (suscripcion.getFechaFin() != null && diasSuspension > 0) {
            suscripcion.setFechaFin(suscripcion.getFechaFin().plusDays(diasSuspension));
        }
        if (suscripcion.getProximoPago() != null && diasSuspension > 0) {
            suscripcion.setProximoPago(suscripcion.getProximoPago().plusDays(diasSuspension));
        }

        if (principal != null) {
            String emailAdmin = principal.getName();
            usuarioRepository.findByEmail(emailAdmin)
                    .map(Usuario::getId)
                    .ifPresent(suspension::setAprobadoPor);
        }

        suspension.setEstado("aprobada");

        suscripcionRepository.save(suscripcion);
        suspensionRepository.save(suspension);

        notificarResultadoSuspension(suspension, "Aprobada");

        return ResponseEntity.ok(mapToDto(suspension));
    }

    @PostMapping("/{id}/rechazar")
    @Transactional
    public ResponseEntity<?> rechazarSuspension(@PathVariable Long id,
                                                @RequestBody(required = false) Map<String, Object> payload,
                                                Principal principal) {
        Optional<SuspensionMembresia> opt = suspensionRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SuspensionMembresia suspension = opt.get();
        if (!"pendiente".equalsIgnoreCase(suspension.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of("error", "La solicitud no está en estado pendiente"));
        }

        if (principal != null) {
            String emailAdmin = principal.getName();
            usuarioRepository.findByEmail(emailAdmin)
                    .map(Usuario::getId)
                    .ifPresent(suspension::setAprobadoPor);
        }

        // Si en el futuro se agrega un campo para nota del administrador en la entidad o tabla,
        // aquí se podría mapear el valor recibido en payload (por ejemplo payload.get("nota")).

        suspension.setEstado("rechazada");
        suspensionRepository.save(suspension);

        notificarResultadoSuspension(suspension, "Rechazada");

        return ResponseEntity.ok(mapToDto(suspension));
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> descargarAdjunto(@PathVariable Long id) {
        Optional<SuspensionMembresia> opt = suspensionRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SuspensionMembresia suspension = opt.get();
        String archivo = suspension.getArchivoAdjunto();
        if (archivo == null || archivo.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path uploadDir = Paths.get("uploads", "suspensiones").toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve(archivo).normalize();

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String visibleName = archivo;
            int idx = archivo.indexOf('_');
            if (idx > 0 && idx < archivo.length() - 1) {
                visibleName = archivo.substring(idx + 1);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + visibleName + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void notificarResultadoSuspension(SuspensionMembresia suspension, String estadoFinal) {
        Persona usuario = suspension.getUsuario();
        if (usuario == null) {
            return;
        }

        String email = usuario.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }

        String sanitizedEmail = email.replaceAll("[\\r\\n]", "").trim();
        if (sanitizedEmail.isEmpty() || !sanitizedEmail.contains("@")) {
            return;
        }

        String nombreCompleto = usuario.getNombreCompleto();
        LocalDate inicio = suspension.getFechaInicio();
        LocalDate fin = suspension.getFechaFin();
        String fechas;
        if (inicio != null && fin != null) {
            fechas = inicio.format(dateFormatter) + " al " + fin.format(dateFormatter);
        } else {
            fechas = "No disponible";
        }

        String motivo = suspension.getMotivo();
        if (motivo == null || motivo.isBlank()) {
            motivo = "No especificado";
        }

        String asunto = "Actualización de tu solicitud de suspensión de membresía - " + estadoFinal;

        String mensaje = "Estimado/a " + nombreCompleto + ",\n\n" +
                "Te informamos que tu solicitud de suspensión temporal de tu membresía ha sido " + estadoFinal.toLowerCase() + ".\n\n" +
                "Detalles de la solicitud:\n" +
                "- Estado final: " + estadoFinal + "\n" +
                "- Fechas solicitadas: " + fechas + "\n" +
                "- Motivo: " + motivo + "\n\n" +
                "Ante cualquier consulta adicional, puedes comunicarte con la administración de FitGym.\n\n" +
                "Atentamente,\n" +
                "Equipo FitGym";

        try {
            emailService.enviarNotificacionGeneral(
                    sanitizedEmail,
                    nombreCompleto,
                    asunto,
                    mensaje
            );
        } catch (MessagingException | IllegalArgumentException ex) {
        }
    }

    private SuspensionAdminDTO mapToDto(SuspensionMembresia suspension) {
        SuspensionAdminDTO dto = new SuspensionAdminDTO();
        dto.setId(suspension.getId());

        Persona usuario = suspension.getUsuario();
        if (usuario != null) {
            dto.setName(usuario.getNombreCompleto());
            dto.setUserId(usuario.getDni());
        }

        Suscripcion suscripcion = suspension.getSuscripcion();
        if (suscripcion != null) {
            if (suscripcion.getPlan() != null) {
                Plan plan = suscripcion.getPlan();
                dto.setPlan(plan.getNombre());
            }
            LocalDate fechaFinSuscripcion = suscripcion.getFechaFin();
            if (fechaFinSuscripcion != null) {
                dto.setExpire(fechaFinSuscripcion.format(dateFormatter));
            }
        }

        LocalDate inicio = suspension.getFechaInicio();
        LocalDate fin = suspension.getFechaFin();
        if (inicio != null) {
            dto.setStart(inicio.format(dateFormatter));
        }
        if (fin != null) {
            dto.setEnd(fin.format(dateFormatter));
        }
        if (inicio != null && fin != null) {
            long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;
            dto.setDays(dias);
            dto.setDates(inicio.format(dateFormatter) + " - " + fin.format(dateFormatter));
        }

        String motivo = suspension.getMotivo();
        if (motivo != null) {
            dto.setReasonDetail(motivo);
            int idx = motivo.indexOf(" - ");
            if (idx > 0) {
                dto.setReasonType(motivo.substring(0, idx));
            } else {
                dto.setReasonType(motivo);
            }
        }

        String estado = suspension.getEstado();
        if (estado != null && !estado.isBlank()) {
            String lower = estado.toLowerCase();
            String capitalized = lower.substring(0, 1).toUpperCase() + lower.substring(1);
            dto.setStatus(capitalized);
        } else {
            dto.setStatus("Pendiente");
        }

        String archivo = suspension.getArchivoAdjunto();
        if (archivo != null && !archivo.isBlank()) {
            String visibleName = archivo;
            int idx = archivo.indexOf('_');
            if (idx > 0 && idx < archivo.length() - 1) {
                visibleName = archivo.substring(idx + 1);
            }
            dto.setAttachmentName(visibleName);
            dto.setAttachmentUrl("/api/admin/suspensiones/" + suspension.getId() + "/archivo");
        }

        return dto;
    }
}
