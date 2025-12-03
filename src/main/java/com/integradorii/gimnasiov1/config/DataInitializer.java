package com.integradorii.gimnasiov1.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    // Temporalmente desactivado para evitar saturación de conexiones al startup
    // @Bean
    // CommandLineRunner seedLockers(LockerRepository lockerRepository) {
    //     return args -> {
    //         if (lockerRepository.count() == 0) {
    //             // Crear 24 lockers disponibles por defecto
    //             for (int i = 1; i <= 24; i++) {
    //                 Locker l = new Locker();
    //                 l.setNumero(String.valueOf(i));
    //                 l.setEstado(EstadoLocker.DISPONIBLE);
    //                 lockerRepository.save(l);
    //             }
    //         }
    //     };
    // }
}
