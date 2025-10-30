package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token válido por email y token
     */
    Optional<PasswordResetToken> findByEmailAndToken(String email, String token);

    /**
     * Busca el token más reciente por email que no haya sido usado
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.email = :email AND t.usado = false ORDER BY t.fechaCreacion DESC")
    Optional<PasswordResetToken> findLatestValidTokenByEmail(@Param("email") String email);

    /**
     * Elimina tokens expirados
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.fechaExpiracion < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Marca todos los tokens previos de un email como usados
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usado = true WHERE t.email = :email AND t.usado = false")
    void invalidateAllTokensByEmail(@Param("email") String email);
}
