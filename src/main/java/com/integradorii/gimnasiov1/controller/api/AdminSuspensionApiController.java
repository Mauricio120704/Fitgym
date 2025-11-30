package com.integradorii.gimnasiov1.controller.api;

import com.integradorii.gimnasiov1.dto.SuspensionAdminDTO;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Plan;
import com.integradorii.gimnasiov1.model.Suscripcion;
import com.integradorii.gimnasiov1.model.SuspensionMembresia;
import com.integradorii.gimnasiov1.model.Usuario;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import com.integradorii.gimnasiov1.repository.SuspensionMembresiaRepository;
import com.integradorii.gimnasiov1.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AdminSuspensionApiController(SuspensionMembresiaRepository suspensionRepository,
                                        SuscripcionRepository suscripcionRepository,
                                        UsuarioRepository usuarioRepository) {
        this.suspensionRepository = suspensionRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.usuarioRepository = usuarioRepository;
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

        return ResponseEntity.ok(mapToDto(suspension));
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

        return dto;
    }
}
