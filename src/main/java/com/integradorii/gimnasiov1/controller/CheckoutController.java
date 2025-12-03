package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.*;
import com.integradorii.gimnasiov1.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
    private final PromocionRepository promocionRepository;
    private final ClaseRepository claseRepository;
    private final ReservaClaseRepository reservaClaseRepository;

    @Value("${stripe.publishable.key:}")
    private String stripePublishableKey;

    public CheckoutController(PersonaRepository personaRepository,
                            PlanRepository planRepository,
                            SuscripcionRepository suscripcionRepository,
                            PagoRepository pagoRepository,
                            PromocionRepository promocionRepository,
                            ClaseRepository claseRepository,
                            ReservaClaseRepository reservaClaseRepository) {
        this.personaRepository = personaRepository;
        this.planRepository = planRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.pagoRepository = pagoRepository;
        this.promocionRepository = promocionRepository;
        this.claseRepository = claseRepository;
        this.reservaClaseRepository = reservaClaseRepository;
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
            @RequestParam(required = false) Long promoId,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean autenticado = userDetails != null && userDetails.getUsername() != null;
        if (!autenticado) {
            return "redirect:/registro";
        }

        String usuarioFinal = usuario;
        if ((usuarioFinal == null || usuarioFinal.isBlank()) && userDetails != null && userDetails.getUsername() != null) {
            usuarioFinal = userDetails.getUsername();
        }

        model.addAttribute("planNombre", plan);
        model.addAttribute("periodo", periodo);
        model.addAttribute("precio", precio);
        model.addAttribute("usuario", usuarioFinal);
        model.addAttribute("stripePublishableKey", stripePublishableKey);
        model.addAttribute("promoId", promoId);
        model.addAttribute("autenticado", autenticado);
        
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

        // Si viene una promoción seleccionada, mostrar información de descuento estimado
        if (promoId != null) {
            Promocion promo = obtenerPromocionAplicable(promoId, periodo);
            if (promo != null) {
                double precioBase = parsePrecio(precio);
                double precioConDescuento = aplicarDescuento(precioBase, promo);
                if (precioConDescuento < 0) {
                    precioConDescuento = 0;
                }
                model.addAttribute("promo", promo);
                model.addAttribute("precioConDescuento", String.format("%.2f", precioConDescuento));
            }
        }
        
        return "checkout";
    }

    /**
     * POST /checkout/create-session
     *
     * Crea una sesión de Checkout de Stripe para el pago de una membresía.
     * 
     */
    @PostMapping("/create-session")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestParam String usuario,
            @RequestParam String plan,
            @RequestParam String periodo,
            @RequestParam String precio,
            @RequestParam(required = false) Long promoId,
            HttpServletRequest request) {

        Map<String, String> response = new HashMap<>();
        try {
            Promocion promo = null;
            if (promoId != null) {
                promo = obtenerPromocionAplicable(promoId, periodo);
            }

            double amountDouble = parsePrecio(precio);
            if (promo != null) {
                amountDouble = aplicarDescuento(amountDouble, promo);
            }
            long amountInCents = Math.round(amountDouble * 100);

            if (amountInCents <= 0) {
                response.put("error", "Monto inválido para el pago.");
                return ResponseEntity.badRequest().body(response);
            }

            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (("http".equals(request.getScheme()) && request.getServerPort() != 80) ||
                ("https".equals(request.getScheme()) && request.getServerPort() != 443)) {
                baseUrl += ":" + request.getServerPort();
            }

            String successUrl = baseUrl + "/checkout/success?session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = baseUrl + "/checkout/cancel?session_id={CHECKOUT_SESSION_ID}";

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(usuario)
                    .putMetadata("usuario", usuario)
                    .putMetadata("plan", plan)
                    .putMetadata("periodo", periodo)
                    .putMetadata("precio", precio);

            if (promo != null) {
                paramsBuilder
                        .putMetadata("promoId", String.valueOf(promo.getId()))
                        .putMetadata("precioConDescuento", String.valueOf(amountDouble));
            }

            SessionCreateParams params = paramsBuilder
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("pen")
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Membresía " + plan)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            response.put("id", session.getId());
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            System.err.println("Error al crear sesión de Stripe: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "No se pudo iniciar el pago. Intenta nuevamente.");
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            System.err.println("Error inesperado al crear sesión de Stripe: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Ocurrió un error al iniciar el pago.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * GET /checkout/clase
     *
     * Muestra la pantalla de pago para una clase de pago individual.
     *
     */
    @GetMapping("/clase")
    public String mostrarCheckoutClase(@RequestParam("claseId") long claseId,
                                       @RequestParam(required = false) Long promoId,
                                       Model model,
                                       @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null || userDetails.getUsername() == null) {
            return "redirect:/login";
        }

        Persona persona = personaRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (persona == null) {
            return "redirect:/login";
        }

        Clase clase = claseRepository.findById(claseId).orElse(null);
        if (clase == null || Boolean.FALSE.equals(clase.getEsPago())) {
            return "redirect:/reservas?claseNoDisponible=true";
        }

        double precioBase = 0.0;
        if (clase.getPrecio() != null) {
            precioBase = clase.getPrecio().doubleValue();
        }

        model.addAttribute("clase", clase);
        model.addAttribute("usuario", persona.getEmail());
        model.addAttribute("stripePublishableKey", stripePublishableKey);
        model.addAttribute("claseId", clase.getId());
        model.addAttribute("promoId", promoId);

        if (promoId != null) {
            Promocion promo = obtenerPromocionClaseAplicable(promoId, clase);
            if (promo != null) {
                double precioConDescuento = aplicarDescuento(precioBase, promo);
                if (precioConDescuento < 0) {
                    precioConDescuento = 0;
                }
                model.addAttribute("promo", promo);
                model.addAttribute("precioConDescuento", String.format("%.2f", precioConDescuento));
            }
        }

        model.addAttribute("precio", String.format("%.2f", precioBase));
        return "checkout_clase";
    }

    /**
     * POST /checkout/clase/create-session
     *
     * Crea una sesión de Stripe para el pago de una clase individual.
     *
     */
    @PostMapping("/clase/create-session")
    @ResponseBody
    @Transactional(readOnly = true) // Solo lectura para validaciones
    public ResponseEntity<Map<String, String>> createCheckoutSessionClase(
            @RequestParam long claseId,
            @RequestParam String usuario,
            @RequestParam String precio,
            @RequestParam(required = false) Long promoId,
            HttpServletRequest request) {

        Map<String, String> response = new HashMap<>();
        
        // 1. Validar que la clase exista y esté disponible
        Clase clase = claseRepository.findById(claseId).orElse(null);
        if (clase == null) {
            response.put("error", "Clase no encontrada");
            return ResponseEntity.badRequest().body(response);
        }

        // 2. Validar que el usuario exista
        Persona persona = personaRepository.findByEmail(usuario).orElse(null);
        if (persona == null) {
            response.put("error", "Usuario no encontrado");
            return ResponseEntity.badRequest().body(response);
        }

        // 3. Validar disponibilidad de la clase
        if (!"Programada".equalsIgnoreCase(clase.getEstado())) {
            response.put("error", "La clase no está disponible para reserva.");
            return ResponseEntity.badRequest().body(response);
        }

        // 4. Validar fecha futura
        java.time.OffsetDateTime ahora = java.time.OffsetDateTime.now();
        if (clase.getFecha() == null || !clase.getFecha().isAfter(ahora)) {
            response.put("error", "La clase ya ocurrió o no tiene fecha válida.");
            return ResponseEntity.badRequest().body(response);
        }

        // 5. Validar que el usuario no tenga ya una reserva activa
        boolean yaReservada = reservaClaseRepository.existsByClase_IdAndDeportista_IdAndEstadoNot(
                clase.getId(), persona.getId(), "Cancelado");
        if (yaReservada) {
            response.put("error", "Ya tienes una reserva para esta clase.");
            return ResponseEntity.badRequest().body(response);
        }

        // 6. Validar capacidad disponible
        long ocupados = reservaClaseRepository.countOcupados(clase.getId());
        if (ocupados >= clase.getCapacidad()) {
            response.put("error", "La clase está completa. No es posible procesar el pago.");
            return ResponseEntity.badRequest().body(response);
        }

        // 7. Si todo está bien, proceder con la creación de la sesión de pago
        try {
            Promocion promo = null;
            if (promoId != null) {
                promo = obtenerPromocionClaseAplicable(promoId, clase);
            }

            double amountDouble = parsePrecio(precio);
            if (promo != null) {
                amountDouble = aplicarDescuento(amountDouble, promo);
            }
            long amountInCents = Math.round(amountDouble * 100);

            if (amountInCents <= 0) {
                response.put("error", "Monto inválido para el pago.");
                return ResponseEntity.badRequest().body(response);
            }

            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (("http".equals(request.getScheme()) && request.getServerPort() != 80) ||
                ("https".equals(request.getScheme()) && request.getServerPort() != 443)) {
                baseUrl += ":" + request.getServerPort();
            }

            String successUrl = baseUrl + "/checkout/clase/success?session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = baseUrl + "/reservas?pagoClaseCancelado=true";

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(usuario)
                    .putMetadata("usuario", usuario)
                    .putMetadata("claseId", String.valueOf(claseId))
                    .putMetadata("precio", precio)
                    .putMetadata("tipoPago", "CLASE");

            if (promo != null) {
                paramsBuilder
                        .putMetadata("promoId", String.valueOf(promo.getId()))
                        .putMetadata("precioConDescuento", String.valueOf(amountDouble));
            }

            SessionCreateParams params = paramsBuilder
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("pen")
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Clase " + clase.getNombre())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            response.put("id", session.getId());
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            System.err.println("Error al crear sesión de Stripe para clase: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "No se pudo iniciar el pago de la clase. Intenta nuevamente.");
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            System.err.println("Error inesperado al crear sesión de Stripe para clase: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Ocurrió un error al iniciar el pago de la clase.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * GET /checkout/clase/success
     *
     * Callback de éxito de Stripe para el pago de una clase.
     *
     */
    @GetMapping("/clase/success")
    @Transactional
    public String stripeSuccessClase(@RequestParam("session_id") String sessionId,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Session session = Session.retrieve(sessionId);

            if (session == null || !"paid".equalsIgnoreCase(session.getPaymentStatus())) {
                return "redirect:/reservas?errorPagoClaseStripe=true";
            }

            String usuario = session.getMetadata() != null ? session.getMetadata().get("usuario") : null;
            String claseIdStr = session.getMetadata() != null ? session.getMetadata().get("claseId") : null;
            String precio = session.getMetadata() != null ? session.getMetadata().get("precio") : null;
            String promoIdStr = session.getMetadata() != null ? session.getMetadata().get("promoId") : null;

            if (usuario == null || claseIdStr == null || precio == null) {
                return "redirect:/reservas?errorPagoClaseStripe=true";
            }

            long claseId = Long.parseLong(claseIdStr);

            Persona persona = personaRepository.findByEmail(usuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuario));

            Clase clase = claseRepository.findById(claseId)
                    .orElseThrow(() -> new RuntimeException("Clase no encontrada: " + claseId));

            boolean yaReservada = reservaClaseRepository.existsByClase_IdAndDeportista_IdAndEstadoNot(
                    clase.getId(), persona.getId(), "Cancelado");
            if (!yaReservada) {
                ReservaClase r = new ReservaClase();
                r.setClase(clase);
                r.setDeportista(persona);
                r.setReservadoEn(java.time.OffsetDateTime.now());
                r.setEstado("Reservado");
                reservaClaseRepository.save(r);
            }

            Pago pago = new Pago();
            pago.setCodigoPago(generarCodigoPago());
            pago.setDeportista(persona);
            pago.setFecha(LocalDate.now());
            pago.setMetodoPago("Stripe");

            Long amountTotal = session.getAmountTotal();
            double montoPagado = amountTotal != null ? amountTotal / 100.0 : parsePrecio(precio);
            pago.setMonto(montoPagado);

            pago.setEstado("Completado");
            pago.setPlanServicio("Clase - " + clase.getNombre());
            pagoRepository.save(pago);

            if (promoIdStr != null && !promoIdStr.isBlank()) {
                try {
                    long promoId = Long.parseLong(promoIdStr);
                    Optional<Promocion> promoOpt = promocionRepository.findById(promoId);
                    if (promoOpt.isPresent()) {
                        Promocion promo = promoOpt.get();
                        Integer usados = promo.getUsados() != null ? promo.getUsados() : 0;
                        promo.setUsados(usados + 1);
                        promocionRepository.save(promo);
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            return "redirect:/reservas?pagoClaseExitoso=true";
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.err.println("Error al procesar el pago de clase mediante Stripe: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/reservas?errorPagoClaseStripe=true";
        }
    }

    /**
     * GET /checkout/success
     *
     * Callback de éxito de Stripe para el pago de una membresía.
     *
     */
    @GetMapping("/success")
    @Transactional
    public String stripeSuccess(@RequestParam("session_id") String sessionId,
                                @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Session session = Session.retrieve(sessionId);

            if (session == null || !"paid".equalsIgnoreCase(session.getPaymentStatus())) {
                return "redirect:/planes?errorPagoStripe=true";
            }

            String usuario = session.getMetadata() != null ? session.getMetadata().get("usuario") : null;
            String plan = session.getMetadata() != null ? session.getMetadata().get("plan") : null;
            String periodo = session.getMetadata() != null ? session.getMetadata().get("periodo") : null;
            String precio = session.getMetadata() != null ? session.getMetadata().get("precio") : null;
            String promoIdStr = session.getMetadata() != null ? session.getMetadata().get("promoId") : null;

            if (usuario == null || plan == null || periodo == null || precio == null) {
                return "redirect:/planes?errorPagoStripe=true";
            }

            Persona persona = personaRepository.findByEmail(usuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuario));

            Plan planEntity = planRepository.findByNombre(plan)
                    .orElseGet(() -> {
                        Plan nuevoPlan = new Plan();
                        nuevoPlan.setNombre(plan);
                        nuevoPlan.setPrecio(parsePrecio(precio));
                        nuevoPlan.setFrecuencia(periodo.equalsIgnoreCase("anual") ? "Anual" : "Mensual");
                        return planRepository.save(nuevoPlan);
                    });

            Optional<Suscripcion> suscripcionExistente = suscripcionRepository.findActiveByDeportistaId(persona.getId());
            Suscripcion suscripcion;

            if (suscripcionExistente.isPresent()) {
                suscripcion = suscripcionExistente.get();
                suscripcion.setPlan(planEntity);
                suscripcion.setEstado("Activa");
            } else {
                suscripcion = new Suscripcion();
                suscripcion.setDeportista(persona);
                suscripcion.setPlan(planEntity);
                suscripcion.setEstado("Activa");
                suscripcion.setFechaInicio(LocalDate.now());
            }

            suscripcion.setFechaFin(calcularFechaFin(periodo));
            suscripcion.setProximoPago(calcularProximoPago(periodo));

            suscripcionRepository.save(suscripcion);

            Pago pago = new Pago();
            pago.setCodigoPago(generarCodigoPago());
            pago.setDeportista(persona);
            pago.setFecha(LocalDate.now());
            pago.setMetodoPago("Stripe");

            Long amountTotal = session.getAmountTotal();
            double montoPagado = amountTotal != null ? amountTotal / 100.0 : parsePrecio(precio);
            pago.setMonto(montoPagado);

            pago.setEstado("Completado");
            pago.setPlanServicio(plan);
            pagoRepository.save(pago);

            persona.setMembresiaActiva(true);
            personaRepository.save(persona);

            // Registrar uso de la promoción si se aplicó
            if (promoIdStr != null && !promoIdStr.isBlank()) {
                try {
                    Long promoId = Long.valueOf(promoIdStr);
                    Optional<Promocion> promoOpt = promocionRepository.findById(promoId);
                    if (promoOpt.isPresent()) {
                        Promocion promo = promoOpt.get();
                        Integer usados = promo.getUsados() != null ? promo.getUsados() : 0;
                        promo.setUsados(usados + 1);
                        promocionRepository.save(promo);
                    }
                } catch (NumberFormatException ignored) {
                    // Si el ID no es válido, simplemente no se registra uso
                }
            }

            // El flujo de compra exige que el usuario esté autenticado antes de pagar,
            // por lo que tras un pago exitoso lo redirigimos siempre a su perfil.
            return "redirect:/perfil";
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.err.println("Error al procesar el pago mediante Stripe: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/planes?errorPagoStripe=true";
        }
    }

    /**
     * GET /checkout/cancel
     * Callback de cancelación de Stripe para membresías.
     * Recupera los datos de la sesión de Stripe (no de parámetros URL).
     */
    @GetMapping("/cancel")
    public String stripeCancel(@RequestParam String session_id, Model model) {
        try {
            Session session = Session.retrieve(session_id);

            String plan = session.getMetadata() != null ? session.getMetadata().get("plan") : null;
            String periodo = session.getMetadata() != null ? session.getMetadata().get("periodo") : null;
            String precio = session.getMetadata() != null ? session.getMetadata().get("precio") : null;
            String usuario = session.getMetadata() != null ? session.getMetadata().get("usuario") : null;

            model.addAttribute("planNombre", plan);
            model.addAttribute("periodo", periodo);
            model.addAttribute("precio", precio);
            model.addAttribute("usuario", usuario);
            model.addAttribute("stripePublishableKey", stripePublishableKey);
            model.addAttribute("error", "El pago fue cancelado. No se ha realizado ningún cargo.");
            return "checkout";
        } catch (Exception e) {
            System.err.println("Error al procesar cancelación de Stripe: " + e.getMessage());
            return "redirect:/planes?errorCancel=true";
        }
    }


    private Promocion obtenerPromocionAplicable(Long promoId, String periodo) {
        if (promoId == null) {
            return null;
        }

        Optional<Promocion> opt = promocionRepository.findById(promoId);
        if (opt.isEmpty()) {
            return null;
        }

        Promocion promo = opt.get();
        if (promo.isEliminado()) {
            return null;
        }
        LocalDate hoy = LocalDate.now();

        boolean vigentePorFecha = 
                (promo.getFechaInicio() == null || !promo.getFechaInicio().isAfter(hoy)) &&
                (promo.getFechaFin() == null || !promo.getFechaFin().isBefore(hoy));

        Integer maxUsos = promo.getMaxUsos();
        Integer usados = promo.getUsados();
        boolean tieneUsosDisponibles = (maxUsos == null) || (usados == null) || (usados < maxUsos);

        boolean aplicaMembresia;
        if (promo.getMembresias() == null || promo.getMembresias().isEmpty()) {
            aplicaMembresia = true;
        } else {
            String membresiaBuscada = "anual".equalsIgnoreCase(periodo) ? "Anual" : "Mensual";
            aplicaMembresia = promo.getMembresias().stream()
                    .anyMatch(m -> m.getMembresia() != null && m.getMembresia().equalsIgnoreCase(membresiaBuscada));
        }

        if (vigentePorFecha && tieneUsosDisponibles && aplicaMembresia && promo.getEstado() == Promocion.Estado.ACTIVE) {
            return promo;
        }

        return null;
    }

    private Promocion obtenerPromocionClaseAplicable(Long promoId, Clase clase) {
        if (promoId == null || clase == null) {
            return null;
        }

        Optional<Promocion> opt = promocionRepository.findById(promoId);
        if (opt.isEmpty()) {
            return null;
        }

        Promocion promo = opt.get();
        if (promo.isEliminado()) {
            return null;
        }
        LocalDate hoy = LocalDate.now();

        boolean vigentePorFecha =
                (promo.getFechaInicio() == null || !promo.getFechaInicio().isAfter(hoy)) &&
                (promo.getFechaFin() == null || !promo.getFechaFin().isBefore(hoy));

        Integer maxUsos = promo.getMaxUsos();
        Integer usados = promo.getUsados();
        boolean tieneUsosDisponibles = (maxUsos == null) || (usados == null) || (usados < maxUsos);

        boolean mismaClase = promo.getClase() != null &&
                promo.getClase().getId() != null &&
                promo.getClase().getId().equals(clase.getId());

        if (vigentePorFecha && tieneUsosDisponibles && mismaClase &&
                promo.getEstado() == Promocion.Estado.ACTIVE &&
                promo.getTipoPromocion() == Promocion.TipoPromocion.CLASE) {
            return promo;
        }

        return null;
    }

    private double aplicarDescuento(double montoBase, Promocion promo) {
        if (promo == null || promo.getValor() == null || montoBase <= 0) {
            return montoBase;
        }

        if (promo.getTipo() == Promocion.TipoDescuento.PERCENTAGE) {
            double porcentaje = promo.getValor().doubleValue();
            return montoBase * (1 - (porcentaje / 100.0));
        } else if (promo.getTipo() == Promocion.TipoDescuento.AMOUNT) {
            double descuento = promo.getValor().doubleValue();
            return montoBase - descuento;
        }

        return montoBase;
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
