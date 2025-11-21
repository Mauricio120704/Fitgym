-- Script para poblar únicamente la tabla reservas_clase con datos coherentes
-- Utiliza deportistas y clases existentes en la base de datos.

DO $$
DECLARE
    class_record RECORD;
    person_record RECORD;
    reservations_to_create INT;
    i INT;
BEGIN
    -- Iterar sobre cada clase que ya existe en la tabla 'clases'
    FOR class_record IN SELECT id, fecha, capacidad FROM public.clases LOOP

        -- Decidir aleatoriamente cuántas reservas crear para esta clase, entre el 40% y el 90% de su capacidad.
        reservations_to_create := floor(random() * (class_record.capacidad * 0.5) + (class_record.capacidad * 0.4))::INT;

        -- Insertar el número decidido de reservas para la clase actual
        FOR i IN 1..reservations_to_create LOOP
            -- Seleccionar un deportista al azar que aún no tenga una reserva para esta clase.
            SELECT id INTO person_record
            FROM public.personas
            WHERE membresia_activa = TRUE AND activo = TRUE
              AND id NOT IN (SELECT deportista_id FROM public.reservas_clase WHERE clase_id = class_record.id)
            ORDER BY random()
            LIMIT 1;

            -- Si se encontró un deportista disponible, crear la reserva.
            IF FOUND THEN
                INSERT INTO public.reservas_clase (clase_id, deportista_id, estado, reservado_en)
                VALUES (
                    class_record.id,
                    person_record.id,
                    -- Si la fecha de la clase es en el pasado, marcar como 'Asistió'. Si es en el futuro, 'Reservado'.
                    CASE
                        WHEN class_record.fecha < NOW() THEN 'Asistió'
                        ELSE 'Reservado'
                    END,
                    -- Establecer la fecha de reserva entre 1 y 7 días antes de la clase.
                    class_record.fecha - (floor(random() * 7 + 1) * interval '1 day')
                );
            END IF;
        END LOOP;
    END LOOP;
END $$;

-- Consulta para verificar la cantidad de reservas creadas
SELECT 
    COUNT(*) AS total_reservas_creadas,
    COUNT(CASE WHEN estado = 'Asistió' THEN 1 END) AS total_asistencias,
    COUNT(CASE WHEN estado = 'Reservado' THEN 1 END) AS total_reservas_activas
FROM public.reservas_clase;
