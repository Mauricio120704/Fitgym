package com.integradorii.gimnasiov1.controller;

import com.integradorii.gimnasiov1.model.Inventario;
import com.integradorii.gimnasiov1.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/admin/inventario")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    public String listarInventario(Model model,
                                  @RequestParam(required = false) String buscar,
                                  @RequestParam(required = false) String estado,
                                  @RequestParam(required = false) String categoria,
                                  @RequestParam(required = false) String proveedor,
                                  @RequestParam(required = false) String ubicacion,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {

        List<Inventario> inventario;

        // Convertir "TODOS" a null para que la consulta funcione correctamente
        String estadoFiltro = "TODOS".equals(estado) ? null : estado;
        String categoriaFiltro = "TODOS".equals(categoria) ? null : categoria;
        String ubicacionFiltro = "TODOS".equals(ubicacion) ? null : ubicacion;
        String proveedorFiltro = (proveedor != null && proveedor.trim().isEmpty()) ? null : proveedor;

        if (buscar != null && !buscar.trim().isEmpty()) {
            inventario = inventarioService.buscarPorTermino(buscar);
        } else {
            inventario = inventarioService.buscarConFiltros(estadoFiltro, categoriaFiltro, proveedorFiltro, ubicacionFiltro, null);
        }

        // Paginación manual sobre la lista resultante
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), inventario.size());
        List<Inventario> pageContent = start <= end ? inventario.subList(start, end) : List.of();
        Page<Inventario> inventarioPage = new PageImpl<>(pageContent, pageable, inventario.size());

        // Estadísticas del dashboard
        InventarioService.DashboardStats stats = inventarioService.getDashboardStats();

        model.addAttribute("inventario", inventarioPage.getContent());
        model.addAttribute("inventarioPage", inventarioPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", inventarioPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("totalProductos", stats.getTotalProductos());
        model.addAttribute("productosDisponibles", stats.getProductosDisponibles());
        model.addAttribute("productosBajoStock", stats.getProductosBajoStock());
        model.addAttribute("productosAgotados", stats.getProductosAgotados());
        model.addAttribute("valorTotalInventario", stats.getValorTotalInventario());

        // Filtros
        model.addAttribute("buscar", buscar);
        model.addAttribute("estadoSeleccionado", estado != null ? estado : "TODOS");
        model.addAttribute("categoriaSeleccionada", categoria != null ? categoria : "TODOS");
        model.addAttribute("proveedorSeleccionado", proveedor != null ? proveedor : "TODOS");
        model.addAttribute("ubicacionSeleccionada", ubicacion != null ? ubicacion : "TODOS");

        // Listas para filtros
        model.addAttribute("estados", List.of("TODOS", "DISPONIBLE", "BAJO_STOCK", "AGOTADO"));
        model.addAttribute("categorias", List.of("TODOS", "EQUIPAMIENTO", "SUPLEMENTOS", "ROPA_DEPORTIVA", "ACCESORIOS", "HIGIENE", "OFICINA","BEBIDAS/SNACKS"));
        model.addAttribute("ubicaciones", List.of("TODOS", "ALMACEN_PRINCIPAL", "VENTA", "SHOWROOM", "DEPOSITO_SECUNDARIO"));

        // Productos que necesitan reorden
        List<Inventario> productosReorden = inventarioService.findProductosQueNecesitanReorden();
        model.addAttribute("productosReorden", productosReorden);

        // Objeto para el modal de "Nuevo Producto" y listas de opciones
        model.addAttribute("productoNuevo", new Inventario());
        model.addAttribute("categoriasOpciones", List.of("EQUIPAMIENTO", "SUPLEMENTOS", "ROPA_DEPORTIVA", "ACCESORIOS", "HIGIENE", "OFICINA", "BEBIDAS/SNACKS"));
        model.addAttribute("ubicacionesOpciones", List.of("ALMACEN_PRINCIPAL", "VENTA", "SHOWROOM", "DEPOSITO_SECUNDARIO"));

        model.addAttribute("activeMenu", "inventario");
        return "admin/inventario/listado";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("inventario", new Inventario());
        model.addAttribute("categorias", List.of("EQUIPAMIENTO", "SUPLEMENTOS", "ROPA_DEPORTIVA", "ACCESORIOS", "HIGIENE", "OFICINA"));
        model.addAttribute("ubicaciones", List.of("ALMACEN_PRINCIPAL", "VENTA", "SHOWROOM", "DEPOSITO_SECUNDARIO"));
        model.addAttribute("activeMenu", "inventario");
        return "admin/inventario/nuevo";
    }

    @PostMapping("/guardar")
    public String guardarInventario(@ModelAttribute Inventario inventario,
                                   RedirectAttributes redirectAttributes) {
        try {
            inventarioService.save(inventario);
            redirectAttributes.addFlashAttribute("mensaje", "Producto guardado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/inventario/nuevo";
        }

        return "redirect:/admin/inventario";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            Inventario inventario = inventarioService.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            model.addAttribute("inventario", inventario);
            model.addAttribute("categorias", List.of("EQUIPAMIENTO", "SUPLEMENTOS", "ROPA_DEPORTIVA", "ACCESORIOS", "HIGIENE", "OFICINA"));
            model.addAttribute("ubicaciones", List.of("ALMACEN_PRINCIPAL", "VENTA", "SHOWROOM", "DEPOSITO_SECUNDARIO"));
            model.addAttribute("activeMenu", "inventario");
            return "admin/inventario/editar";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/inventario";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarInventario(@PathVariable Long id,
                                      @ModelAttribute Inventario inventario,
                                      RedirectAttributes redirectAttributes) {
        try {
            inventario.setId(id);
            inventarioService.save(inventario);
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/admin/inventario";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            Inventario inventario = inventarioService.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            model.addAttribute("inventario", inventario);
            model.addAttribute("valorTotal", inventario.getValorTotal());
            model.addAttribute("necesitaReorden", inventario.necesitaReorden());
            model.addAttribute("activeMenu", "inventario");
            return "admin/inventario/detalle";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/inventario";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarInventario(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            inventarioService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/admin/inventario";
    }

    // Operaciones de stock
    @GetMapping("/ajustar-stock/{id}")
    public String mostrarFormularioAjustarStock(@PathVariable Long id, Model model,
                                              RedirectAttributes redirectAttributes) {
        try {
            Inventario inventario = inventarioService.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            model.addAttribute("producto", inventario);
            model.addAttribute("activeMenu", "inventario");
            return "admin/inventario/ajustar-stock";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/inventario";
        }
    }

    @PostMapping("/actualizar-stock/{id}")
    public String actualizarStock(@PathVariable Long id,
                                 @RequestParam Integer nuevaCantidad,
                                 RedirectAttributes redirectAttributes) {
        try {
            inventarioService.actualizarStock(id, nuevaCantidad);
            redirectAttributes.addFlashAttribute("mensaje", "Stock actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar stock: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/admin/inventario";
    }

    // Reportes
    @GetMapping("/reporte/bajo-stock")
    public String reporteBajoStock(Model model) {
        List<Inventario> productos = inventarioService.generarReporteBajoStock();
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Reporte de Productos con Bajo Stock");
        model.addAttribute("activeMenu", "inventario");
        return "admin/inventario/reporte";
    }

    @GetMapping("/reporte/agotados")
    public String reporteAgotados(Model model) {
        List<Inventario> productos = inventarioService.generarReporteProductosAgotados();
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Reporte de Productos Agotados");
        model.addAttribute("activeMenu", "inventario");
        return "admin/inventario/reporte";
    }

    @GetMapping("/reporte/reorden")
    public String reporteReorden(Model model) {
        List<Inventario> productos = inventarioService.generarReporteReorden();
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Reporte de Productos para Reorden");
        model.addAttribute("activeMenu", "inventario");
        return "admin/inventario/reporte";
    }
}
