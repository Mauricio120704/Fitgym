package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Visitante;
import com.integradorii.gimnasiov1.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, Long> {
    
    // Buscar visitantes por documento de identidad
    List<Visitante> findByDocumentoIdentidad(String documentoIdentidad);
    
    // Buscar visitantes activos (sin marcar salida)
    List<Visitante> findByEstado(String estado);
    
    // Buscar visitantes por rango de fechas
    @Query("SELECT v FROM Visitante v WHERE v.fechaHoraIngreso BETWEEN :fechaInicio AND :fechaFin")
    List<Visitante> findByRangoFechas(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
    
    // Buscar por código de pase
    Visitante findByCodigoPase(String codigoPase);
    
    // Contar visitantes activos
    Long countByEstado(String estado);
    
    // Buscar invitados por persona (miembro que invita)
    List<Visitante> findByInvitadoPorPersona(Persona persona);
    
    // Buscar invitados activos por persona
    List<Visitante> findByInvitadoPorPersonaAndEstado(Persona persona, String estado);
    
    // Contar invitados por persona
    Long countByInvitadoPorPersona(Persona persona);
}
