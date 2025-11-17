package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.ComunidadPost;
import com.integradorii.gimnasiov1.model.ComunidadRespuesta;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.ComunidadPostRepository;
import com.integradorii.gimnasiov1.repository.ComunidadRespuestaRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comunidad/foro")
@PreAuthorize("hasRole('CLIENTE')")
public class ComunidadForoController {

    private final ComunidadPostRepository postRepository;
    private final ComunidadRespuestaRepository respuestaRepository;
    private final PersonaRepository personaRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ComunidadForoController(ComunidadPostRepository postRepository,
                                   ComunidadRespuestaRepository respuestaRepository,
                                   PersonaRepository personaRepository) {
        this.postRepository = postRepository;
        this.respuestaRepository = respuestaRepository;
        this.personaRepository = personaRepository;
    }

    @GetMapping("/posts")
    public List<Map<String, Object>> listarPosts() {
        List<ComunidadPost> posts = postRepository.findTop50ByActivoTrueOrderByCreadoEnDesc();
        return posts.stream().map(this::toPostDto).collect(Collectors.toList());
    }

    @PostMapping("/posts")
    public ResponseEntity<Map<String, Object>> crearPost(@RequestParam("contenido") String contenido,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> resp = new HashMap<>();

        if (userDetails == null) {
            resp.put("success", false);
            resp.put("message", "Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }

        Persona deportista = personaRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (deportista == null) {
            resp.put("success", false);
            resp.put("message", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        String trimmed = contenido != null ? contenido.trim() : "";
        if (trimmed.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "El contenido no puede estar vacío");
            return ResponseEntity.badRequest().body(resp);
        }
        if (trimmed.length() > 1000) {
            trimmed = trimmed.substring(0, 1000);
        }

        ComunidadPost post = new ComunidadPost();
        post.setDeportista(deportista);
        post.setContenido(trimmed);
        post.setCreadoEn(LocalDateTime.now());
        post.setActualizadoEn(LocalDateTime.now());
        post.setActivo(Boolean.TRUE);

        post = postRepository.save(post);

        resp.put("success", true);
        resp.put("post", toPostDto(post));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/posts/{postId}/respuestas")
    public ResponseEntity<Map<String, Object>> crearRespuesta(@PathVariable("postId") Long postId,
                                                              @RequestParam("contenido") String contenido,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> resp = new HashMap<>();

        if (userDetails == null) {
            resp.put("success", false);
            resp.put("message", "Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }

        Persona deportista = personaRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (deportista == null) {
            resp.put("success", false);
            resp.put("message", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        ComunidadPost post = postRepository.findById(postId).orElse(null);
        if (post == null || Boolean.FALSE.equals(post.getActivo())) {
            resp.put("success", false);
            resp.put("message", "Publicación no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        String trimmed = contenido != null ? contenido.trim() : "";
        if (trimmed.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "El contenido no puede estar vacío");
            return ResponseEntity.badRequest().body(resp);
        }
        if (trimmed.length() > 800) {
            trimmed = trimmed.substring(0, 800);
        }

        ComunidadRespuesta respuesta = new ComunidadRespuesta();
        respuesta.setPost(post);
        respuesta.setDeportista(deportista);
        respuesta.setContenido(trimmed);
        respuesta.setCreadoEn(LocalDateTime.now());
        respuesta.setActualizadoEn(LocalDateTime.now());
        respuesta.setActivo(Boolean.TRUE);

        respuesta = respuestaRepository.save(respuesta);

        resp.put("success", true);
        resp.put("respuesta", toRespuestaDto(respuesta));
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toPostDto(ComunidadPost post) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", post.getId());
        Persona autor = post.getDeportista();
        if (autor != null) {
            String nombre = autor.getNombreCompleto();
            m.put("autorNombre", nombre);
            String iniciales = safeInitial(autor.getNombre()) + safeInitial(autor.getApellido());
            m.put("autorIniciales", iniciales.trim().isEmpty() ? "??" : iniciales.trim());
        }
        m.put("contenido", post.getContenido());
        if (post.getCreadoEn() != null) {
            m.put("creadoEnTexto", post.getCreadoEn().format(FORMATTER));
        }

        List<Map<String, Object>> respuestas = post.getRespuestas() == null ? List.of() :
                post.getRespuestas().stream()
                        .filter(r -> Boolean.TRUE.equals(r.getActivo()))
                        .map(this::toRespuestaDto)
                        .collect(Collectors.toList());
        m.put("respuestas", respuestas);
        return m;
    }

    private Map<String, Object> toRespuestaDto(ComunidadRespuesta r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        Persona autor = r.getDeportista();
        if (autor != null) {
            String nombre = autor.getNombreCompleto();
            m.put("autorNombre", nombre);
            String iniciales = safeInitial(autor.getNombre()) + safeInitial(autor.getApellido());
            m.put("autorIniciales", iniciales.trim().isEmpty() ? "??" : iniciales.trim());
        }
        m.put("contenido", r.getContenido());
        if (r.getCreadoEn() != null) {
            m.put("creadoEnTexto", r.getCreadoEn().format(FORMATTER));
        }
        return m;
    }

    private String safeInitial(String s) {
        return (s != null && !s.isBlank()) ? s.substring(0, 1).toUpperCase() : "";
    }
}
