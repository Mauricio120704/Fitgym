-- ===================================================
-- Script para corregir estados inválidos en inventario
-- ===================================================
-- Este script identifica y corrige todos los valores de 'estado' 
-- que no corresponden con los valores válidos del combo de filtro
-- Valores válidos: DISPONIBLE, BAJO_STOCK, AGOTADO, DESCONTINUADO

-- 1. Mostrar estados inválidos actuales
SELECT DISTINCT estado, COUNT(*) as cantidad
FROM inventario
WHERE estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO')
GROUP BY estado;

-- 2. Corregir estados basados en la cantidad de stock
-- Si cantidad <= 0 -> AGOTADO
UPDATE inventario 
SET estado = 'AGOTADO', ultima_actualizacion = CURRENT_DATE
WHERE cantidad <= 0 
  AND estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO');

-- 3. Si cantidad > 0 y cantidad <= stock_minimo -> BAJO_STOCK
UPDATE inventario 
SET estado = 'BAJO_STOCK', ultima_actualizacion = CURRENT_DATE
WHERE cantidad > 0 
  AND cantidad <= stock_minimo 
  AND estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO');

-- 4. Si cantidad > stock_minimo -> DISPONIBLE
UPDATE inventario 
SET estado = 'DISPONIBLE', ultima_actualizacion = CURRENT_DATE
WHERE cantidad > stock_minimo 
  AND estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO');

-- 5. Para cualquier otro estado inválido que quede, asignar DISPONIBLE por defecto
UPDATE inventario 
SET estado = 'DISPONIBLE', ultima_actualizacion = CURRENT_DATE
WHERE estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO');

-- 6. Verificar que todos los estados sean válidos
SELECT DISTINCT estado, COUNT(*) as cantidad
FROM inventario
GROUP BY estado
ORDER BY estado;

-- 7. Mostrar resumen de cambios
SELECT 
    COUNT(*) as total_productos,
    SUM(CASE WHEN estado = 'DISPONIBLE' THEN 1 ELSE 0 END) as disponibles,
    SUM(CASE WHEN estado = 'BAJO_STOCK' THEN 1 ELSE 0 END) as bajo_stock,
    SUM(CASE WHEN estado = 'AGOTADO' THEN 1 ELSE 0 END) as agotados,
    SUM(CASE WHEN estado = 'DESCONTINUADO' THEN 1 ELSE 0 END) as descontinuados
FROM inventario;
