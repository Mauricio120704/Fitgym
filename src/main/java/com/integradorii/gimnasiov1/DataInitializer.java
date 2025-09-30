package com.integradorii.gimnasiov1;

import com.integradorii.gimnasiov1.model.Miembro;
import com.integradorii.gimnasiov1.model.Pago;
import com.integradorii.gimnasiov1.repository.MiembroRepository;
import com.integradorii.gimnasiov1.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDate;

//@Component  // Deshabilitado - Los datos ya están en PostgreSQL
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private MiembroRepository miembroRepository;
    
    @Autowired
    private PagoRepository pagoRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Crear algunos miembros de ejemplo
        Miembro m1 = new Miembro("Juan", "Pérez", "juan.perez@email.com", "555-0101");
        m1.setFechaRegistro(LocalDate.now().minusMonths(6));
        
        Miembro m2 = new Miembro("María", "González", "maria.gonzalez@email.com", "555-0102");
        m2.setFechaRegistro(LocalDate.now().minusMonths(3));
        
        Miembro m3 = new Miembro("Carlos", "Rodríguez", "carlos.rodriguez@email.com", "555-0103");
        m3.setFechaRegistro(LocalDate.now().minusMonths(1));
        
        Miembro m4 = new Miembro("Ana", "Martínez", "ana.martinez@email.com", "555-0104");
        m4.setFechaRegistro(LocalDate.now().minusWeeks(2));
        m4.setMembresiaActiva(false);
        
        miembroRepository.save(m1);
        miembroRepository.save(m2);
        miembroRepository.save(m3);
        miembroRepository.save(m4);
        
        System.out.println("✅ Datos iniciales cargados: " + miembroRepository.count() + " miembros");
        
        // Crear pagos de ejemplo
        Pago p1 = new Pago("PAY-001", "Membresía Premium", "Tarjeta de Crédito", 899.00, "Completado");
        p1.setFecha(LocalDate.of(2024, 1, 14));
        p1.setMiembro(m1);
        
        Pago p2 = new Pago("PAY-002", "Membresía Premium", "Tarjeta de Crédito", 899.00, "Completado");
        p2.setFecha(LocalDate.of(2023, 12, 14));
        p2.setMiembro(m1);
        
        Pago p3 = new Pago("PAY-003", "Membresía Premium", "Transferencia", 899.00, "Completado");
        p3.setFecha(LocalDate.of(2023, 11, 14));
        p3.setMiembro(m2);
        
        Pago p4 = new Pago("PAY-004", "Entrenamiento Personal", "Tarjeta de Débito", 1209.00, "Pendiente");
        p4.setFecha(LocalDate.of(2024, 1, 19));
        p4.setMiembro(m3);
        
        pagoRepository.save(p1);
        pagoRepository.save(p2);
        pagoRepository.save(p3);
        pagoRepository.save(p4);
        
        System.out.println("✅ Datos de pagos cargados: " + pagoRepository.count() + " pagos");
    }
}
