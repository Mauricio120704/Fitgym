-- Script para eliminar todos los datos de la tabla reservas_clase

DELETE FROM public.reservas_clase;

-- Reiniciar el secuencial de ID (opcional)
ALTER SEQUENCE public.reservas_clase_id_seq RESTART WITH 1;

-- Verificar que la tabla está vacía
SELECT COUNT(*) AS total_reservas FROM public.reservas_clase;
