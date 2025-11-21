-- Script para limpiar y repoblar reservas_clase

-- Paso 1 y 2: Eliminar todas las reservas y poblar nuevamente
DO $$
DECLARE
    class_record RECORD;
    person_record RECORD;
    reservations_to_create INT;
    i INT;
    reservation_date TIMESTAMP WITH TIME ZONE;
    total_personas INT;
    reservas_creadas INT := 0;
    clases_procesadas INT := 0;
BEGIN
    -- Eliminar todas las reservas
    DELETE FROM public.reservas_clase;
    
    -- Reiniciar el secuencial de ID
    ALTER SEQUENCE public.reservas_clase_id_seq RESTART WITH 1;
    
    RAISE NOTICE 'Reservas eliminadas. Iniciando poblado...';
    
    -- Contar total de personas activas con membresía
    SELECT COUNT(*) INTO total_personas
    FROM public.personas
    WHERE membresia_activa = TRUE AND activo = TRUE;
    
    IF total_personas = 0 THEN
        RAISE EXCEPTION 'No hay personas activas con membresía para crear reservas';
    END IF;
    
    RAISE NOTICE 'Total de personas activas: %', total_personas;
    
    -- Iterar sobre cada clase de 2024 y 2025
    FOR class_record IN 
        SELECT id, fecha, capacidad 
        FROM public.clases 
        WHERE fecha >= '2024-01-01'::timestamp with time zone
          AND fecha < '2026-01-01'::timestamp with time zone
        ORDER BY fecha ASC
    LOOP
        clases_procesadas := clases_procesadas + 1;
        
        -- Decidir aleatoriamente cuántas reservas crear para esta clase
        -- Entre el 40% y el 90% de su capacidad
        reservations_to_create := floor(random() * (class_record.capacidad * 0.5) + (class_record.capacidad * 0.4))::INT;
        
        -- Insertar el número decidido de reservas para la clase actual
        FOR i IN 1..reservations_to_create LOOP
            -- Seleccionar un deportista al azar que no tenga reserva para esta clase
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
    
    RAISE NOTICE 'Clases procesadas: %, Reservas creadas: %', clases_procesadas, reservas_creadas;
END $$;

-- Paso 3: Reporte final
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

-- Resumen general
SELECT
    COUNT(*) AS total_reservas,
    COUNT(CASE WHEN estado = 'Asistió' THEN 1 END) AS total_asistencias,
    COUNT(CASE WHEN estado = 'Reservado' THEN 1 END) AS total_reservas_activas,
    COUNT(DISTINCT clase_id) AS total_clases_con_reservas,
    COUNT(DISTINCT deportista_id) AS total_deportistas_con_reservas
FROM public.reservas_clase;
