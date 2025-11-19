-- Script para poblar la tabla reservas_clase con 2 años de datos históricos
-- Basado en los deportistas existentes y sus fechas de registro

-- Primero, asegurémonos de que tenemos suficientes clases en el sistema
INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Spinning', 
    'Clase de alta intensidad en bicicleta estática.',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    20, 
    CURRENT_DATE - INTERVAL '1 month', 
    60, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Spinning' AND DATE(fecha) = CURRENT_DATE - INTERVAL '1 month');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Yoga Matutino', 
    'Sesión suave de estiramiento y respiración',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    25, 
    CURRENT_DATE - INTERVAL '2 months', 
    60, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Yoga Matutino' AND DATE(fecha) = CURRENT_DATE - INTERVAL '2 months');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'CrossFit', 
    'Entrenamiento funcional de alta intensidad',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    15, 
    CURRENT_DATE - INTERVAL '3 months', 
    45, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='CrossFit' AND DATE(fecha) = CURRENT_DATE - INTERVAL '3 months');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Zumba', 
    'Clase de baile fitness con música latina',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    30, 
    CURRENT_DATE - INTERVAL '1 month', 
    50, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Zumba' AND DATE(fecha) = CURRENT_DATE - INTERVAL '1 month');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Pilates', 
    'Ejercicios de fortalecimiento y flexibilidad',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    20, 
    CURRENT_DATE - INTERVAL '2 months', 
    55, 
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Pilates' AND DATE(fecha) = CURRENT_DATE - INTERVAL '2 months');

-- Generar datos históricos de reservas para los últimos 2 años
-- Crearemos múltiples instancias de cada clase para tener una buena distribución

-- Función para generar reservas para un rango de fechas
-- Para cada mes de los últimos 2 años, generaremos clases y reservas

-- Año 2023 (hace 1 año y varios meses)
INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    CASE WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 1 THEN 'Spinning'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 2 THEN 'Yoga Matutino'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 3 THEN 'CrossFit'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 4 THEN 'Zumba'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 5 THEN 'Pilates'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 6 THEN 'Spinning'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 7 THEN 'Yoga Matutino'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 8 THEN 'CrossFit'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 9 THEN 'Zumba'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 10 THEN 'Pilates'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 11 THEN 'Spinning'
         ELSE 'Yoga Matutino' END,
    CASE WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 1 THEN 'Clase de alta intensidad en bicicleta estática.'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 2 THEN 'Sesión suave de estiramiento y respiración'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 3 THEN 'Entrenamiento funcional de alta intensidad'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 4 THEN 'Clase de baile fitness con música latina'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 5 THEN 'Ejercicios de fortalecimiento y flexibilidad'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 6 THEN 'Clase de alta intensidad en bicicleta estática.'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 7 THEN 'Sesión suave de estiramiento y respiración'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 8 THEN 'Entrenamiento funcional de alta intensidad'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 9 THEN 'Clase de baile fitness con música latina'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 10 THEN 'Ejercicios de fortalecimiento y flexibilidad'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') = 11 THEN 'Clase de alta intensidad en bicicleta estática.'
         ELSE 'Sesión suave de estiramiento y respiración' END,
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    CASE WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '18 months') IN (1,3,5,7,9,11) THEN 20 ELSE 25 END,
    CURRENT_DATE - INTERVAL '18 months',
    60,
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE DATE(fecha) = CURRENT_DATE - INTERVAL '18 months');

-- Generar reservas históricas usando los deportistas existentes
-- Mes 1: Hace 18 meses
INSERT INTO reservas_clase (clase_id, deportista_id, estado, reservado_en)
SELECT 
    c.id,
    p.id,
    CASE WHEN RANDOM() > 0.2 THEN 'Asistió' ELSE 'Cancelado' END,
    c.fecha - INTERVAL '2 days'
FROM clases c
CROSS JOIN personas p
WHERE DATE(c.fecha) = CURRENT_DATE - INTERVAL '18 months'
  AND p.membresia_activa = TRUE
  AND p.activo = TRUE
  AND NOT EXISTS (
    SELECT 1 FROM reservas_clase rc 
    WHERE rc.clase_id = c.id AND rc.deportista_id = p.id
  )
LIMIT 15;

-- Mes 2: Hace 17 meses
INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    CASE WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 1 THEN 'Yoga Matutino'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 2 THEN 'CrossFit'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 3 THEN 'Zumba'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 4 THEN 'Pilates'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 5 THEN 'Spinning'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 6 THEN 'Yoga Matutino'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 7 THEN 'CrossFit'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 8 THEN 'Zumba'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 9 THEN 'Pilates'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 10 THEN 'Spinning'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 11 THEN 'Yoga Matutino'
         ELSE 'CrossFit' END,
    CASE WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 1 THEN 'Sesión suave de estiramiento y respiración'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 2 THEN 'Entrenamiento funcional de alta intensidad'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 3 THEN 'Clase de baile fitness con música latina'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 4 THEN 'Ejercicios de fortalecimiento y flexibilidad'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 5 THEN 'Clase de alta intensidad en bicicleta estática.'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 6 THEN 'Sesión suave de estiramiento y respiración'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 7 THEN 'Entrenamiento funcional de alta intensidad'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 8 THEN 'Clase de baile fitness con música latina'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 9 THEN 'Ejercicios de fortalecimiento y flexibilidad'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 10 THEN 'Clase de alta intensidad en bicicleta estática.'
         WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') = 11 THEN 'Sesión suave de estiramiento y respiración'
         ELSE 'Entrenamiento funcional de alta intensidad' END,
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    CASE WHEN EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '17 months') IN (2,4,6,8,10,12) THEN 25 ELSE 20 END,
    CURRENT_DATE - INTERVAL '17 months',
    60,
    'Completada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE DATE(fecha) = CURRENT_DATE - INTERVAL '17 months');

INSERT INTO reservas_clase (clase_id, deportista_id, estado, reservado_en)
SELECT 
    c.id,
    p.id,
    CASE WHEN RANDOM() > 0.15 THEN 'Asistió' ELSE 'Cancelado' END,
    c.fecha - INTERVAL '1 day'
FROM clases c
CROSS JOIN personas p
WHERE DATE(c.fecha) = CURRENT_DATE - INTERVAL '17 months'
  AND p.membresia_activa = TRUE
  AND p.activo = TRUE
  AND NOT EXISTS (
    SELECT 1 FROM reservas_clase rc 
    WHERE rc.clase_id = c.id AND rc.deportista_id = p.id
  )
LIMIT 18;

-- Continuar con los meses restantes para completar 2 años de datos
-- Generaremos datos para 24 meses en total

DO $$
DECLARE
    month_offset INTEGER;
    class_types TEXT[] := ARRAY['Spinning', 'Yoga Matutino', 'CrossFit', 'Zumba', 'Pilates'];
    class_descriptions TEXT[] := ARRAY[
        'Clase de alta intensidad en bicicleta estática.',
        'Sesión suave de estiramiento y respiración',
        'Entrenamiento funcional de alta intensidad',
        'Clase de baile fitness con música latina',
        'Ejercicios de fortalecimiento y flexibilidad'
    ];
    selected_class TEXT;
    selected_description TEXT;
BEGIN
    FOR month_offset IN 16..1 LOOP
        -- Seleccionar tipo de clase basado en el mes
        selected_class := class_types[(month_offset % 5) + 1];
        selected_description := class_descriptions[(month_offset % 5) + 1];
        
        -- Insertar clase para este mes
        INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
        SELECT 
            selected_class,
            selected_description,
            (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
            CASE WHEN month_offset % 2 = 0 THEN 20 ELSE 25 END,
            CURRENT_DATE - INTERVAL (month_offset || ' months'),
            60,
            'Completada'
        WHERE NOT EXISTS (SELECT 1 FROM clases WHERE DATE(fecha) = CURRENT_DATE - INTERVAL (month_offset || ' months'))
        RETURNING id INTO class_id;
        
        -- Insertar reservas para esta clase
        INSERT INTO reservas_clase (clase_id, deportista_id, estado, reservado_en)
        SELECT 
            c.id,
            p.id,
            CASE WHEN RANDOM() > 0.25 THEN 'Asistió' ELSE 'Cancelado' END,
            c.fecha - INTERVAL (FLOOR(RANDOM() * 3 + 1) || ' days')
        FROM clases c
        CROSS JOIN personas p
        WHERE DATE(c.fecha) = CURRENT_DATE - INTERVAL (month_offset || ' months')
          AND p.membresia_activa = TRUE
          AND p.activo = TRUE
          AND NOT EXISTS (
            SELECT 1 FROM reservas_clase rc 
            WHERE rc.clase_id = c.id AND rc.deportista_id = p.id
          )
        LIMIT (12 + FLOOR(RANDOM() * 8)); -- Entre 12 y 20 reservas por clase
    END LOOP;
END $$;

-- Generar datos para el año actual (últimos 12 meses)
DO $$
DECLARE
    month_offset INTEGER;
    class_types TEXT[] := ARRAY['Spinning', 'Yoga Matutino', 'CrossFit', 'Zumba', 'Pilates'];
    class_descriptions TEXT[] := ARRAY[
        'Clase de alta intensidad en bicicleta estática.',
        'Sesión suave de estiramiento y respiración',
        'Entrenamiento funcional de alta intensidad',
        'Clase de baile fitness con música latina',
        'Ejercicios de fortalecimiento y flexibilidad'
    ];
BEGIN
    FOR month_offset IN 12..1 LOOP
        -- Insertar múltiples clases por mes para el año actual
        INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
        SELECT 
            class_types[(month_offset % 5) + 1],
            class_descriptions[(month_offset % 5) + 1],
            (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
            CASE WHEN month_offset % 2 = 0 THEN 20 ELSE 25 END,
            CURRENT_DATE - INTERVAL (month_offset || ' months'),
            60,
            'Completada'
        WHERE NOT EXISTS (SELECT 1 FROM clases WHERE DATE(fecha) = CURRENT_DATE - INTERVAL (month_offset || ' months'));
        
        -- Insertar reservas con mayor tasa de asistencia para el año actual
        INSERT INTO reservas_clase (clase_id, deportista_id, estado, reservado_en)
        SELECT 
            c.id,
            p.id,
            CASE WHEN RANDOM() > 0.1 THEN 'Asistió' ELSE 'Cancelado' END, -- Mayor tasa de asistencia
            c.fecha - INTERVAL (FLOOR(RANDOM() * 3 + 1) || ' days')
        FROM clases c
        CROSS JOIN personas p
        WHERE DATE(c.fecha) = CURRENT_DATE - INTERVAL (month_offset || ' months')
          AND p.membresia_activa = TRUE
          AND p.activo = TRUE
          AND NOT EXISTS (
            SELECT 1 FROM reservas_clase rc 
            WHERE rc.clase_id = c.id AND rc.deportista_id = p.id
          )
        LIMIT (15 + FLOOR(RANDOM() * 10)); -- Entre 15 y 25 reservas por clase
    END LOOP;
END $$;

-- Agregar algunas clases futuras para tener reservas activas
INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Spinning', 
    'Clase de alta intensidad en bicicleta estática.',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    20, 
    CURRENT_DATE + INTERVAL '3 days', 
    60, 
    'Programada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Spinning' AND DATE(fecha) = CURRENT_DATE + INTERVAL '3 days');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'Yoga Matutino', 
    'Sesión suave de estiramiento y respiración',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    25, 
    CURRENT_DATE + INTERVAL '5 days', 
    60, 
    'Programada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Yoga Matutino' AND DATE(fecha) = CURRENT_DATE + INTERVAL '5 days');

INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 
    'CrossFit', 
    'Entrenamiento funcional de alta intensidad',
    (SELECT id FROM personas WHERE email='carlos.lopez@email.com' LIMIT 1),
    15, 
    CURRENT_DATE + INTERVAL '7 days', 
    45, 
    'Programada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='CrossFit' AND DATE(fecha) = CURRENT_DATE + INTERVAL '7 days');

-- Reservas para clases futuras (estado "Reservado")
INSERT INTO reservas_clase (clase_id, deportista_id, estado, reservado_en)
SELECT 
    c.id,
    p.id,
    'Reservado',
    CURRENT_TIMESTAMP - INTERVAL (FLOOR(RANDOM() * 7) || ' days')
FROM clases c
CROSS JOIN personas p
WHERE c.estado = 'Programada'
  AND p.membresia_activa = TRUE
  AND p.activo = TRUE
  AND NOT EXISTS (
    SELECT 1 FROM reservas_clase rc 
    WHERE rc.clase_id = c.id AND rc.deportista_id = p.id
  )
LIMIT 8;

-- Actualizar estadísticas
SELECT 
    'Resumen de datos generados:' as info,
    COUNT(DISTINCT c.id) as total_clases,
    COUNT(DISTINCT rc.deportista_id) as deportistas_con_reservas,
    COUNT(rc.id) as total_reservas,
    COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) as asistencias,
    COUNT(CASE WHEN rc.estado = 'Cancelado' THEN 1 END) as cancelaciones,
    COUNT(CASE WHEN rc.estado = 'Reservado' THEN 1 END) as reservas_activas,
    ROUND(COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) * 100.0 / COUNT(rc.id), 2) as tasa_asistencia_pct
FROM clases c
LEFT JOIN reservas_clase rc ON c.id = rc.clase_id;

-- Distribución por mes y año para análisis
SELECT 
    EXTRACT(YEAR FROM c.fecha) as año,
    EXTRACT(MONTH FROM c.fecha) as mes,
    COUNT(DISTINCT c.id) as clases_mes,
    COUNT(rc.id) as reservas_mes,
    COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) as asistencias_mes,
    ROUND(COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) * 100.0 / NULLIF(COUNT(rc.id), 0), 2) as tasa_asistencia_mes_pct
FROM clases c
LEFT JOIN reservas_clase rc ON c.id = rc.clase_id
GROUP BY EXTRACT(YEAR FROM c.fecha), EXTRACT(MONTH FROM c.fecha)
ORDER BY año DESC, mes DESC;
