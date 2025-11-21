-- ===================================================
-- Corrección inmediata de estados inválidos
-- ===================================================

-- 1. Reemplazar 'Crítico' por 'BAJO_STOCK'
UPDATE inventario SET estado = 'BAJO_STOCK' WHERE estado = 'Crítico';

-- 2. Reemplazar 'Bajo' por 'BAJO_STOCK'
UPDATE inventario SET estado = 'BAJO_STOCK' WHERE estado = 'Bajo';

-- 3. Reemplazar 'DISPONIBLE' (si está en minúsculas o con variaciones)
UPDATE inventario SET estado = 'DISPONIBLE' WHERE UPPER(estado) = 'DISPONIBLE' AND estado != 'DISPONIBLE';

-- 4. Corregir cualquier valor que no sea uno de los 4 válidos
UPDATE inventario 
SET estado = 'DISPONIBLE'
WHERE estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO');

-- 5. Verificar que todos los estados sean válidos
SELECT DISTINCT estado, COUNT(*) as cantidad
FROM inventario
GROUP BY estado
ORDER BY estado;

-- 6. Mostrar todos los productos con sus estados
SELECT codigo_producto, nombre, cantidad, stock_minimo, estado
FROM inventario
ORDER BY codigo_producto;
