package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.dto.AsistenciaDTO;
import com.integradorii.gimnasiov1.model.Asistencia;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Locker;
import com.integradorii.gimnasiov1.model.EstadoLocker;
import com.integradorii.gimnasiov1.repository.AsistenciaRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.LockerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private PersonaRepository personaRepository;
    
    @Autowired
    private LockerRepository lockerRepository;

    @Transactional(readOnly = true)
    public AsistenciaDTO buscarDatosAsistenciaPorDni(String dni) {
        // Buscar la persona por DNI
        Optional<Persona> personaOpt = personaRepository.findByDni(dni);
        if (personaOpt.isEmpty()) {
            return null;
        }
        
        Persona persona = personaOpt.get();
        
        // Verificar si tiene un check-in sin check-out
        List<Asistencia> asistenciasActivas = asistenciaRepository.findAsistenciaActivaByPersonaId(persona.getId());
        boolean tieneCheckinActivo = !asistenciasActivas.isEmpty();

        // Crear DTO con los datos de la persona
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setId(persona.getId());
        dto.setNombreCompleto(persona.getNombre() + " " + persona.getApellido());
        dto.setDni(persona.getDni());
        dto.setTieneCheckinActivo(tieneCheckinActivo);
        
        if (tieneCheckinActivo) {
            Asistencia ultimoCheckin = asistenciasActivas.get(0);
            dto.setFechaIngreso(ultimoCheckin.getFechaHoraIngreso());
        }

        // Obtener información de la membresía
        boolean membresiaActiva = persona.getMembresiaActiva() != null && persona.getMembresiaActiva();
        dto.setEstadoMembresia(membresiaActiva ? "Activa" : "Inactiva");

        // Establecer la URL de la foto (ajusta según cómo manejes las imágenes)
        dto.setFotoUrl("https://ui-avatars.com/api/?name=" + 
                       persona.getNombre().replace(" ", "+") + "+" + 
                       persona.getApellido().replace(" ", "+") + 
                       "&background=random");

        return dto;
    }

    @Transactional
    public Map<String, Object> registrarAsistencia(String dni) {
        // Buscar la persona por DNI
        Persona persona = personaRepository.findByDni(dni)
            .orElseThrow(() -> new RuntimeException("No se encontró el deportista con DNI: " + dni));

        // Verificar si la membresía está activa
        if (persona.getMembresiaActiva() == null || !persona.getMembresiaActiva()) {
            throw new RuntimeException("La membresía no está activa");
        }

        // Verificar si ya tiene un check-in sin check-out
        List<Asistencia> asistenciasActivas = asistenciaRepository.findAsistenciaActivaByPersonaId(persona.getId());
        
        Map<String, Object> respuesta = new HashMap<>();
        
        if (!asistenciasActivas.isEmpty()) {
            // Registrar salida (check-out)
            Asistencia asistencia = asistenciasActivas.get(0);
            asistencia.setFechaHoraSalida(LocalDateTime.now());
            asistenciaRepository.save(asistencia);
            
            // Liberar el locker asignado al deportista si lo tiene
            liberarLockerDeportista(persona);
            
            // Calcular tiempo de estadía
            long minutos = asistencia.getTiempoEstadiaMinutos();
            long horas = minutos / 60;
            minutos = minutos % 60;
            
            respuesta.put("tipo", "salida");
            respuesta.put("mensaje", "Salida registrada exitosamente");
            respuesta.put("tiempoEstadia", String.format("%d horas y %d minutos", horas, minutos));
            respuesta.put("fechaIngreso", asistencia.getFechaHoraIngreso());
            respuesta.put("fechaSalida", asistencia.getFechaHoraSalida());
        } else {
            // Registrar ingreso (check-in)
            Asistencia asistencia = new Asistencia();
            asistencia.setPersona(persona);
            asistencia.setFechaHoraIngreso(LocalDateTime.now());
            asistenciaRepository.save(asistencia);
            
            respuesta.put("tipo", "ingreso");
            respuesta.put("mensaje", "Ingreso registrado exitosamente");
            respuesta.put("fechaIngreso", asistencia.getFechaHoraIngreso());
        }
        
        return respuesta;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerAsistenciasRecientes() {
        // Obtener las últimas 50 asistencias ordenadas por fecha de ingreso descendente
        return asistenciaRepository.findTop50ByOrderByFechaHoraIngresoDesc().stream()
            .map(asistencia -> {
                try {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", asistencia.getId());

                    Persona persona = asistencia.getPersona();
                    String nombre = persona != null ? Objects.toString(persona.getNombre(), "") : "";
                    String apellido = persona != null ? Objects.toString(persona.getApellido(), "") : "";

                    dto.put("nombreCompleto", (nombre + " " + apellido).trim());
                    dto.put("dni", persona != null ? persona.getDni() : null);
                    dto.put("fechaHoraIngreso", asistencia.getFechaHoraIngreso());
                    dto.put("fechaHoraSalida", asistencia.getFechaHoraSalida());
                    return dto;
                } catch (Exception e) {
                    System.err.println("Error al procesar asistencia ID " + asistencia.getId() + ": " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * Registra la salida de un deportista cuando se libera su locker
     * @param personaId ID del deportista
     */
    @Transactional
    public void registrarSalidaPorLocker(Long personaId) {
        // Buscar la asistencia activa (check-in sin check-out)
        List<Asistencia> asistenciasActivas = asistenciaRepository.findAsistenciaActivaByPersonaId(personaId);
        
        if (!asistenciasActivas.isEmpty()) {
            // Tomar la asistencia más reciente
            Asistencia asistencia = asistenciasActivas.get(0);
            // Registrar la hora de salida
            asistencia.setFechaHoraSalida(LocalDateTime.now());
            asistenciaRepository.save(asistencia);
            
            // También podemos registrar en los logs
            System.out.println("Registrada salida automática para el deportista ID: " + personaId);
        }
    }
    
    /**
     * Libera el locker asignado a un deportista cuando realiza el check-out
     * @param persona El deportista que está realizando el check-out
     */
    private void liberarLockerDeportista(Persona persona) {
        try {
            // Buscar lockers asignados a la persona
            List<Locker> lockersAsignados = lockerRepository.findByPersonaAsignadaId(persona.getId());
            
            // Liberar todos los lockers asignados
            for (Locker locker : lockersAsignados) {
                locker.setEstado(EstadoLocker.DISPONIBLE);
                locker.setPersonaAsignada(null);
                locker.setFechaAsignacion(null);
                lockerRepository.save(locker);
            }
        } catch (Exception e) {
            // Registrar el error pero no interrumpir el flujo principal
            System.err.println("Error al liberar locker para el deportista " + persona.getDni() + ": " + e.getMessage());
        }
    }
}
