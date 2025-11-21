-- ===================================================
-- Migración: Corregir estados inválidos en inventario
-- ===================================================
-- Fecha: 2025-11-20
-- Descripción: Corrige todos los valores de 'estado' en la tabla inventario
-- que no corresponden con los valores válidos del combo de filtro
-- Valores válidos: DISPONIBLE, BAJO_STOCK, AGOTADO, DESCONTINUADO

-- 1. Reemplazar valores específicos inválidos
UPDATE inventario SET estado = 'BAJO_STOCK', ultima_actualizacion = CURRENT_DATE WHERE estado = 'Crítico';
UPDATE inventario SET estado = 'BAJO_STOCK', ultima_actualizacion = CURRENT_DATE WHERE estado = 'Bajo';
UPDATE inventario SET estado = 'DISPONIBLE', ultima_actualizacion = CURRENT_DATE WHERE UPPER(estado) = 'DISPONIBLE' AND estado != 'DISPONIBLE';

-- 2. Corregir estados basados en la cantidad de stock
-- Si cantidad <= 0 -> AGOTADO
UPDATE inventario 
SET estado = 'AGOTADO', ultima_actualizacion = CURRENT_DATE
WHERE cantidad <= 0 
  AND estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO');

-- Si cantidad > 0 y cantidad <= stock_minimo -> BAJO_STOCK
UPDATE inventario 
SET estado = 'BAJO_STOCK', ultima_actualizacion = CURRENT_DATE
WHERE cantidad > 0 
  AND cantidad <= stock_minimo 
  AND estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO');

-- Si cantidad > stock_minimo -> DISPONIBLE
UPDATE inventario 
SET estado = 'DISPONIBLE', ultima_actualizacion = CURRENT_DATE
WHERE cantidad > stock_minimo 
  AND estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO');

-- 3. Para cualquier otro estado inválido que quede, asignar DISPONIBLE por defecto
UPDATE inventario 
SET estado = 'DISPONIBLE', ultima_actualizacion = CURRENT_DATE
WHERE estado NOT IN ('DISPONIBLE', 'BAJO_STOCK', 'AGOTADO', 'DESCONTINUADO');
