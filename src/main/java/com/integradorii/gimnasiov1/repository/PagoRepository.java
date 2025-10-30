package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.dto.MembresiaIngresoDTO;
import com.integradorii.gimnasiov1.dto.PaymentSummaryDTO;
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

    // Admin search over all pagos
    @Query("select p from Pago p where (:estado = 'todos' or p.estado = :estado) and (" +
            " (:buscar is null or :buscar = '' or lower(p.planServicio) like lower(concat('%', :buscar, '%')) or " +
            "  lower(p.metodoPago) like lower(concat('%', :buscar, '%')) or lower(p.codigoPago) like lower(concat('%', :buscar, '%')) )" +
            ") order by p.fecha desc")
    List<Pago> searchAdmin(@Param("estado") String estado, @Param("buscar") String buscar);
    
    /**
     * Find completed payments within a date range
     */
    @Query("SELECT p FROM Pago p WHERE p.estado = 'Completado' AND p.fecha BETWEEN :startDate AND :endDate ORDER BY p.fecha")
    List<Pago> findCompletedPaymentsByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find completed membership payments within a date range
     */
    @Query("SELECT new com.integradorii.gimnasiov1.dto.MembresiaIngresoDTO(" +
           "p.id, p.planServicio, CONCAT(d.nombre, ' ', d.apellido), p.fecha, p.monto, p.metodoPago) " +
           "FROM Pago p JOIN p.deportista d " +
           "WHERE p.estado = 'Completado' AND p.planServicio LIKE '%Membresía%' " +
           "AND p.fecha BETWEEN :startDate AND :endDate " +
           "ORDER BY p.fecha DESC")
    List<MembresiaIngresoDTO> findMembresiaPaymentsByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Get payment summary by date range
     */
    @Query("SELECT " +
           "new com.integradorii.gimnasiov1.dto.PaymentSummaryDTO(\n                p.planServicio, \n                COUNT(p), \n                SUM(p.monto), \n                p.metodoPago\n           ) " +
           "FROM Pago p " +
           "WHERE p.estado = 'Completado' AND p.fecha BETWEEN :startDate AND :endDate " +
           "GROUP BY p.planServicio, p.metodoPago")
    List<PaymentSummaryDTO> getPaymentSummaryByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
