package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.Miembro;
import com.integradorii.gimnasiov1.repository.MiembroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MiembroService {
    
    @Autowired
    private MiembroRepository miembroRepository;
    
    public List<Miembro> obtenerTodos() {
        return miembroRepository.findAll();
    }
    
    public Optional<Miembro> obtenerPorId(Long id) {
        return miembroRepository.findById(id);
    }
    
    public Miembro guardar(Miembro miembro) {
        return miembroRepository.save(miembro);
    }
    
    public void eliminar(Long id) {
        miembroRepository.deleteById(id);
    }
    
    public List<Miembro> obtenerMiembrosActivos() {
        return miembroRepository.findByMembresiaActiva(true);
    }
}
