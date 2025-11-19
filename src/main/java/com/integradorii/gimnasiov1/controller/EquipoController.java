package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Equipo;
import com.integradorii.gimnasiov1.model.Mantenimiento;
import com.integradorii.gimnasiov1.service.EquipoService;
import com.integradorii.gimnasiov1.service.MantenimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/equipos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private MantenimientoService mantenimientoService;

    @GetMapping
    public String listarEquipos(Model model, 
                               @RequestParam(required = false) String buscar,
                               @RequestParam(required = false) String estado,
                               @RequestParam(required = false) String tipo) {
        
        List<Equipo> equipos;
        
        if (buscar != null && !buscar.trim().isEmpty()) {
            equipos = equipoService.buscarEquiposPorTermino(buscar);
        } else if (estado != null && !estado.equals("TODOS")) {
            equipos = equipoService.findByEstado(estado);
        } else if (tipo != null && !tipo.equals("TODOS")) {
            equipos = equipoService.findByTipo(tipo);
        } else {
            equipos = equipoService.findAll();
        }

        // Estadísticas
        Long totalEquipos = (long) equipos.size();
        Long equiposActivos = equipoService.contarEquiposPorEstado("ACTIVO");
        Long equiposEnMantenimiento = equipoService.contarEquiposPorEstado("MANTENIMIENTO");
        Long equiposDanados = equipoService.contarEquiposPorEstado("DAÑADO");
        Long equiposFueraServicio = equipoService.contarEquiposPorEstado("FUERA_DE_SERVICIO");

        model.addAttribute("equipos", equipos);
        model.addAttribute("totalEquipos", totalEquipos);
        model.addAttribute("equiposActivos", equiposActivos);
        model.addAttribute("equiposEnMantenimiento", equiposEnMantenimiento);
        model.addAttribute("equiposDanados", equiposDanados);
        model.addAttribute("equiposFueraServicio", equiposFueraServicio);
        
        // Filtros
        model.addAttribute("buscar", buscar);
        model.addAttribute("estadoSeleccionado", estado != null ? estado : "TODOS");
        model.addAttribute("tipoSeleccionado", tipo != null ? tipo : "TODOS");
        
        // Equipos con mantenimiento pendiente
        List<Equipo> equiposMantenimientoPendiente = equipoService.findEquiposConMantenimientoPendiente();
        model.addAttribute("equiposMantenimientoPendiente", equiposMantenimientoPendiente);
        
        model.addAttribute("activeMenu", "equipos");
        return "admin/equipos/listado";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("equipo", new Equipo());
        model.addAttribute("activeMenu", "equipos");
        return "admin/equipos/nuevo";
    }

    @PostMapping("/guardar")
    public String guardarEquipo(@ModelAttribute Equipo equipo, RedirectAttributes redirectAttributes) {
        try {
            equipo.setUltimoMantenimiento(LocalDate.now());
            equipoService.save(equipo);
            redirectAttributes.addFlashAttribute("mensaje", "Equipo guardado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar el equipo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/admin/equipos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Equipo equipo = equipoService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));
            model.addAttribute("equipo", equipo);
            model.addAttribute("activeMenu", "equipos");
            return "admin/equipos/editar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/equipos";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarEquipo(@PathVariable Long id, @ModelAttribute Equipo equipo, RedirectAttributes redirectAttributes) {
        try {
            Equipo equipoExistente = equipoService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));
            
            equipoExistente.setNombre(equipo.getNombre());
            equipoExistente.setTipo(equipo.getTipo());
            equipoExistente.setMarca(equipo.getMarca());
            equipoExistente.setModelo(equipo.getModelo());
            equipoExistente.setNumeroSerie(equipo.getNumeroSerie());
            equipoExistente.setUbicacion(equipo.getUbicacion());
            equipoExistente.setFechaAdquisicion(equipo.getFechaAdquisicion());
            equipoExistente.setEstado(equipo.getEstado());
            equipoExistente.setDescripcion(equipo.getDescripcion());
            equipoExistente.setUltimoMantenimiento(equipo.getUltimoMantenimiento());
            equipoExistente.setProximoMantenimiento(equipo.getProximoMantenimiento());
            
            equipoService.save(equipoExistente);
            redirectAttributes.addFlashAttribute("mensaje", "Equipo actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el equipo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/admin/equipos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEquipo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipoService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Equipo eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el equipo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/admin/equipos";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleEquipo(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Equipo equipo = equipoService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));
            
            List<Mantenimiento> historialMantenimiento = mantenimientoService.findHistorialMantenimientoPorEquipo(id);
            
            model.addAttribute("equipo", equipo);
            model.addAttribute("historialMantenimiento", historialMantenimiento);
            model.addAttribute("activeMenu", "equipos");
            return "admin/equipos/detalle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/equipos";
        }
    }

    @GetMapping("/registrar-servicio/{id}")
    public String mostrarFormularioRegistrarServicio(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Equipo equipo = equipoService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));
            
            Mantenimiento mantenimiento = new Mantenimiento();
            mantenimiento.setEquipo(equipo);
            mantenimiento.setFechaServicio(LocalDate.now());
            mantenimiento.setEstado("PROGRAMADO");
            
            model.addAttribute("equipo", equipo);
            model.addAttribute("mantenimiento", mantenimiento);
            model.addAttribute("activeMenu", "equipos");
            return "admin/equipos/registrar-servicio";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/equipos";
        }
    }

    @PostMapping("/guardar-servicio")
    public String guardarServicio(@ModelAttribute Mantenimiento mantenimiento, RedirectAttributes redirectAttributes) {
        try {
            mantenimientoService.save(mantenimiento);
            
            // Actualizar estado del equipo si es necesario
            if ("COMPLETADO".equals(mantenimiento.getEstado())) {
                equipoService.actualizarEstado(mantenimiento.getEquipo().getId(), "ACTIVO");
                equipoService.actualizarProximoMantenimiento(
                    mantenimiento.getEquipo().getId(), 
                    LocalDate.parse(mantenimiento.getProximaRevision())
                );
            } else if ("EN_PROGRESO".equals(mantenimiento.getEstado())) {
                equipoService.actualizarEstado(mantenimiento.getEquipo().getId(), "MANTENIMIENTO");
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Servicio registrado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al registrar el servicio: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/admin/equipos";
    }
}
