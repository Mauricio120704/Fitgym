package com.integradorii.gimnasiov1.service;

import com.integradorii.gimnasiov1.model.Inventario;
import com.integradorii.gimnasiov1.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    // CRUD Operations
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    public Optional<Inventario> findById(Long id) {
        return inventarioRepository.findById(id);
    }

    public Inventario save(Inventario inventario) {
        // Validar que el código de producto no exista (para nuevos productos)
        if (inventario.getId() == null && inventarioRepository.existsByCodigoProducto(inventario.getCodigoProducto())) {
            throw new RuntimeException("Ya existe un producto con el código: " + inventario.getCodigoProducto());
        }
        
        inventario.actualizarEstado();
        return inventarioRepository.save(inventario);
    }

    public void deleteById(Long id) {
        inventarioRepository.deleteById(id);
    }

    // Search and Filter Operations
    public List<Inventario> buscarPorTermino(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return findAll();
        }
        return inventarioRepository.buscarPorTermino(termino);
    }

    public List<Inventario> findByEstado(String estado) {
        return inventarioRepository.findByEstado(estado);
    }

    public List<Inventario> findByCategoria(String categoria) {
        return inventarioRepository.findByCategoria(categoria);
    }

    public List<Inventario> findByProveedor(String proveedor) {
        return inventarioRepository.findByProveedor(proveedor);
    }

    public List<Inventario> findByUbicacion(String ubicacion) {
        return inventarioRepository.findByUbicacion(ubicacion);
    }

    public List<Inventario> buscarConFiltros(String estado, String categoria, String proveedor, String ubicacion, String termino) {
        return inventarioRepository.buscarConFiltros(estado, categoria, proveedor, ubicacion, termino);
    }

    // Stock Management
    public List<Inventario> findProductosConBajoStock() {
        return inventarioRepository.findProductosConBajoStock();
    }

    public List<Inventario> findProductosAgotados() {
        return inventarioRepository.findProductosAgotados();
    }

    public List<Inventario> findProductosQueNecesitanReorden() {
        return inventarioRepository.findProductosQueNecesitanReorden();
    }

    public List<Inventario> findProductosProximosAgotarse() {
        return inventarioRepository.findProductosProximosAgotarse();
    }

    // Statistics
    public Long contarProductosPorEstado(String estado) {
        return inventarioRepository.countByEstado(estado);
    }

    public Long contarProductosPorCategoria(String categoria) {
        return inventarioRepository.countByCategoria(categoria);
    }

    public Long contarProductosBajoStock() {
        return inventarioRepository.contarProductosBajoStock();
    }

    public Double calcularValorTotalInventario() {
        Double valor = inventarioRepository.calcularValorTotalInventario();
        return valor != null ? valor : 0.0;
    }

    // Business Operations
    public Inventario actualizarStock(Long id, Integer nuevaCantidad) {
        Optional<Inventario> inventarioOpt = inventarioRepository.findById(id);
        if (!inventarioOpt.isPresent()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        Inventario inventario = inventarioOpt.get();
        inventario.setCantidad(nuevaCantidad);
        return inventarioRepository.save(inventario);
    }

    public Inventario reducirStock(Long id, Integer cantidadAReducir) {
        Optional<Inventario> inventarioOpt = inventarioRepository.findById(id);
        if (!inventarioOpt.isPresent()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        Inventario inventario = inventarioOpt.get();
        if (!inventario.puedeSolicitar(cantidadAReducir)) {
            throw new RuntimeException("Stock insuficiente para reducir " + cantidadAReducir + " unidades. Stock actual: " + inventario.getCantidad());
        }

        inventario.reducirStock(cantidadAReducir);
        return inventarioRepository.save(inventario);
    }

    public Inventario aumentarStock(Long id, Integer cantidadAAumentar) {
        Optional<Inventario> inventarioOpt = inventarioRepository.findById(id);
        if (!inventarioOpt.isPresent()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        Inventario inventario = inventarioOpt.get();
        inventario.aumentarStock(cantidadAAumentar);
        return inventarioRepository.save(inventario);
    }

    public Inventario findByCodigoProducto(String codigoProducto) {
        return inventarioRepository.findByCodigoProducto(codigoProducto);
    }

    // Validation Methods
    public boolean existeCodigoProducto(String codigoProducto) {
        return inventarioRepository.existsByCodigoProducto(codigoProducto);
    }

    public boolean puedeSolicitarProducto(Long id, Integer cantidad) {
        Optional<Inventario> inventarioOpt = inventarioRepository.findById(id);
        return inventarioOpt.isPresent() && inventarioOpt.get().puedeSolicitar(cantidad);
    }

    // Reports
    public List<Inventario> generarReporteBajoStock() {
        return findProductosConBajoStock();
    }

    public List<Inventario> generarReporteProductosAgotados() {
        return findProductosAgotados();
    }

    public List<Inventario> generarReporteReorden() {
        return findProductosQueNecesitanReorden();
    }

    // Dashboard Statistics
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        Long totalProductos = (long) findAll().size();
        Long productosDisponibles = contarProductosPorEstado("DISPONIBLE");
        Long productosBajoStock = contarProductosBajoStock();
        Long productosAgotados = contarProductosPorEstado("AGOTADO");
        Double valorTotal = calcularValorTotalInventario();

        stats.setTotalProductos(totalProductos);
        stats.setProductosDisponibles(productosDisponibles);
        stats.setProductosBajoStock(productosBajoStock);
        stats.setProductosAgotados(productosAgotados);
        stats.setValorTotalInventario(valorTotal);

        return stats;
    }

    // Inner class for dashboard statistics
    public static class DashboardStats {
        private Long totalProductos;
        private Long productosDisponibles;
        private Long productosBajoStock;
        private Long productosAgotados;
        private Double valorTotalInventario;

        // Getters and Setters
        public Long getTotalProductos() { return totalProductos; }
        public void setTotalProductos(Long totalProductos) { this.totalProductos = totalProductos; }

        public Long getProductosDisponibles() { return productosDisponibles; }
        public void setProductosDisponibles(Long productosDisponibles) { this.productosDisponibles = productosDisponibles; }

        public Long getProductosBajoStock() { return productosBajoStock; }
        public void setProductosBajoStock(Long productosBajoStock) { this.productosBajoStock = productosBajoStock; }

        public Long getProductosAgotados() { return productosAgotados; }
        public void setProductosAgotados(Long productosAgotados) { this.productosAgotados = productosAgotados; }

        public Double getValorTotalInventario() { return valorTotalInventario; }
        public void setValorTotalInventario(Double valorTotalInventario) { this.valorTotalInventario = valorTotalInventario; }
    }
}
