package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.Reclamo;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.repository.ReclamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReclamoService {
    
    private final ReclamoRepository reclamoRepository;
    
    public ReclamoService(ReclamoRepository reclamoRepository) {
        this.reclamoRepository = reclamoRepository;
    }
    
    @Transactional
    public Reclamo crearReclamo(Persona deportista, String categoria, String asunto, 
                                String descripcion, String prioridad) {
        
        // Validaciones
        if (deportista == null) {
            throw new IllegalArgumentException("El deportista no puede ser nulo");
        }
        
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        
        if (asunto == null || asunto.trim().isEmpty()) {
            throw new IllegalArgumentException("El asunto es obligatorio");
        }
        
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        
        // Crear el reclamo
        Reclamo reclamo = new Reclamo();
        reclamo.setDeportista(deportista);
        reclamo.setCategoria(categoria.trim());
        reclamo.setAsunto(asunto.trim());
        reclamo.setDescripcion(descripcion.trim());
        reclamo.setEstado("En proceso");
        reclamo.setActivo(true);
        
        // Establecer prioridad (por defecto "Normal" si no se especifica)
        if (prioridad != null && !prioridad.trim().isEmpty()) {
            reclamo.setPrioridad(prioridad.trim());
        } else {
            reclamo.setPrioridad("Normal");
        }
        
        return reclamoRepository.save(reclamo);
    }
    
    @Transactional(readOnly = true)
    public List<Reclamo> obtenerReclamosPorDeportista(Persona deportista) {
        return reclamoRepository.findByDeportistaAndActivoTrueOrderByFechaCreacionDesc(deportista);
    }
    
    @Transactional(readOnly = true)
    public List<Reclamo> obtenerTodosLosReclamos() {
        return reclamoRepository.findByActivoTrueOrderByFechaCreacionDesc();
    }
    
    @Transactional(readOnly = true)
    public List<Reclamo> obtenerReclamosPorEstado(String estado) {
        return reclamoRepository.findByEstadoAndActivoTrueOrderByFechaCreacionDesc(estado);
    }
    
    @Transactional(readOnly = true)
    public long contarReclamosPorDeportista(Persona deportista) {
        return reclamoRepository.countByDeportistaAndActivoTrue(deportista);
    }
}
