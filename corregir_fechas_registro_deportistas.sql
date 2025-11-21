-- Script para corregir la fecha de registro de deportistas y asegurar que existan en 2024

DO $$
DECLARE
    deportista_record RECORD;
    counter INT := 0;
BEGIN
    RAISE NOTICE 'Actualizando fecha de registro de hasta 50 deportistas...';

    -- Seleccionar hasta 50 deportistas activos y con membresía
    FOR deportista_record IN 
        SELECT id 
        FROM public.personas 
        WHERE membresia_activa = TRUE AND activo = TRUE
        ORDER BY fecha_registro DESC -- Empezar por los más recientes
        LIMIT 50
    LOOP
        -- Actualizar su fecha de registro a una fecha aleatoria en enero de 2024
        UPDATE public.personas
        SET fecha_registro = '2024-01-01'::date + (floor(random() * 30) * interval '1 day')
        WHERE id = deportista_record.id;
        
        counter := counter + 1;
    END LOOP;

    RAISE NOTICE '% deportistas actualizados al inicio de 2024.', counter;
END $$;

-- Verificar las fechas de registro actualizadas
SELECT 
    MIN(fecha_registro) AS primera_fecha_registro,
    MAX(fecha_registro) AS ultima_fecha_registro,
    COUNT(*) AS total_deportistas_actualizados
FROM public.personas
WHERE fecha_registro >= '2024-01-01' AND fecha_registro < '2024-02-01';
