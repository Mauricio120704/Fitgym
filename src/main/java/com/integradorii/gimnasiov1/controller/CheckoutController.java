package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Pago;
import com.integradorii.gimnasiov1.model.Persona;
import com.integradorii.gimnasiov1.model.Plan;
import com.integradorii.gimnasiov1.model.Suscripcion;
import com.integradorii.gimnasiov1.repository.PagoRepository;
import com.integradorii.gimnasiov1.repository.PersonaRepository;
import com.integradorii.gimnasiov1.repository.PlanRepository;
import com.integradorii.gimnasiov1.repository.SuscripcionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Controller
public class CheckoutController {

    private final PersonaRepository personaRepository;
    private final PlanRepository planRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PagoRepository pagoRepository;

    public CheckoutController(PersonaRepository personaRepository,
                              PlanRepository planRepository,
                              SuscripcionRepository suscripcionRepository,
                              PagoRepository pagoRepository) {
        this.personaRepository = personaRepository;
        this.planRepository = planRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.pagoRepository = pagoRepository;
    }

    @PostMapping("/checkout/pagar")
    public String pagar(@RequestParam String usuario,
                        @RequestParam String plan,
                        @RequestParam String periodo,
                        @RequestParam String precio) {
        // 1) Persona
        Persona p = personaRepository.findByEmail(usuario).orElse(null);
        if (p == null) {
            // Si por alguna razón no existe, no interrumpimos: redirigir a registro
            return "redirect:/registro";
        }

        // 2) Plan (buscar por nombre, crear si no existe)
        Plan planEntity = planRepository.findByNombre(plan).orElseGet(() -> {
            Plan np = new Plan();
            np.setNombre(plan);
            // precio puede venir con separadores, normalizar
            np.setPrecio(parsePrecio(precio));
            np.setFrecuencia(capitalize(periodo));
            return planRepository.save(np);
        });

        // 3) Suscripción Activa
        Suscripcion s = new Suscripcion();
        s.setDeportista(p);
        s.setPlan(planEntity);
        s.setEstado("Activa");
        s.setFechaInicio(LocalDate.now());
        s.setProximoPago(calcularProximoPago(periodo));
        suscripcionRepository.save(s);

        // 4) Pago Completado
        Pago pago = new Pago();
        pago.setCodigoPago(generarCodigo());
        pago.setDeportista(p);
        pago.setFecha(LocalDate.now());
        pago.setMetodoPago("Tarjeta de Crédito");
        pago.setMonto(parsePrecio(precio));
        pago.setEstado("Completado");
        pago.setPlanServicio(planEntity.getNombre());
        pagoRepository.save(pago);

        // 5) Redirigir al perfil del usuario registrado
        return "redirect:/perfil?usuario=" + usuario;
    }

    private String generarCodigo() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private Double parsePrecio(String precio) {
        try {
            String norm = precio.replace("S/", "").replace(" ", "").replace(",", "");
            return Double.parseDouble(norm);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        String lower = s.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private LocalDate calcularProximoPago(String periodo) {
        if (periodo != null && periodo.toLowerCase(Locale.ROOT).contains("anual")) {
            return LocalDate.now().plusYears(1);
        }
        return LocalDate.now().plusMonths(1);
    }
}
