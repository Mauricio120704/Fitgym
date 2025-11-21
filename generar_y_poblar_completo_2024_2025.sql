-- Script completo para generar clases y reservas para TODO 2024 y 2025
-- Primero elimina clases sin reservas, luego regenera todo

DO $$
DECLARE
    current_month DATE;
    tipo_clase_id BIGINT;
    entrenador_id BIGINT;
    classes_per_month INT := 15;
    j INT;
    day_offset INT;
    class_hour INT;
    days_in_month INT;
    class_date TIMESTAMP WITH TIME ZONE;
    classes_created INT := 0;
BEGIN
    -- Obtener un tipo de clase válido
    SELECT id INTO tipo_clase_id FROM public.tipos_clase LIMIT 1;
    IF tipo_clase_id IS NULL THEN
        RAISE EXCEPTION 'No hay tipos de clase disponibles';
    END IF;
    
    -- Obtener un entrenador válido
    SELECT id INTO entrenador_id FROM public.usuarios WHERE rol_id IS NOT NULL LIMIT 1;
    
    RAISE NOTICE 'Iniciando generación de clases para 2024 y 2025...';
    
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
            class_hour := 6 + MOD(j, 18);
            
            class_date := current_month + (day_offset - 1) * interval '1 day' + (class_hour * interval '1 hour');
            
            -- Insertar clase (ignorar si ya existe)
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
                'Clase ' || TO_CHAR(class_date, 'DD/MM/YYYY HH24:00'),
                'Clase generada para ' || TO_CHAR(current_month, 'Month YYYY'),
                entrenador_id,
                25,
                class_date,
                60,
                'Programada',
                tipo_clase_id,
                FALSE,
                TRUE
            )
            ON CONFLICT DO NOTHING;
            
            classes_created := classes_created + 1;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Clases procesadas: %', classes_created;
END $$;

-- Verificar clases generadas
SELECT
    DATE_TRUNC('month', fecha)::date AS mes,
    COUNT(*) AS total_clases
FROM public.clases
WHERE fecha >= '2024-01-01'::timestamp with time zone
  AND fecha < '2026-01-01'::timestamp with time zone
GROUP BY DATE_TRUNC('month', fecha)
ORDER BY mes ASC;

-- Ahora poblar las reservas
DO $$
DECLARE
    class_record RECORD;
    person_record RECORD;
    reservations_to_create INT;
    i INT;
    reservation_date TIMESTAMP WITH TIME ZONE;
    total_personas INT;
    reservas_creadas INT := 0;
BEGIN
    -- Contar total de personas activas con membresía
    SELECT COUNT(*) INTO total_personas
    FROM public.personas
    WHERE membresia_activa = TRUE AND activo = TRUE;
    
    IF total_personas = 0 THEN
        RAISE EXCEPTION 'No hay personas activas con membresía para crear reservas';
    END IF;
    
    RAISE NOTICE 'Total de personas activas: %, iniciando poblado de reservas...', total_personas;
    
    -- Iterar sobre cada clase de 2024 y 2025
    FOR class_record IN 
        SELECT id, fecha, capacidad 
        FROM public.clases 
        WHERE fecha >= '2024-01-01'::timestamp with time zone
          AND fecha < '2026-01-01'::timestamp with time zone
        ORDER BY fecha ASC
    LOOP
        -- Decidir aleatoriamente cuántas reservas crear para esta clase
        reservations_to_create := floor(random() * (class_record.capacidad * 0.5) + (class_record.capacidad * 0.4))::INT;
        
        -- Insertar el número decidido de reservas para la clase actual
        FOR i IN 1..reservations_to_create LOOP
            -- Seleccionar un deportista al azar
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
                reservation_date := class_record.fecha - (floor(random() * 7 + 1) * interval '1 day');
                
                INSERT INTO public.reservas_clase (clase_id, deportista_id, estado, reservado_en)
                VALUES (
                    class_record.id,
                    person_record.id,
                    CASE
                        WHEN class_record.fecha < NOW() THEN 'Asistió'
                        ELSE 'Reservado'
                    END,
                    reservation_date
                );
                
                reservas_creadas := reservas_creadas + 1;
            END IF;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Reservas creadas: %', reservas_creadas;
END $$;

-- Reporte final
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
