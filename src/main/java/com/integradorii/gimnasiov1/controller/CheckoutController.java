package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.*;
import com.integradorii.gimnasiov1.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador de Checkout - Proceso de pago de membresías
 * Ruta: /checkout | Acceso: Público (para nuevos registros)
 * Tablas: personas, planes, suscripciones, pagos
 * Proceso transaccional: crea/actualiza suscripción y registra pago
 */
@Controller
@RequestMapping("/checkout")
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

    /**
     * GET /checkout - Muestra página de pago
     * Parámetros: plan, periodo (mensual/anual), precio, usuario (email)
     * Calcula ahorro si es plan anual
     */
    @GetMapping
    public String mostrarCheckout(
            @RequestParam String plan,
            @RequestParam String periodo,
            @RequestParam String precio,
            @RequestParam(required = false) String usuario,
            Model model) {
        
        model.addAttribute("planNombre", plan);
        model.addAttribute("periodo", periodo);
        model.addAttribute("precio", precio);
        model.addAttribute("usuario", usuario);
        
        // Calcular el ahorro si es anual
        if ("anual".equalsIgnoreCase(periodo)) {
            double precioMensual = parsePrecio(precio) / 12;
            double ahorroMensual = 0;
            
            // Ajustar según el plan
            switch (plan.toLowerCase()) {
                case "básico":
                    ahorroMensual = 49.90 - precioMensual;
                    break;
                case "premium":
                    ahorroMensual = 79.90 - precioMensual;
                    break;
                case "elite":
                    ahorroMensual = 129.90 - precioMensual;
                    break;
            }
            
            model.addAttribute("ahorroAnual", String.format("%.2f", ahorroMensual * 12));
        }
        
        return "checkout";
    }

    /**
     * POST /checkout/pagar - Procesa pago de membresía
     * Transaccional: crea/actualiza suscripción, registra pago, activa membresía
     * Rollback automático si falla cualquier paso
     */
    @PostMapping("/pagar")
    @Transactional
    public String pagar(@RequestParam String usuario,
                       @RequestParam String plan,
                       @RequestParam String periodo,
                       @RequestParam String precio,
                       @RequestParam String nombreTitular,
                       @RequestParam String numeroTarjeta,
                       @RequestParam String fechaExpiracion,
                       @RequestParam String cvv,
                       Model model,
                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Validación básica de datos de tarjeta (además de los required del formulario)
            String numeroNormalizado = numeroTarjeta != null ? numeroTarjeta.replaceAll("\\s+", "") : "";
            boolean datosInvalidos =
                    nombreTitular == null || nombreTitular.isBlank() ||
                    numeroNormalizado.length() < 13 || numeroNormalizado.length() > 19 ||
                    fechaExpiracion == null || !fechaExpiracion.matches("\\d{2}/\\d{2}") ||
                    cvv == null || !cvv.matches("\\d{3}");

            if (datosInvalidos) {
                model.addAttribute("error", "Datos de tarjeta inválidos. Por favor, verifica la información ingresada.");
                model.addAttribute("planNombre", plan);
                model.addAttribute("periodo", periodo);
                model.addAttribute("precio", precio);
                model.addAttribute("usuario", usuario);
                return "checkout";
            }

            // Paso 1: Verificar que el deportista existe
            Persona persona = personaRepository.findByEmail(usuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuario));

            // Paso 2: Buscar plan existente o crear nuevo
            Plan planEntity = planRepository.findByNombre(plan)
                .orElseGet(() -> {
                    Plan nuevoPlan = new Plan();
                    nuevoPlan.setNombre(plan);
                    nuevoPlan.setPrecio(parsePrecio(precio));
                    nuevoPlan.setFrecuencia(periodo.equalsIgnoreCase("anual") ? "Anual" : "Mensual");
                    return planRepository.save(nuevoPlan);
                });

            // Paso 3: Crear o actualizar suscripción
            Optional<Suscripcion> suscripcionExistente = suscripcionRepository.findActiveByDeportistaId(persona.getId());
            Suscripcion suscripcion;
            
            if (suscripcionExistente.isPresent()) {
                // Actualizar suscripción existente
                suscripcion = suscripcionExistente.get();
                suscripcion.setPlan(planEntity);
                suscripcion.setEstado("Activa");
            } else {
                // Crear nueva suscripción
                suscripcion = new Suscripcion();
                suscripcion.setDeportista(persona);
                suscripcion.setPlan(planEntity);
                suscripcion.setEstado("Activa");
                suscripcion.setFechaInicio(LocalDate.now());
            }
            
            // Actualizar fechas
            suscripcion.setFechaFin(calcularFechaFin(periodo));
            suscripcion.setProximoPago(calcularProximoPago(periodo));
            
            try {
                // Guardar la suscripción
                suscripcion = suscripcionRepository.save(suscripcion);
                System.out.println("Suscripción guardada con ID: " + suscripcion.getId());
            } catch (Exception e) {
                System.err.println("Error al guardar la suscripción: " + e.getMessage());
                throw new RuntimeException("Error al guardar la suscripción: " + e.getMessage(), e);
            }

            // Paso 4: Registrar pago en historial
            Pago pago = new Pago();
            pago.setCodigoPago(generarCodigoPago());
            pago.setDeportista(persona);
            pago.setFecha(LocalDate.now());
            pago.setMetodoPago("Tarjeta de Crédito");
            pago.setMonto(parsePrecio(precio));
            pago.setEstado("Completado");
            pago.setPlanServicio(plan);
            pagoRepository.save(pago);

            // Paso 5: Activar membresía del deportista
            persona.setMembresiaActiva(true);
            personaRepository.save(persona);

            // Paso 6: Redirigir según contexto
            boolean esDeportistaAutenticado =
                    userDetails != null &&
                    userDetails.getUsername() != null &&
                    userDetails.getUsername().equalsIgnoreCase(usuario);

            if (esDeportistaAutenticado) {
                // Si el deportista ya está autenticado (flujo desde su perfil), volver a su perfil
                return "redirect:/perfil";
            } else {
                // Flujo original para nuevos registros: volver a login con mensaje
                return "redirect:/login?registroExitoso=true&mensaje=¡Pago+procesado+correctamente!+Ya+puedes+iniciar+sesión.";
            }
            
        } catch (Exception e) {
            // En caso de error, hacer rollback completo de la transacción
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            
            // Registrar el error
            System.err.println("Error al procesar el pago: " + e.getMessage());
            e.printStackTrace();
            
            // Devolver a la página de checkout con el error
            model.addAttribute("error", "Error al procesar el pago. Por favor, verifica tus datos e inténtalo nuevamente.");
            model.addAttribute("planNombre", plan);
            model.addAttribute("periodo", periodo);
            model.addAttribute("precio", precio);
            model.addAttribute("usuario", usuario);
            return "checkout";
        }
    }

    // Genera código único para el pago (formato: PAY-XXXXXXXX)
    private String generarCodigoPago() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    // Convierte string de precio (ej: "S/ 49.90") a Double
    private Double parsePrecio(String precio) {
        try {
            String norm = precio.replace("S/", "").replace(" ", "").replace(",", "");
            return Double.parseDouble(norm);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // Calcula fecha de fin de suscripción según periodo
    private LocalDate calcularFechaFin(String periodo) {
        LocalDate hoy = LocalDate.now();
        if ("anual".equalsIgnoreCase(periodo)) {
            return hoy.plusYears(1);
        } else {
            return hoy.plusMonths(1);
        }
    }

    // Calcula fecha del próximo pago según periodo
    private LocalDate calcularProximoPago(String periodo) {
        LocalDate hoy = LocalDate.now();
        if ("anual".equalsIgnoreCase(periodo)) {
            return hoy.plusYears(1);
        } else {
            return hoy.plusMonths(1);
        }
    }
}
