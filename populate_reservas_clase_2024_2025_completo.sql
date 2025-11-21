-- Script para poblar la tabla reservas_clase con datos coherentes de 2024 y 2025
-- Genera clases para todos los meses si no existen, y luego crea reservas coherentes

DO $$
DECLARE
    class_record RECORD;
    person_record RECORD;
    reservations_to_create INT;
    i INT;
    j INT;
    reservation_date TIMESTAMP WITH TIME ZONE;
    current_date TIMESTAMP WITH TIME ZONE;
    current_month DATE;
    tipo_clase_id BIGINT;
    entrenador_id BIGINT;
    classes_per_month INT := 12; -- 12 clases por mes
    days_in_month INT;
    day_offset INT;
    class_hour INT;
BEGIN
    -- Obtener un tipo de clase válido
    SELECT id INTO tipo_clase_id FROM public.tipos_clase LIMIT 1;
    IF tipo_clase_id IS NULL THEN
        RAISE EXCEPTION 'No hay tipos de clase disponibles';
    END IF;
    
    -- Obtener un entrenador válido (opcional)
    SELECT id INTO entrenador_id FROM public.usuarios WHERE rol_id IS NOT NULL LIMIT 1;
    
    -- Generar clases para cada mes de 2024 y 2025
    FOR current_month IN 
        SELECT DATE_TRUNC('month', d)::date
        FROM generate_series('2024-01-01'::date, '2025-12-31'::date, '1 month'::interval) d
    LOOP
        -- Calcular número de días en el mes
        days_in_month := EXTRACT(DAY FROM (DATE_TRUNC('month', current_month) + '1 month'::interval - '1 day'::interval))::INT;
        
        -- Crear clases distribuidas en el mes
        FOR j IN 1..classes_per_month LOOP
            -- Distribuir las clases uniformemente en el mes
            day_offset := FLOOR((j - 1) * days_in_month / classes_per_month)::INT + 1;
            class_hour := 6 + MOD(j, 18); -- Horas entre 6 AM y 11 PM
            
            -- Verificar si la clase ya existe
            IF NOT EXISTS (
                SELECT 1 FROM public.clases 
                WHERE DATE_TRUNC('day', fecha) = current_month + (day_offset - 1) * interval '1 day'
                  AND EXTRACT(HOUR FROM fecha) = class_hour
            ) THEN
                INSERT INTO public.clases (
                    nombre, 
                    descripcion, 
                    entrenador_id, 
                    capacidad, 
                    fecha, 
                    duracion_minutos, 
                    estado, 
                    tipo_clase_id, 
                    es_pago, 
                    para_todos
                )
                VALUES (
                    'Clase ' || TO_CHAR(current_month + (day_offset - 1) * interval '1 day', 'DD/MM/YYYY HH24:00'),
                    'Clase generada automáticamente para ' || TO_CHAR(current_month, 'Month YYYY'),
                    entrenador_id,
                    20, -- Capacidad de 20 personas
                    current_month + (day_offset - 1) * interval '1 day' + (class_hour * interval '1 hour'),
                    60, -- Duración de 60 minutos
                    'Programada',
                    tipo_clase_id,
                    FALSE,
                    TRUE
                );
            END IF;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Clases generadas/verificadas para 2024 y 2025';
END $$;

-- Ahora poblar las reservas_clase con datos coherentes
DO $$
DECLARE
    class_record RECORD;
    person_record RECORD;
    reservations_to_create INT;
    i INT;
    reservation_date TIMESTAMP WITH TIME ZONE;
BEGIN
    -- Iterar sobre cada clase de 2024 y 2025
    FOR class_record IN 
        SELECT id, fecha, capacidad 
        FROM public.clases 
        WHERE fecha >= '2024-01-01'::timestamp with time zone
          AND fecha < '2026-01-01'::timestamp with time zone
        ORDER BY fecha ASC
    LOOP
        -- Decidir aleatoriamente cuántas reservas crear para esta clase
        -- Entre el 40% y el 90% de su capacidad
        reservations_to_create := floor(random() * (class_record.capacidad * 0.5) + (class_record.capacidad * 0.4))::INT;
        
        -- Insertar el número decidido de reservas para la clase actual
        FOR i IN 1..reservations_to_create LOOP
            -- Seleccionar un deportista al azar que:
            -- 1. Tenga membresía activa
            -- 2. Esté activo
            -- 3. No tenga ya una reserva para esta clase
            SELECT id INTO person_record
            FROM public.personas
            WHERE membresia_activa = TRUE 
              AND activo = TRUE
              AND id NOT IN (
                  SELECT deportista_id 
                  FROM public.reservas_clase 
                  WHERE clase_id = class_record.id
              )
            ORDER BY random()
            LIMIT 1;
            
            -- Si se encontró un deportista disponible, crear la reserva
            IF FOUND THEN
                -- Calcular fecha de reserva: entre 1 y 7 días antes de la clase
                reservation_date := class_record.fecha - (floor(random() * 7 + 1) * interval '1 day');
                
                INSERT INTO public.reservas_clase (clase_id, deportista_id, estado, reservado_en)
                VALUES (
                    class_record.id,
                    person_record.id,
                    -- Si la clase es en el pasado, marcar como 'Asistió'. Si es en el futuro, 'Reservado'.
                    CASE
                        WHEN class_record.fecha < NOW() THEN 'Asistió'
                        ELSE 'Reservado'
                    END,
                    reservation_date
                );
            END IF;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Poblado completado: reservas_clase para 2024 y 2025';
END $$;

-- Consulta para verificar la cantidad de reservas creadas
SELECT
    COUNT(*) AS total_reservas_creadas,
    COUNT(CASE WHEN estado = 'Asistió' THEN 1 END) AS total_asistencias,
    COUNT(CASE WHEN estado = 'Reservado' THEN 1 END) AS total_reservas_activas,
    COUNT(DISTINCT clase_id) AS total_clases_con_reservas,
    COUNT(DISTINCT deportista_id) AS total_deportistas_con_reservas
FROM public.reservas_clase;

-- Consulta adicional para ver distribución por mes
SELECT
    DATE_TRUNC('month', c.fecha)::date AS mes,
    COUNT(c.id) AS total_clases,
    COUNT(rc.id) AS total_reservas,
    COUNT(CASE WHEN rc.estado = 'Asistió' THEN 1 END) AS asistencias,
    COUNT(CASE WHEN rc.estado = 'Reservado' THEN 1 END) AS reservas_activas
FROM public.clases c
LEFT JOIN public.reservas_clase rc ON c.id = rc.clase_id
WHERE c.fecha >= '2024-01-01'::timestamp with time zone
  AND c.fecha < '2026-01-01'::timestamp with time zone
GROUP BY DATE_TRUNC('month', c.fecha)
ORDER BY mes ASC;
