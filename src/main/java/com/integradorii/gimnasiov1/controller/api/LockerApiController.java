package com.integradorii.gimnasiov1.controller.api;

import com.integradorii.gimnasiov1.dto.PersonaDTO;
import com.integradorii.gimnasiov1.model.Locker;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.EstadoLocker;
import com.integradorii.gimnasiov1.repository.LockerRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.service.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lockers")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
public class LockerApiController {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private AsistenciaService asistenciaService;

    @GetMapping
    public ResponseEntity<List<Locker>> getAllLockers() {
        List<Locker> lockers = lockerRepository.findAll();
        return ResponseEntity.ok(lockers);
    }

    @GetMapping("/buscar-personas")
    public ResponseEntity<List<PersonaDTO>> buscarPersonas(@RequestParam String termino) {
        List<Persona> personas = personaRepository.buscarDeportistasActivosPorNombreOApellido(
            termino
        );

        List<PersonaDTO> resultados = personas.stream()
            .map(p -> {
                PersonaDTO dto = new PersonaDTO();
                dto.setId(p.getId());
                dto.setNombreCompleto(p.getNombre() + " " + p.getApellido());
                dto.setDni(p.getDni());
                dto.setTipoMembresia(p.getMembresiaActiva() ? "Activa" : "Inactiva");
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(resultados);
    }

    @PostMapping("/asignar")
    public ResponseEntity<?> asignarLocker(
            @RequestParam String numeroLocker,
            @RequestParam long personaId) {
        
        Optional<Locker> lockerOpt = lockerRepository.findByNumero(numeroLocker);
        Optional<Persona> personaOpt = personaRepository.findById(personaId);

        if (lockerOpt.isEmpty() || personaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Locker o persona no encontrados");
        }

        Locker locker = lockerOpt.get();
        if (locker.getEstado() != EstadoLocker.DISPONIBLE) {
            return ResponseEntity.badRequest().body("El locker no está disponible");
        }

        locker.setPersonaAsignada(personaOpt.get());
        locker.setEstado(EstadoLocker.OCUPADO);
        locker.setFechaAsignacion(LocalDateTime.now());
        lockerRepository.save(locker);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/liberar/{numeroLocker}")
    public ResponseEntity<?> liberarLocker(@PathVariable String numeroLocker) {
        Optional<Locker> lockerOpt = lockerRepository.findByNumero(numeroLocker);
        
        if (lockerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Locker locker = lockerOpt.get();
        
        // Si el locker tiene una persona asignada, actualizar su asistencia
        if (locker.getPersonaAsignada() != null) {
            try {
                // Actualizar la asistencia de la persona
                asistenciaService.registrarSalidaPorLocker(locker.getPersonaAsignada().getId());
            } catch (Exception e) {
                // Registrar el error pero continuar con la liberación del locker
                System.err.println("Error al actualizar la asistencia al liberar el locker: " + e.getMessage());
            }
        }
        
        // Liberar el locker
        locker.setPersonaAsignada(null);
        locker.setEstado(EstadoLocker.DISPONIBLE);
        locker.setFechaAsignacion(null);
        lockerRepository.save(locker);

        return ResponseEntity.ok().build();
    }
}
