package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.ClaseCalificacion;
import com.integradorii.gimnasiov1.repository.ClaseCalificacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

@RestController
@RequestMapping("/api/clases/calificaciones")
public class ClaseCalificacionApiController {

    private final ClaseCalificacionRepository repo;

    public ClaseCalificacionApiController(ClaseCalificacionRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/by-reserva/{reservaId}")
    public ResponseEntity<?> getByReserva(@PathVariable Long reservaId) {
        Optional<ClaseCalificacion> opt = repo.findByReservaId(reservaId);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createOrUpdate(@RequestBody ClaseCalificacion payload) {
        // Validación temprana: una calificación necesita vínculo con reserva, clase y deportista
        if (payload.getReservaId() == null || payload.getClaseId() == null || payload.getDeportistaId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "reservaId, claseId y deportistaId son obligatorios"));
        }
        Optional<ClaseCalificacion> existing = repo.findByReservaId(payload.getReservaId());
        ClaseCalificacion c = existing.orElseGet(ClaseCalificacion::new);
        // Si es una nueva calificación seteamos la reserva para mantener la relación única
        if (c.getId() == null) {
            c.setReservaId(payload.getReservaId());
        }
        // Actualizamos todas las métricas de calificación desde el payload
        c.setClaseId(payload.getClaseId());
        c.setDeportistaId(payload.getDeportistaId());
        c.setRatingGeneral(payload.getRatingGeneral());
        c.setRatingInstructor(payload.getRatingInstructor());
        c.setRatingInstalaciones(payload.getRatingInstalaciones());
        c.setRatingMusica(payload.getRatingMusica());
        c.setRatingDificultad(payload.getRatingDificultad());
        c.setComentario(payload.getComentario());
        ClaseCalificacion saved = repo.save(c);
        // Retornamos 200 si ya existía registro, 201 Created cuando se inserta uno nuevo
        if (existing.isPresent()) {
            return ResponseEntity.ok(saved);
        }
        URI location = Objects.requireNonNull(URI.create("/api/clases/calificaciones/" + saved.getId()));
        return ResponseEntity.created(location).body(saved);
    }

    @DeleteMapping("/by-reserva/{reservaId}")
    public ResponseEntity<?> deleteByReserva(@PathVariable Long reservaId) {
        Optional<ClaseCalificacion> existing = repo.findByReservaId(reservaId);
        // Si no existe la calificación asociada a la reserva respondemos 404
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        // Eliminamos usando la entidad encontrada para evitar problemas con queries derivadas
        repo.delete(existing.get());
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
