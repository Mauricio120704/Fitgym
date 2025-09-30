package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Miembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MiembroRepository extends JpaRepository<Miembro, Long> {
    
    List<Miembro> findByMembresiaActiva(Boolean activa);
    
    Miembro findByEmail(String email);
}
