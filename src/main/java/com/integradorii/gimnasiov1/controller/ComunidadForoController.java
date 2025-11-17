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

/**
 * API de foro de la comunidad.
 *
 * Pensado para ser consumido desde la vista de comunidad del deportista a través
 * de peticiones AJAX (por ejemplo, en un tabpanel con pestañas de "Foro").
 *
 * Responsabilidades principales:
 * - Listar las últimas publicaciones activas.
 * - Permitir crear nuevas publicaciones.
 * - Permitir responder a publicaciones existentes.
 * - Devolver estructuras planas (Map<String,Object>) fáciles de renderizar en la UI.
 */
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

    /**
     * GET /api/comunidad/foro/posts
     *
     * Devuelve las últimas 50 publicaciones activas del foro, ordenadas por fecha
     * de creación descendente. Cada post se serializa con sus respuestas activas
     * para que la UI pueda renderizar el árbol de comentarios sin llamadas
     * adicionales.
     */
    @GetMapping("/posts")
    public List<Map<String, Object>> listarPosts() {
        List<ComunidadPost> posts = postRepository.findTop50ByActivoTrueOrderByCreadoEnDesc();
        return posts.stream().map(this::toPostDto).collect(Collectors.toList());
    }

    /**
     * POST /api/comunidad/foro/posts
     *
     * Crea una nueva publicación en el foro para el deportista autenticado.
     *
     * Reglas de validación:
     * - El usuario debe estar autenticado y existir en la tabla personas.
     * - El contenido no puede estar vacío.
     * - Se limita el tamaño máximo del contenido a 1000 caracteres para evitar
     *   textos excesivamente largos.
     */
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

    /**
     * POST /api/comunidad/foro/posts/{postId}/respuestas
     *
     * Registra una respuesta a una publicación existente.
     *
     * Reglas de validación:
     * - Usuario autenticado y existente.
     * - La publicación debe existir y estar activa.
     * - El contenido no puede estar vacío.
     * - Se limita a 800 caracteres por respuesta.
     */
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

    /**
     * Transforma una entidad `ComunidadPost` en un DTO basado en `Map`.
     *
     * Incluye:
     * - Datos básicos del autor (nombre completo, iniciales).
     * - Contenido del post.
     * - Fecha formateada para mostrar en la UI.
     * - Lista de respuestas activas ya serializadas (para un tabpanel o componente
     *   de hilo de conversación).
     */
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

    /**
     * Transforma una entidad `ComunidadRespuesta` en un DTO basado en `Map`.
     *
     * Incluye datos del autor, contenido y fecha formateada para que el frontend
     * pueda renderizar cada respuesta como parte del hilo de comentarios.
     */
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

    /**
     * Obtiene de forma segura la inicial de un texto (nombre o apellido).
     *
     * Si la cadena es nula o vacía, devuelve una cadena vacía, permitiendo
     * que el frontend muestre un placeholder genérico (por ejemplo "??").
     */
    private String safeInitial(String s) {
        return (s != null && !s.isBlank()) ? s.substring(0, 1).toUpperCase() : "";
    }
}
