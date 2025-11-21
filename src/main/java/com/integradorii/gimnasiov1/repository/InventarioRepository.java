package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    // Búsqueda por término en nombre, descripción o código de producto - usando SQL nativo
    @Query(value = "SELECT * FROM inventario i WHERE " +
           "i.nombre LIKE CAST(CONCAT('%', :termino, '%') AS VARCHAR) OR " +
           "i.descripcion LIKE CAST(CONCAT('%', :termino, '%') AS VARCHAR) OR " +
           "i.codigo_producto LIKE CAST(CONCAT('%', :termino, '%') AS VARCHAR) OR " +
           "i.proveedor LIKE CAST(CONCAT('%', :termino, '%') AS VARCHAR)", 
           nativeQuery = true)
    List<Inventario> buscarPorTermino(@Param("termino") String termino);

    // Filtrar por estado
    List<Inventario> findByEstado(String estado);

    // Filtrar por categoría
    List<Inventario> findByCategoria(String categoria);

    // Filtrar por proveedor
    List<Inventario> findByProveedor(String proveedor);

    // Productos con bajo stock
    @Query("SELECT i FROM Inventario i WHERE i.cantidad <= i.stockMinimo AND i.estado != 'AGOTADO'")
    List<Inventario> findProductosConBajoStock();

    // Productos agotados
    @Query("SELECT i FROM Inventario i WHERE i.cantidad <= 0 OR i.estado = 'AGOTADO'")
    List<Inventario> findProductosAgotados();

    // Productos que necesitan reorden
    @Query("SELECT i FROM Inventario i WHERE i.cantidad <= i.stockMinimo")
    List<Inventario> findProductosQueNecesitanReorden();

    // Contar productos por estado
    Long countByEstado(String estado);

    // Contar productos por categoría
    Long countByCategoria(String categoria);

    // Buscar por código de producto exacto
    Inventario findByCodigoProducto(String codigoProducto);

    // Verificar si existe código de producto
    boolean existsByCodigoProducto(String codigoProducto);

    // Productos por ubicación
    List<Inventario> findByUbicacion(String ubicacion);

    // Productos con stock por debajo del mínimo (excluyendo agotados)
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.cantidad <= i.stockMinimo AND i.cantidad > 0")
    Long contarProductosBajoStock();

    // Valor total del inventario
    @Query("SELECT SUM(i.cantidad * i.precioUnitario) FROM Inventario i WHERE i.estado != 'DESCONTINUADO'")
    Double calcularValorTotalInventario();

    // Productos próximos a agotarse (menos del 20% del stock mínimo)
    @Query("SELECT i FROM Inventario i WHERE i.cantidad > 0 AND i.cantidad <= (i.stockMinimo * 0.2)")
    List<Inventario> findProductosProximosAgotarse();

    // Búsqueda combinada - usando SQL nativo para evitar problemas de tipo
    @Query(value = "SELECT * FROM inventario i WHERE " +
           "(:estado IS NULL OR i.estado = CAST(:estado AS VARCHAR)) AND " +
           "(:categoria IS NULL OR i.categoria = CAST(:categoria AS VARCHAR)) AND " +
           "(:proveedor IS NULL OR i.proveedor LIKE CAST(CONCAT('%', :proveedor, '%') AS VARCHAR)) AND " +
           "(:ubicacion IS NULL OR i.ubicacion = CAST(:ubicacion AS VARCHAR)) AND " +
           "(:termino IS NULL OR " +
           "i.nombre LIKE CAST(CONCAT('%', :termino, '%') AS VARCHAR) OR " +
           "i.descripcion LIKE CAST(CONCAT('%', :termino, '%') AS VARCHAR) OR " +
           "i.codigo_producto LIKE CAST(CONCAT('%', :termino, '%') AS VARCHAR))", 
           nativeQuery = true)
    List<Inventario> buscarConFiltros(
            @Param("estado") String estado,
            @Param("categoria") String categoria,
            @Param("proveedor") String proveedor,
            @Param("ubicacion") String ubicacion,
            @Param("termino") String termino
    );
}
