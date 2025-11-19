-- Script para poblar la tabla reservas_clase con 2 años de datos históricos
-- Base de datos: PostgreSQL
-- Generación de datos para análisis de asistencia por año y mes

-- Primero, crear clases adicionales si no existen suficientes
INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Spinning Intensivo', 
    'Clase de alta intensidad en bicicleta estática con música motivadora.',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    20, 
    CURRENT_DATE - INTERVAL '1 month', 
    60, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Spinning Intensivo' AND DATE(fecha) = CURRENT_DATE - INTERVAL '1 month');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Yoga Relajación', 
    'Sesión suave de estiramiento y respiración para reducir el estrés.',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    25, 
    CURRENT_DATE - INTERVAL '2 months', 
    60, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Yoga Relajación' AND DATE(fecha) = CURRENT_DATE - INTERVAL '2 months');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'CrossFit Funcional', 
    'Entrenamiento funcional de alta intensidad con equipos variados.',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    15, 
    CURRENT_DATE - INTERVAL '3 months', 
    45, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='CrossFit Funcional' AND DATE(fecha) = CURRENT_DATE - INTERVAL '3 months');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Zumba Fitness', 
    'Clase de baile fitness con música latina y coreografías energéticas.',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    30, 
    CURRENT_DATE - INTERVAL '1 month', 
    50, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Zumba Fitness' AND DATE(fecha) = CURRENT_DATE - INTERVAL '1 month');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Pilates Core', 
    'Ejercicios de fortalecimiento del core y flexibilidad avanzada.',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    20, 
    CURRENT_DATE - INTERVAL '2 months', 
    55, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Pilates Core' AND DATE(fecha) = CURRENT_DATE - INTERVAL '2 months');

-- Generar datos históricos para los últimos 24 meses
-- Usaremos una función PostgreSQL para generar las fechas y datos

DO $$
DECLARE
    month_offset INTEGER;
    class_names TEXT[] := ARRAY['Spinning Intensivo', 'Yoga Relajación', 'CrossFit Funcional', 'Zumba Fitness', 'Pilates Core'];
    class_descriptions TEXT[] := ARRAY[
        'Clase de alta intensidad en bicicleta estática con música motivadora.',
        'Sesión suave de estiramiento y respiración para reducir el estrés.',
        'Entrenamiento funcional de alta intensidad con equipos variados.',
        'Clase de baile fitness con música latina y coreografías energéticas.',
        'Ejercicios de fortalecimiento del core y flexibilidad avanzada.'
    ];
    class_index INTEGER;
    class_date DATE;
    class_id BIGINT;
    reservation_count INTEGER;
    attendance_rate NUMERIC;
BEGIN
    -- Generar datos para los últimos 24 meses
    FOR month_offset IN 23..0 LOOP
        class_date := CURRENT_DATE - INTERVAL (month_offset || ' months');
        class_index := (month_offset % 5) + 1;
        
        -- Insertar 2-4 clases por mes para variar los datos
        FOR class_iteration IN 1..(2 + (month_offset % 3)) LOOP
            -- Insertar clase
            INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
            SELECT 
                class_names[class_index],
                class_descriptions[class_index],
                (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
                CASE WHEN class_index IN (1,3) THEN 20 WHEN class_index = 4 THEN 30 ELSE 25 END,
                class_date + INTERVAL (class_iteration * 7 || ' days'),
                CASE WHEN class_index = 3 THEN 50 ELSE 60 END,
                CASE WHEN month_offset > 2 THEN 'Completada' ELSE 'Programada' END
            RETURNING id INTO class_id;
            
            -- Determinar número de reservas y tasa de asistencia
            reservation_count := 12 + FLOOR(RANDOM() * 13); -- 12-24 reservas
            attendance_rate := CASE 
                WHEN month_offset > 12 THEN 0.75 + (RANDOM() * 0.2) -- 75-95% para meses más antiguos
                WHEN month_offset > 6 THEN 0.80 + (RANDOM() * 0.15) -- 80-95% para meses intermedios
                ELSE 0.85 + (RANDOM() * 0.10) -- 85-95% para meses recientes
            END;
            
            -- Insertar reservas para esta clase
            INSERT INTO reservas_clase (clase_id, deportista_id, estado, reservado_en)
            SELECT 
                class_id,
                p.id,
                CASE 
                    WHEN row_number < (reservation_count * attendance_rate) THEN 'Asistió'
                    WHEN row_number < reservation_count THEN 'Cancelado'
                    ELSE 'Reservado'
                END,
                class_date + INTERVAL (class_iteration * 7 || ' days') - INTERVAL (FLOOR(RANDOM() * 5 + 1) || ' days')
            FROM (
                SELECT p.id, ROW_NUMBER() OVER (ORDER BY p.id) as row_number
                FROM personas p
                WHERE p.membresia_activa = TRUE 
                  AND p.activo = TRUE
                LIMIT reservation_count
            ) p;
        END LOOP;
    END LOOP;
END $$;

-- Agregar clases específicas para análisis estacional
-- Clases de verano (diciembre - febrero)
DO $$
DECLARE
    summer_dates DATE[] := ARRAY[
        CURRENT_DATE - INTERVAL '14 months',
        CURRENT_DATE - INTERVAL '14 months' + INTERVAL '7 days',
        CURRENT_DATE - INTERVAL '14 months' + INTERVAL '14 days',
        CURRENT_DATE - INTERVAL '2 months',
        CURRENT_DATE - INTERVAL '2 months' + INTERVAL '7 days',
        CURRENT_DATE - INTERVAL '2 months' + INTERVAL '14 days'
    ];
    summer_class_id BIGINT;
BEGIN
    FOR i IN 1..ARRAY_LENGTH(summer_dates, 1) LOOP
        INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
        SELECT 
            'Spinning Verano', 
            'Clase especial de verano con música tropical y mayor intensidad.',
            (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
            25, 
            summer_dates[i], 
            60, 
            'Completada'
        RETURNING id INTO summer_class_id;
        
        -- Más reservas en verano (temporada alta)
        INSERT INTO reservas_clase (clase_id, deportista_id, estado, reservado_en)
        SELECT 
            summer_class_id,
            p.id,
            CASE WHEN RANDOM() > 0.1 THEN 'Asistió' ELSE 'Cancelado' END,
            summer_dates[i] - INTERVAL (FLOOR(RANDOM() * 7) || ' days')
        FROM (
            SELECT p.id
            FROM personas p
            WHERE p.membresia_activa = TRUE 
              AND p.activo = TRUE
            ORDER BY RANDOM()
            LIMIT 25 -- Más reservas en temporada alta
        ) p;
    END LOOP;
END $$;

-- Clases de invierno (junio - agosto)
DO $$
DECLARE
    winter_dates DATE[] := ARRAY[
        CURRENT_DATE - INTERVAL '8 months',
        CURRENT_DATE - INTERVAL '8 months' + INTERVAL '7 days',
        CURRENT_DATE - INTERVAL '8 months' + INTERVAL '14 days',
        CURRENT_DATE - INTERVAL '6 months',
        CURRENT_DATE - INTERVAL '6 months' + INTERVAL '7 days',
        CURRENT_DATE - INTERVAL '6 months' + INTERVAL '14 days'
    ];
    winter_class_id BIGINT;
BEGIN
    FOR i IN 1..ARRAY_LENGTH(winter_dates, 1) LOOP
        INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
        SELECT 
            'Yoga Invierno', 
            'Clase especial de invierno con ejercicios de calentamiento y relajación profunda.',
            (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
            20, 
            winter_dates[i], 
            60, 
            'Completada'
        RETURNING id INTO winter_class_id;
        
        -- Menos reservas en invierno (temporada baja)
        INSERT INTO reservas_clase (clase_id, deportista_id, estado, reservado_en)
        SELECT 
            winter_class_id,
            p.id,
            CASE WHEN RANDOM() > 0.2 THEN 'Asistió' ELSE 'Cancelado' END,
            winter_dates[i] - INTERVAL (FLOOR(RANDOM() * 7) || ' days')
        FROM (
            SELECT p.id
            FROM personas p
            WHERE p.membresia_activa = TRUE 
              AND p.activo = TRUE
            ORDER BY RANDOM()
            LIMIT 15 -- Menos reservas en temporada baja
        ) p;
    END LOOP;
END $$;

-- Resumen de datos generados
SELECT 
    '=== RESUMEN DE DATOS GENERADOS ===' as info,
    '' as separator;

SELECT 
    COUNT(DISTINCT c.id) as total_clases_generadas,
    COUNT(DISTINCT rc.deportista_id) as deportistas_con_reservas,
    COUNT(rc.id) as total_reservas,
    COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) as total_asistencias,
    COUNT(CASE WHEN rc.estado = 'Cancelado' THEN 1 END) as total_cancelaciones,
    COUNT(CASE WHEN rc.estado = 'Reservado' THEN 1 END) as reservas_activas,
    ROUND(
        COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) * 100.0 / 
        NULLIF(COUNT(rc.id), 0), 2
    ) as tasa_asistencia_general_pct
FROM clases c
LEFT JOIN reservas_clase rc ON c.id = rc.clase_id;

-- Análisis por año
SELECT 
    EXTRACT(YEAR FROM c.fecha) as año,
    COUNT(DISTINCT c.id) as clases_año,
    COUNT(rc.id) as reservas_año,
    COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) as asistencias_año,
    ROUND(
        COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) * 100.0 / 
        NULLIF(COUNT(rc.id), 0), 2
    ) as tasa_asistencia_año_pct
FROM clases c
LEFT JOIN reservas_clase rc ON c.id = rc.clase_id
GROUP BY EXTRACT(YEAR FROM c.fecha)
ORDER BY año DESC;

-- Análisis por mes (últimos 12 meses)
SELECT 
    EXTRACT(YEAR FROM c.fecha) as año,
    EXTRACT(MONTH FROM c.fecha) as mes,
    TO_CHAR(c.fecha, 'Month') as nombre_mes,
    COUNT(DISTINCT c.id) as clases_mes,
    COUNT(rc.id) as reservas_mes,
    COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) as asistencias_mes,
    ROUND(
        COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) * 100.0 / 
        NULLIF(COUNT(rc.id), 0), 2
    ) as tasa_asistencia_mes_pct
FROM clases c
LEFT JOIN reservas_clase rc ON c.id = rc.clase_id
WHERE c.fecha >= CURRENT_DATE - INTERVAL '12 months'
GROUP BY EXTRACT(YEAR FROM c.fecha), EXTRACT(MONTH FROM c.fecha), TO_CHAR(c.fecha, 'Month')
ORDER BY año DESC, mes DESC;

-- Análisis por tipo de clase
SELECT 
    c.nombre as tipo_clase,
    COUNT(DISTINCT c.id) as total_clases_tipo,
    COUNT(rc.id) as total_reservas_tipo,
    COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) as total_asistencias_tipo,
    ROUND(
        COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) * 100.0 / 
        NULLIF(COUNT(rc.id), 0), 2
    ) as tasa_asistencia_tipo_pct,
    ROUND(AVG(c.capacidad), 2) as capacidad_promedio
FROM clases c
LEFT JOIN reservas_clase rc ON c.id = rc.clase_id
GROUP BY c.nombre
ORDER BY total_asistencias_tipo DESC;

-- Top 10 deportistas con más asistencias
SELECT 
    p.nombre || ' ' || p.apellido as deportista,
    p.email,
    COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) as total_asistencias,
    COUNT(rc.id) as total_reservas,
    ROUND(
        COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) * 100.0 / 
        NULLIF(COUNT(rc.id), 0), 2
    ) as tasa_asistencia_pct
FROM personas p
JOIN reservas_clase rc ON p.id = rc.deportista_id
GROUP BY p.id, p.nombre, p.apellido, p.email
ORDER BY total_asistencias DESC
LIMIT 10;

-- Tendencia mensual de asistencia (últimos 24 meses)
SELECT 
    DATE_TRUNC('month', c.fecha) as mes,
    COUNT(DISTINCT c.id) as clases_mes,
    COUNT(rc.id) as reservas_mes,
    COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) as asistencias_mes,
    ROUND(
        COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) * 100.0 / 
        NULLIF(COUNT(rc.id), 0), 2
    ) as tasa_asistencia_mes_pct,
    ROUND(AVG(c.capacidad), 2) as capacidad_promedio_mes
FROM clases c
LEFT JOIN reservas_clase rc ON c.id = rc.clase_id
WHERE c.fecha >= CURRENT_DATE - INTERVAL '24 months'
GROUP BY DATE_TRUNC('month', c.fecha)
ORDER BY mes DESC;

SELECT 
    '=== DATOS LISTOS PARA ANÁLISIS ===' as info,
    'Ejecuta las consultas anteriores para obtener insights sobre asistencia.' as mensaje;
