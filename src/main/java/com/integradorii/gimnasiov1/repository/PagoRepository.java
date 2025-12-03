package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("select p from Pago p join p.deportista d where d.email = :email order by p.fecha desc")
    List<Pago> findByEmail(@Param("email") String email);

    @Query("select p from Pago p join p.deportista d where d.email = :email and (:estado = 'todos' or p.estado = :estado) and (" +
            " (:buscar is null or :buscar = '' or lower(p.planServicio) like lower(concat('%', :buscar, '%')) or " +
            "  lower(p.metodoPago) like lower(concat('%', :buscar, '%')) or lower(p.codigoPago) like lower(concat('%', :buscar, '%')) )" +
            ") order by p.fecha desc")
    List<Pago> searchByEmail(@Param("email") String email, @Param("estado") String estado, @Param("buscar") String buscar);

    @Query("select coalesce(sum(p.monto),0) from Pago p join p.deportista d where d.email = :email and p.estado = 'Completado' and function('date_part','year', p.fecha) = function('date_part','year', current_date)")
    Double totalAnioCompletado(@Param("email") String email);

    /**
     * Find completed payments within a date range
     */
    @Query("SELECT p FROM Pago p WHERE p.estado = 'Completado' AND p.fecha BETWEEN :startDate AND :endDate ORDER BY p.fecha")
    List<Pago> findCompletedPaymentsByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Search all payments for admin with filters by status and search term
     */
    @Query("SELECT p FROM Pago p JOIN p.deportista d WHERE " +
           "(:estado = 'todos' OR p.estado = :estado) AND " +
           "(LOWER(d.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           "LOWER(d.apellido) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           "LOWER(d.dni) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           "LOWER(d.email) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           "LOWER(p.codigoPago) LIKE LOWER(CONCAT('%', :buscar, '%'))) " +
           "ORDER BY p.fecha DESC")
    List<Pago> searchAdmin(
        @Param("estado") String estado,
        @Param("buscar") String buscar
    );
}
