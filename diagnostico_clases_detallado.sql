-- Diagnóstico detallado de clases

-- 1. Ver el rango completo de clases
SELECT
    MIN(fecha) AS fecha_minima,
    MAX(fecha) AS fecha_maxima,
    COUNT(*) AS total_clases
FROM public.clases;

-- 2. Ver clases por mes (2024-2025)
SELECT
    DATE_TRUNC('month', fecha)::date AS mes,
    COUNT(*) AS total_clases,
    MIN(fecha) AS primera_clase,
    MAX(fecha) AS ultima_clase
FROM public.clases
WHERE fecha >= '2024-01-01'::timestamp with time zone
  AND fecha < '2026-01-01'::timestamp with time zone
GROUP BY DATE_TRUNC('month', fecha)
ORDER BY mes ASC;

-- 3. Ver si hay clases después de mayo 2025
SELECT
    COUNT(*) AS clases_despues_mayo_2025
FROM public.clases
WHERE fecha >= '2025-06-01'::timestamp with time zone;

-- 4. Ver todas las clases de junio a diciembre 2025
SELECT
    DATE_TRUNC('month', fecha)::date AS mes,
    COUNT(*) AS total_clases
FROM public.clases
WHERE fecha >= '2025-06-01'::timestamp with time zone
  AND fecha < '2026-01-01'::timestamp with time zone
GROUP BY DATE_TRUNC('month', fecha)
ORDER BY mes ASC;

-- 5. Ver reservas por mes
SELECT
    DATE_TRUNC('month', c.fecha)::date AS mes,
    COUNT(c.id) AS total_clases,
    COUNT(rc.id) AS total_reservas
FROM public.clases c
LEFT JOIN public.reservas_clase rc ON c.id = rc.clase_id
WHERE c.fecha >= '2024-01-01'::timestamp with time zone
  AND c.fecha < '2026-01-01'::timestamp with time zone
GROUP BY DATE_TRUNC('month', c.fecha)
ORDER BY mes ASC;
