package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.VerificationToken;
import com.integradorii.gimnasiov1.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    
    Optional<VerificationToken> findByToken(String token);
    
    Optional<VerificationToken> findByPersona(Persona persona);
    
    void deleteByPersona(Persona persona);
}
