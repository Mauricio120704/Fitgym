package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    List<Pago> findByEstado(String estado);
    
    List<Pago> findByOrderByFechaDesc();
    
    Pago findByCodigoPago(String codigoPago);
}
