-- Script para corregir los valores de estado en la tabla equipos
-- Mapea los valores actuales a los estados correctos

-- Mapeo de valores:
-- NORMAL -> ACTIVO
-- VENCIDO -> MANTENIMIENTO
-- Pendiente -> DAÑADO
-- Los demás se mantienen igual

UPDATE public.equipos
SET estado = 'ACTIVO'
WHERE estado = 'NORMAL';

UPDATE public.equipos
SET estado = 'MANTENIMIENTO'
WHERE estado = 'VENCIDO';

UPDATE public.equipos
SET estado = 'DAÑADO'
WHERE estado = 'Pendiente';

-- Verificar los cambios
SELECT estado, COUNT(*) as cantidad
FROM public.equipos
GROUP BY estado
ORDER BY estado;
