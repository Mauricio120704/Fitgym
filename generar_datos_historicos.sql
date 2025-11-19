-- Script para poblar datos históricos en reservas_clase
-- Genera 2 años de datos usando clases y personas existentes

-- Primero, verificamos cuántos datos existen actualmente
SELECT COUNT(*) as reservas_existentes FROM reservas_clase;

-- Generamos datos históricos de 2 años atrás
-- Este script crea reservas realistas basadas en fechas de registro de deportistas

-- Variables para el rango de fechas
SET @fecha_inicio = DATE_SUB(CURRENT_DATE, INTERVAL 2 YEAR);
SET @fecha_fin = CURRENT_DATE;

-- Insertamos datos históricos masivos
INSERT INTO reservas_clase (id_clase, id_persona, fecha_reserva, estado, creado_en, actualizado_en)
SELECT 
    c.id_clase,
    p.id_persona,
    -- Fecha de reserva aleatoria dentro del rango histórico, pero posterior al registro del deportista
    DATE_ADD(
        GREATEST(p.fecha_registro, @fecha_inicio),
        INTERVAL FLOOR(RAND() * DATEDIFF(@fecha_fin, GREATEST(p.fecha_registro, @fecha_inicio))) DAY
    ) as fecha_reserva,
    -- Estados realistas: 70% Asistió, 15% Cancelado, 15% Reservado
    CASE 
        WHEN RAND() < 0.7 THEN 'ASISTIO'
        WHEN RAND() < 0.85 THEN 'CANCELADO'
        ELSE 'RESERVADO'
    END as estado,
    NOW() as creado_en,
    NOW() as actualizado_en
FROM 
    clases c
CROSS JOIN 
    personas p
WHERE 
    -- Solo deportistas (rol DEPORTISTA)
    p.rol = 'DEPORTISTA'
    -- Solo clases activas
    AND c.estado = 'ACTIVO'
    -- Limitar para no generar demasiados datos (aprox 50-100 reservas por clase)
    AND RAND() < 0.3
    -- Asegurar que la fecha de reserva sea posterior al registro del deportista
    AND p.fecha_registro IS NOT NULL
    -- Evitar duplicados
    AND NOT EXISTS (
        SELECT 1 FROM reservas_clase rc 
        WHERE rc.id_clase = c.id_clase 
        AND rc.id_persona = p.id_persona
    )
ORDER BY 
    RAND()
LIMIT 5000;  -- Limitar a 5000 reservas para mantener un tamaño manejable

-- Generamos algunas reservas futuras (próximos 30 días) para demostración
INSERT INTO reservas_clase (id_clase, id_persona, fecha_reserva, estado, creado_en, actualizado_en)
SELECT 
    c.id_clase,
    p.id_persona,
    -- Fechas futuras para próximos 30 días
    DATE_ADD(CURRENT_DATE, INTERVAL FLOOR(RAND() * 30) DAY) as fecha_reserva,
    'RESERVADO' as estado,  -- Todas las futuras están en estado RESERVADO
    NOW() as creado_en,
    NOW() as actualizado_en
FROM 
    clases c
CROSS JOIN 
    personas p
WHERE 
    -- Solo deportistas
    p.rol = 'DEPORTISTA'
    -- Clases futuras (con fecha posterior a hoy)
    AND c.fecha_clase > CURRENT_DATE
    -- Limitar cantidad de reservas futuras
    AND RAND() < 0.2
    -- Evitar duplicados
    AND NOT EXISTS (
        SELECT 1 FROM reservas_clase rc 
        WHERE rc.id_clase = c.id_clase 
        AND rc.id_persona = p.id_persona
    )
LIMIT 500;  -- 500 reservas futuras para demostración

-- Mostrar estadísticas después de la inserción
SELECT 
    'ESTADÍSTICAS FINALES' as tipo,
    COUNT(*) as total_reservas,
    COUNT(CASE WHEN estado = 'ASISTIO' THEN 1 END) as asistencias,
    COUNT(CASE WHEN estado = 'CANCELADO' THEN 1 END) as cancelaciones,
    COUNT(CASE WHEN estado = 'RESERVADO' THEN 1 END) as reservados,
    MIN(fecha_reserva) as fecha_mas_antigua,
    MAX(fecha_reserva) as fecha_mas_reciente
FROM reservas_clase;

-- Distribución por tipo de clase
SELECT 
    'DISTRIBUCIÓN POR CLASE' as tipo,
    cl.nombre,
    COUNT(*) as total_reservas,
    COUNT(CASE WHEN rc.estado = 'ASISTIO' THEN 1 END) as asistencias,
    ROUND(COUNT(CASE WHEN rc.estado = 'ASISTIO' THEN 1 END) * 100.0 / COUNT(*), 2) as tasa_asistencia_pct
FROM reservas_clase rc
JOIN clases cl ON rc.id_clase = cl.id_clase
GROUP BY cl.id_clase, cl.nombre
ORDER BY total_reservas DESC;

-- Tendencias mensuales (últimos 12 meses)
SELECT 
    'TENDENCIAS MENSUALES' as tipo,
    DATE_FORMAT(fecha_reserva, '%Y-%m') as mes,
    COUNT(*) as total_reservas,
    COUNT(CASE WHEN estado = 'ASISTIO' THEN 1 END) as asistencias,
    COUNT(CASE WHEN estado = 'CANCELADO' THEN 1 END) as cancelaciones
FROM reservas_clase 
WHERE fecha_reserva >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH)
GROUP BY DATE_FORMAT(fecha_reserva, '%Y-%m')
ORDER BY mes;

-- Datos de los deportistas más activos
SELECT 
    'DEPORTISTAS MÁS ACTIVOS' as tipo,
    p.nombre,
    p.apellido,
    COUNT(*) as total_reservas,
    COUNT(CASE WHEN rc.estado = 'ASISTIO' THEN 1 END) as asistencias
FROM reservas_clase rc
JOIN personas p ON rc.id_persona = p.id_persona
WHERE p.rol = 'DEPORTISTA'
GROUP BY p.id_persona, p.nombre, p.apellido
ORDER BY total_reservas DESC
LIMIT 10;

COMMIT;
