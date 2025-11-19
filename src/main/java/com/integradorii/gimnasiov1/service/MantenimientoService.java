package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.Mantenimiento;
import com.integradorii.gimnasiov1.repository.MantenimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MantenimientoService {

    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    public List<Mantenimiento> findAll() {
        return mantenimientoRepository.findAll();
    }

    public Optional<Mantenimiento> findById(Long id) {
        return mantenimientoRepository.findById(id);
    }

    public Mantenimiento save(Mantenimiento mantenimiento) {
        if (mantenimiento.getFechaRegistro() == null) {
            mantenimiento.setFechaRegistro(LocalDateTime.now());
        }
        return mantenimientoRepository.save(mantenimiento);
    }

    public void deleteById(Long id) {
        mantenimientoRepository.deleteById(id);
    }

    public List<Mantenimiento> findByEquipoId(Long equipoId) {
        return mantenimientoRepository.findByEquipoId(equipoId);
    }

    public List<Mantenimiento> findByEstado(String estado) {
        return mantenimientoRepository.findByEstado(estado);
    }

    public List<Mantenimiento> findByTipoServicio(String tipoServicio) {
        return mantenimientoRepository.findByTipoServicio(tipoServicio);
    }

    public List<Mantenimiento> findByTecnicoResponsable(String tecnicoResponsable) {
        return mantenimientoRepository.findByTecnicoResponsable(tecnicoResponsable);
    }

    public List<Mantenimiento> findByFechaServicioBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        return mantenimientoRepository.findByFechaServicioBetween(fechaInicio, fechaFin);
    }

    public List<Mantenimiento> findHistorialMantenimientoPorEquipo(Long equipoId) {
        return mantenimientoRepository.findHistorialMantenimientoPorEquipo(equipoId);
    }

    public Long contarMantenimientosPorEstado(String estado) {
        return mantenimientoRepository.contarMantenimientosPorEstado(estado);
    }

    public List<Mantenimiento> findMantenimientosProgramadosPendientes() {
        return mantenimientoRepository.findMantenimientosProgramadosPendientes(LocalDate.now());
    }

    public List<Mantenimiento> buscarMantenimientosPorTermino(String termino) {
        return mantenimientoRepository.buscarMantenimientosPorTermino(termino);
    }

    public Mantenimiento actualizarEstado(Long id, String nuevoEstado) {
        Optional<Mantenimiento> mantenimientoOpt = mantenimientoRepository.findById(id);
        if (mantenimientoOpt.isPresent()) {
            Mantenimiento mantenimiento = mantenimientoOpt.get();
            mantenimiento.setEstado(nuevoEstado);
            return mantenimientoRepository.save(mantenimiento);
        }
        return null;
    }
}
