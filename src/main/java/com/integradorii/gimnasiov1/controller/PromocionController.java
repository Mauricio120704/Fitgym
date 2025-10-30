package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Promocion;
import com.integradorii.gimnasiov1.model.PromocionMembresia;
import com.integradorii.gimnasiov1.repository.PromocionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador de Promociones - Gestión de descuentos y ofertas
 * Ruta: /promociones | Acceso: ADMIN, RECEPCIONISTA, ENTRENADOR
 * Tabla: promociones, promocion_membresia
 */
@Controller
public class PromocionController {

    private final PromocionRepository promocionRepository;

    public PromocionController(PromocionRepository promocionRepository) {
        this.promocionRepository = promocionRepository;
    }

    /**
     * GET /promociones - Lista todas las promociones
     * Permite búsqueda por nombre/descripción
     * Separa activas/expiradas de inactivas
     */
    @GetMapping("/promociones")
    public String listarPromociones(@RequestParam(name = "q", required = false) String q, Model model) {
        List<Promocion> activasYExpiradas;
        List<Promocion> inactivas;
        if (q != null && q.isBlank()) {
            return "redirect:/promociones";
        }
        if (q != null) {
            List<Promocion> filtradas = promocionRepository
                    .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(q, q);
            activasYExpiradas = filtradas.stream()
                    .filter(p -> p.getEstado() == Promocion.Estado.ACTIVE || p.getEstado() == Promocion.Estado.EXPIRED)
                    .collect(Collectors.toList());
            inactivas = filtradas.stream()
                    .filter(p -> p.getEstado() == Promocion.Estado.INACTIVE)
                    .collect(Collectors.toList());
            model.addAttribute("q", q);
        } else {
            activasYExpiradas = promocionRepository.findByEstadoIn(
                    Arrays.asList(Promocion.Estado.ACTIVE, Promocion.Estado.EXPIRED)
            );
            inactivas = promocionRepository.findByEstado(Promocion.Estado.INACTIVE);
        }
        model.addAttribute("activas", activasYExpiradas);
        model.addAttribute("inactivas", inactivas);
        return "promociones";
    }

    /**
     * POST /promociones - Crea nueva promoción
     * Tipos: AMOUNT (monto fijo) o PERCENTAGE (porcentaje)
     * Estado auto: EXPIRED si fecha fin < hoy, sino ACTIVE
     */
    @PostMapping("/promociones")
    public String crearPromocion(@RequestParam String nombre,
                                 @RequestParam String tipo,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam BigDecimal valor,
                                 @RequestParam Integer maxUsos,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                 @RequestParam(name = "membership", required = false) List<String> memberships) {

        Promocion p = new Promocion();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setTipo("AMOUNT".equalsIgnoreCase(tipo) ? Promocion.TipoDescuento.AMOUNT : Promocion.TipoDescuento.PERCENTAGE);
        p.setValor(valor);
        p.setMaxUsos(maxUsos);
        p.setFechaInicio(fechaInicio);
        p.setFechaFin(fechaFin);

        // Calcular estado según fecha de fin
        if (fechaFin != null && fechaFin.isBefore(LocalDate.now())) {
            p.setEstado(Promocion.Estado.EXPIRED);
        } else {
            p.setEstado(Promocion.Estado.ACTIVE);
        }

        if (memberships != null) {
            for (String m : memberships) {
                PromocionMembresia pm = new PromocionMembresia();
                pm.setPromocion(p);
                pm.setMembresia(m);
                p.getMembresias().add(pm);
            }
        }

        promocionRepository.save(p);
        return "redirect:/promociones";
    }

    /**
     * POST /promociones/{id}/toggle - Activa/Desactiva promoción
     * Solo alterna entre ACTIVE e INACTIVE (no afecta EXPIRED)
     */
    @PostMapping("/promociones/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        Optional<Promocion> opt = promocionRepository.findById(id);
        if (opt.isPresent()) {
            Promocion p = opt.get();
            if (p.getEstado() == Promocion.Estado.INACTIVE) {
                p.setEstado(Promocion.Estado.ACTIVE);
            } else if (p.getEstado() == Promocion.Estado.ACTIVE) {
                p.setEstado(Promocion.Estado.INACTIVE);
            }
            promocionRepository.save(p);
        }
        return "redirect:/promociones";
    }

    /**
     * POST /promociones/{id}/delete - Elimina promoción por ID
     */
    @PostMapping("/promociones/{id}/delete")
    public String delete(@PathVariable Long id) {
        promocionRepository.deleteById(id);
        return "redirect:/promociones";
    }

    /**
     * POST /promociones/{id}/editar - Actualiza promoción existente
     * Reemplaza completamente las membresías asociadas
     */
    @PostMapping("/promociones/{id}/editar")
    public String editar(@PathVariable Long id,
                         @RequestParam String nombre,
                         @RequestParam String tipo,
                         @RequestParam(required = false) String descripcion,
                         @RequestParam BigDecimal valor,
                         @RequestParam Integer maxUsos,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                         @RequestParam(name = "membership", required = false) List<String> memberships) {
        Promocion p = promocionRepository.findById(id).orElseThrow();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setTipo("AMOUNT".equalsIgnoreCase(tipo) ? Promocion.TipoDescuento.AMOUNT : Promocion.TipoDescuento.PERCENTAGE);
        p.setValor(valor);
        p.setMaxUsos(maxUsos);
        p.setFechaInicio(fechaInicio);
        p.setFechaFin(fechaFin);
        if (fechaFin != null && fechaFin.isBefore(LocalDate.now())) {
            p.setEstado(Promocion.Estado.EXPIRED);
        }
        // Reemplazar membresías
        p.getMembresias().clear();
        if (memberships != null) {
            for (String m : memberships) {
                PromocionMembresia pm = new PromocionMembresia();
                pm.setPromocion(p);
                pm.setMembresia(m);
                p.getMembresias().add(pm);
            }
        }
        promocionRepository.save(p);
        return "redirect:/promociones";
    }
}
