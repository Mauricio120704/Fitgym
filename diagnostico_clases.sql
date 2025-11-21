-- Script de diagnóstico para ver qué clases existen

-- Ver rango de fechas de clases
SELECT
    MIN(fecha) AS fecha_minima,
    MAX(fecha) AS fecha_maxima,
    COUNT(*) AS total_clases
FROM public.clases;

-- Ver distribución de clases por mes
SELECT
    DATE_TRUNC('month', fecha)::date AS mes,
    COUNT(*) AS total_clases,
    MIN(fecha) AS primera_clase,
    MAX(fecha) AS ultima_clase
FROM public.clases
GROUP BY DATE_TRUNC('month', fecha)
ORDER BY mes ASC;

-- Ver si hay clases en 2024 y 2025
SELECT
    EXTRACT(YEAR FROM fecha)::INT AS año,
    COUNT(*) AS total_clases
FROM public.clases
WHERE EXTRACT(YEAR FROM fecha) IN (2024, 2025)
GROUP BY EXTRACT(YEAR FROM fecha)
ORDER BY año;

-- Ver clases de diciembre 2023 a mayo 2025
SELECT
    DATE_TRUNC('month', fecha)::date AS mes,
    COUNT(*) AS total_clases
FROM public.clases
WHERE fecha >= '2023-12-01'::timestamp with time zone
  AND fecha < '2025-06-01'::timestamp with time zone
GROUP BY DATE_TRUNC('month', fecha)
ORDER BY mes ASC;
