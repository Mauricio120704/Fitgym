package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.Equipo;
import com.integradorii.gimnasiov1.repository.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    public List<Equipo> findAll() {
        return equipoRepository.findAll();
    }

    public Optional<Equipo> findById(long id) {
        return equipoRepository.findById(id);
    }

    public Equipo save(Equipo equipo) {
        return equipoRepository.save(java.util.Objects.requireNonNull(equipo));
    }

    public void deleteById(long id) {
        equipoRepository.deleteById(id);
    }

    public List<Equipo> findByEstado(String estado) {
        return equipoRepository.findByEstado(estado);
    }

    public List<Equipo> findByTipo(String tipo) {
        return equipoRepository.findByTipo(tipo);
    }

    public List<Equipo> findByUbicacion(String ubicacion) {
        return equipoRepository.findByUbicacion(ubicacion);
    }

    public List<Equipo> findEquiposConMantenimientoPendiente() {
        return equipoRepository.findEquiposConMantenimientoPendiente(LocalDate.now().plusDays(7));
    }

    public List<Equipo> findEquiposEnMantenimiento() {
        return equipoRepository.findEquiposEnMantenimiento();
    }

    public List<Equipo> buscarEquiposPorTermino(String termino) {
        return equipoRepository.buscarEquiposPorTermino(termino);
    }

    public Long contarEquiposPorEstado(String estado) {
        return equipoRepository.contarEquiposPorEstado(estado);
    }

    public List<Equipo> findEquiposOrdenadosPorProximoMantenimiento() {
        return equipoRepository.findEquiposOrdenadosPorProximoMantenimiento();
    }

    public Equipo actualizarEstado(long id, String nuevoEstado) {
        Optional<Equipo> equipoOpt = equipoRepository.findById(id);
        if (equipoOpt.isPresent()) {
            Equipo equipo = equipoOpt.get();
            equipo.setEstado(nuevoEstado);
            return equipoRepository.save(equipo);
        }
        return null;
    }

    public Equipo actualizarProximoMantenimiento(long id, LocalDate proximoMantenimiento) {
        Optional<Equipo> equipoOpt = equipoRepository.findById(id);
        if (equipoOpt.isPresent()) {
            Equipo equipo = equipoOpt.get();
            equipo.setProximoMantenimiento(proximoMantenimiento);
            return equipoRepository.save(equipo);
        }
        return null;
    }

    // Pageable variants
    public Page<Equipo> findAll(Pageable pageable) {
        return equipoRepository.findAll(java.util.Objects.requireNonNull(pageable));
    }

    public Page<Equipo> findByEstado(String estado, Pageable pageable) {
        return equipoRepository.findByEstado(estado, java.util.Objects.requireNonNull(pageable));
    }

    public Page<Equipo> findByTipo(String tipo, Pageable pageable) {
        return equipoRepository.findByTipo(tipo, java.util.Objects.requireNonNull(pageable));
    }

    public Page<Equipo> buscarEquiposPorTermino(String termino, Pageable pageable) {
        return equipoRepository.buscarEquiposPorTermino(termino, java.util.Objects.requireNonNull(pageable));
    }
}
