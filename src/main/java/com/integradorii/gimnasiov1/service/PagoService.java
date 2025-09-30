package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.Pago;
import com.integradorii.gimnasiov1.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PagoService {
    
    @Autowired
    private PagoRepository pagoRepository;
    
    public List<Pago> obtenerTodos() {
        return pagoRepository.findByOrderByFechaDesc();
    }
    
    public Optional<Pago> obtenerPorId(Long id) {
        return pagoRepository.findById(id);
    }
    
    public Pago guardar(Pago pago) {
        return pagoRepository.save(pago);
    }
    
    public void eliminar(Long id) {
        pagoRepository.deleteById(id);
    }
    
    public List<Pago> obtenerPorEstado(String estado) {
        return pagoRepository.findByEstado(estado);
    }
    
    public Double calcularTotalAnio(int anio) {
        return obtenerTodos().stream()
                .filter(p -> p.getFecha().getYear() == anio && "Completado".equals(p.getEstado()))
                .mapToDouble(Pago::getMonto)
                .sum();
    }
    
    public long contarPagosAnio(int anio) {
        return obtenerTodos().stream()
                .filter(p -> p.getFecha().getYear() == anio && "Completado".equals(p.getEstado()))
                .count();
    }
}
