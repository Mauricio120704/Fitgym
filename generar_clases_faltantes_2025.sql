-- Script para generar clases faltantes de junio a diciembre 2025

DO $$
DECLARE
    current_month DATE;
    tipo_clase_id BIGINT;
    entrenador_id BIGINT;
    classes_per_month INT := 15; -- 15 clases por mes
    j INT;
    day_offset INT;
    class_hour INT;
    days_in_month INT;
    class_date TIMESTAMP WITH TIME ZONE;
BEGIN
    -- Obtener un tipo de clase válido
    SELECT id INTO tipo_clase_id FROM public.tipos_clase LIMIT 1;
    IF tipo_clase_id IS NULL THEN
        RAISE EXCEPTION 'No hay tipos de clase disponibles';
    END IF;
    
    -- Obtener un entrenador válido (opcional)
    SELECT id INTO entrenador_id FROM public.usuarios WHERE rol_id IS NOT NULL LIMIT 1;
    
    -- Generar clases para junio a diciembre 2025
    FOR current_month IN 
        SELECT DATE_TRUNC('month', d)::date
        FROM generate_series('2025-06-01'::date, '2025-12-31'::date, '1 month'::interval) d
    LOOP
        RAISE NOTICE 'Generando clases para: %', TO_CHAR(current_month, 'Month YYYY');
        
        -- Calcular número de días en el mes
        days_in_month := EXTRACT(DAY FROM (DATE_TRUNC('month', current_month) + '1 month'::interval - '1 day'::interval))::INT;
        
        -- Crear clases distribuidas en el mes
        FOR j IN 1..classes_per_month LOOP
            -- Distribuir las clases uniformemente en el mes
            day_offset := FLOOR((j - 1) * days_in_month / classes_per_month)::INT + 1;
            class_hour := 6 + MOD(j, 18); -- Horas entre 6 AM y 11 PM
            
            class_date := current_month + (day_offset - 1) * interval '1 day' + (class_hour * interval '1 hour');
            
            -- Verificar si la clase ya existe para esa fecha y hora
            IF NOT EXISTS (
                SELECT 1 FROM public.clases 
                WHERE DATE_TRUNC('hour', fecha) = DATE_TRUNC('hour', class_date)
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
                    'Clase ' || TO_CHAR(class_date, 'DD/MM/YYYY HH24:00'),
                    'Clase generada para ' || TO_CHAR(current_month, 'Month YYYY'),
                    entrenador_id,
                    25, -- Capacidad de 25 personas
                    class_date,
                    60, -- Duración de 60 minutos
                    'Programada',
                    tipo_clase_id,
                    FALSE,
                    TRUE
                );
            END IF;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Clases generadas para junio a diciembre 2025';
END $$;

-- Verificar que las clases se crearon
SELECT
    DATE_TRUNC('month', fecha)::date AS mes,
    COUNT(*) AS total_clases
FROM public.clases
WHERE fecha >= '2025-06-01'::timestamp with time zone
  AND fecha < '2026-01-01'::timestamp with time zone
GROUP BY DATE_TRUNC('month', fecha)
ORDER BY mes ASC;
