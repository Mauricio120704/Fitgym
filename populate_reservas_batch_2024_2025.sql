-- Script idempotente para:
 -- 1) Asegurar tipos de clase base
 -- 2) Asegurar clases en 2024 y en TODOS los meses de 2025
 -- 3) Asegurar un pool mínimo de personas activas con membresía
 -- 4) Poblar reservas coherentes 2024-2025

 BEGIN;

 -- 1) Tipos de clase base
 INSERT INTO public.tipos_clase (nombre, descripcion, duracion_predeterminada, color_codigo, activo)
 SELECT x.nombre, x.descripcion, x.duracion_predeterminada, x.color_codigo, TRUE
 FROM (VALUES
   ('Funcional', 'Entrenamiento funcional', 60, '#16a34a'),
   ('Spinning', 'Ciclismo indoor', 45, '#06b6d4'),
   ('Yoga', 'Sesión de yoga', 60, '#a855f7'),
   ('HIIT', 'Alta intensidad por intervalos', 40, '#ef4444')
 ) AS x(nombre, descripcion, duracion_predeterminada, color_codigo)
 WHERE NOT EXISTS (
   SELECT 1 FROM public.tipos_clase t WHERE t.nombre = x.nombre
 );

 -- 2) CLASES 2024: si no hay ninguna en todo 2024, crear una base (12 clases, día 10 de cada mes 18:00)
 WITH tiene_2024 AS (
   SELECT COUNT(*) AS cnt
   FROM public.clases
   WHERE fecha >= '2024-01-01'::timestamptz AND fecha < '2025-01-01'::timestamptz
 )
 INSERT INTO public.clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado, tipo_clase_id, es_pago, precio, para_todos, cupos_basico)
 SELECT
   'Clase Base ' || to_char(m, 'Mon YYYY'),
   'Clase generada automáticamente (base 2024)',
   NULL,
   24,
   ((m + interval '10 days')::date + time '18:00')::timestamptz,
   COALESCE((SELECT duracion_predeterminada FROM public.tipos_clase WHERE nombre='Funcional' LIMIT 1), 60),
   'Programada',
   (SELECT id FROM public.tipos_clase WHERE nombre='Funcional' LIMIT 1),
   FALSE,
   NULL,
   TRUE,
   0
 FROM generate_series('2024-01-01'::date, '2024-12-01'::date, interval '1 month') AS m
 WHERE (SELECT cnt FROM tiene_2024) = 0;

 -- 2b) CLASES 2025: asegurar que CADA MES tenga clases. Para meses sin clases, crear 6 clases: días 3,10,17 a las 07:00 y 18:00
 WITH meses AS (
   SELECT gs::date AS inicio_mes, (gs + interval '1 month')::date AS fin_mes
   FROM generate_series('2025-01-01'::date, '2025-12-01'::date, interval '1 month') gs
 ), meses_faltantes AS (
   SELECT m.*
   FROM meses m
   WHERE NOT EXISTS (
     SELECT 1 FROM public.clases c
     WHERE c.fecha >= m.inicio_mes::timestamptz AND c.fecha < m.fin_mes::timestamptz
   )
 ), plantillas AS (
   SELECT unnest(ARRAY[3,10,17]) AS dia, unnest(ARRAY['07:00'::time,'18:00'::time]) AS hora
 )
 INSERT INTO public.clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado, tipo_clase_id, es_pago, precio, para_todos, cupos_basico)
 WITH datos AS (
   SELECT 
     m.inicio_mes,
     p.dia AS dia_objetivo,
     p.hora,
     -- último día del mes
     EXTRACT(DAY FROM (date_trunc('month', m.inicio_mes) + interval '1 month' - interval '1 day'))::int AS dias_en_mes,
     t.id AS tipo_id,
     t.nombre AS tipo_nombre,
     t.duracion_predeterminada AS duracion,
     GREATEST(1, LEAST(p.dia, EXTRACT(DAY FROM (date_trunc('month', m.inicio_mes) + interval '1 month' - interval '1 day'))::int))::int AS clamped_day
   FROM meses_faltantes m
   CROSS JOIN plantillas p
   CROSS JOIN LATERAL (
     SELECT id, nombre, COALESCE(duracion_predeterminada,60) AS duracion_predeterminada
     FROM public.tipos_clase
     ORDER BY nombre
     LIMIT 1
   ) t
 )
 SELECT
   'Clase ' || to_char(d.inicio_mes, 'Mon YYYY') || ' - ' || lpad(d.clamped_day::text, 2, '0') || ' ' || d.tipo_nombre,
   'Clase generada automáticamente (completar mes 2025)',
   NULL,
   24,
   make_timestamptz(
      EXTRACT(YEAR FROM d.inicio_mes)::int,
      EXTRACT(MONTH FROM d.inicio_mes)::int,
      d.clamped_day,
      COALESCE(split_part(d.hora::text, ':', 1)::int, 9),
      COALESCE(split_part(d.hora::text, ':', 2)::int, 0),
      0
  ),
   d.duracion,
   'Programada',
   d.tipo_id,
   FALSE,
   NULL,
   TRUE,
   0
 FROM datos d;


 -- 3) PERSONAS: asegurar al menos 300 activas con membresía
 DO $$
 DECLARE
   desired INT := 300;
   current_cnt INT;
   to_create INT;
 BEGIN
   SELECT COUNT(*) INTO current_cnt
   FROM public.personas p
   WHERE COALESCE(p.membresia_activa, TRUE) = TRUE AND COALESCE(p.activo, TRUE) = TRUE;

   to_create := GREATEST(0, desired - current_cnt);

   IF to_create > 0 THEN
     INSERT INTO public.personas (
       nombre, apellido, email, telefono, dni, fecha_registro, membresia_activa, "contraseña", fecha_nacimiento,
       genero, activo, email_verificado, perfil_visible
     )
     SELECT
       'Deportista',
       'Auto',
       'deportista_' || substr(md5(random()::text), 1, 10) || '@fitgym.local',
       '+51' || (100000000 + floor(random()*899999999))::bigint::text,
       'D' || (10000000 + floor(random()*89999999))::bigint::text,
       CURRENT_DATE,
       TRUE,
       -- contraseña placeholder (no usada en tests de login)
       md5(random()::text),
       date '1990-01-01' + (floor(random()*10000))::int,
       (ARRAY['M','F'])[1 + floor(random()*2)],
       TRUE,
       TRUE,
       TRUE
     FROM generate_series(1, to_create);
   END IF;
 END $$;

-- 4b) Rebalanceo: asegurar al menos 6 y como máximo 35 clases distintas por día (reservado_en::date)
-- Nota: solo añade reservas faltantes, no borra. Respeta capacidad de la clase y evita duplicados clase-persona
WITH dias AS (
  SELECT 
    r.reservado_en::date AS dia,
    COUNT(DISTINCT r.clase_id) AS cnt,
    ARRAY_AGG(DISTINCT r.clase_id) AS clases_existentes,
    -- objetivo aleatorio entre 6 y 35, pero nunca menor que cnt actual
    LEAST(35, GREATEST(COUNT(DISTINCT r.clase_id), 6) + FLOOR(random() * (35 - GREATEST(COUNT(DISTINCT r.clase_id),6) + 1))::int) AS target_cnt
  FROM public.reservas_clase r
  WHERE r.reservado_en >= '2024-01-01'::timestamptz AND r.reservado_en < '2026-01-01'::timestamptz
  GROUP BY r.reservado_en::date
  HAVING COUNT(DISTINCT r.clase_id) < 35
), candidatos AS (
  SELECT d.dia, c.id AS clase_id
  FROM dias d
  CROSS JOIN LATERAL (
    SELECT c.id
    FROM public.clases c
    WHERE c.fecha >= '2024-01-01'::timestamptz AND c.fecha < '2026-01-01'::timestamptz
      AND NOT (c.id = ANY(d.clases_existentes))
    ORDER BY random()
    LIMIT GREATEST(d.target_cnt - d.cnt, 0)
  ) c
), cupo AS (
  SELECT r.clase_id, COUNT(*) AS reservas
  FROM public.reservas_clase r
  GROUP BY r.clase_id
), seleccion AS (
  SELECT cand.dia, cand.clase_id
  FROM candidatos cand
  JOIN public.clases cl ON cl.id = cand.clase_id
  LEFT JOIN cupo cu ON cu.clase_id = cand.clase_id
  WHERE COALESCE(cu.reservas,0) < cl.capacidad
)
INSERT INTO public.reservas_clase (clase_id, deportista_id, estado, reservado_en)
SELECT
  s.clase_id,
  psel.deportista_id,
  CASE WHEN cl.fecha < NOW() THEN 'Asistió' ELSE 'Reservado' END,
  (s.dia::timestamp + (interval '1 hour' * (8 + ((ROW_NUMBER() OVER (PARTITION BY s.dia ORDER BY s.clase_id)) % 10))))
FROM seleccion s
JOIN public.clases cl ON cl.id = s.clase_id
JOIN LATERAL (
  SELECT p.id AS deportista_id
  FROM public.personas p
  WHERE COALESCE(p.membresia_activa, TRUE) = TRUE
    AND COALESCE(p.activo, TRUE) = TRUE
    AND NOT EXISTS (
      SELECT 1 FROM public.reservas_clase r
      WHERE r.clase_id = s.clase_id AND r.deportista_id = p.id
    )
  ORDER BY random()
  LIMIT 1
) psel ON true
ON CONFLICT (clase_id, deportista_id) DO NOTHING;

 COMMIT;

 -- 4) Poblar reservas por lotes para TODAS las clases 2024-2025 (idempotente con fallback)
 DO $$
 DECLARE
   cls RECORD;
   target_count INT;
 BEGIN
   FOR cls IN
     SELECT id, fecha, capacidad
     FROM public.clases
     WHERE fecha >= '2024-01-01'::timestamptz
       AND fecha <  '2026-01-01'::timestamptz
     ORDER BY fecha
   LOOP
     -- 40% a 90% de la capacidad (al menos 1)
     target_count := GREATEST(1, FLOOR(random() * (cls.capacidad * 0.5) + (cls.capacidad * 0.4))::INT);

     -- 1) Intentar con personas activas con membresía
     WITH candidatos AS (
       SELECT p.id AS deportista_id
       FROM public.personas p
       WHERE COALESCE(p.membresia_activa, TRUE) = TRUE
         AND COALESCE(p.activo, TRUE) = TRUE
         AND NOT EXISTS (
           SELECT 1 FROM public.reservas_clase r
           WHERE r.clase_id = cls.id AND r.deportista_id = p.id
         )
       ORDER BY random()
       LIMIT target_count
     )
     INSERT INTO public.reservas_clase (clase_id, deportista_id, estado, reservado_en)
     SELECT
       cls.id,
       c.deportista_id,
       CASE WHEN cls.fecha < NOW() THEN 'Asistió' ELSE 'Reservado' END,
       LEAST(cls.fecha - interval '1 hour', cls.fecha - ( (1 + FLOOR(random() * 7))::INT * INTERVAL '1 day'))
     FROM candidatos c
     ON CONFLICT (clase_id, deportista_id) DO NOTHING;

     -- 2) Fallback: completar con cualquier persona si faltan cupos
     WITH faltan AS (
       SELECT GREATEST(target_count - COUNT(*), 0) AS restantes
       FROM public.reservas_clase r
       WHERE r.clase_id = cls.id
     ), candidatos_any AS (
       SELECT p.id AS deportista_id
       FROM public.personas p
       WHERE (SELECT restantes FROM faltan) > 0
         AND NOT EXISTS (
           SELECT 1 FROM public.reservas_clase r
           WHERE r.clase_id = cls.id AND r.deportista_id = p.id
         )
       ORDER BY random()
       LIMIT (SELECT restantes FROM faltan)
     )
     INSERT INTO public.reservas_clase (clase_id, deportista_id, estado, reservado_en)
     SELECT
       cls.id,
       c.deportista_id,
       CASE WHEN cls.fecha < NOW() THEN 'Asistió' ELSE 'Reservado' END,
       LEAST(cls.fecha - interval '1 hour', cls.fecha - ( (1 + FLOOR(random() * 7))::INT * INTERVAL '1 day'))
     FROM candidatos_any c
     ON CONFLICT (clase_id, deportista_id) DO NOTHING;
   END LOOP;
 END $$;

 -- Reporte mensual
 SELECT
   DATE_TRUNC('month', c.fecha)::date AS mes,
   COUNT(c.id) AS total_clases,
   COUNT(r.id) AS total_reservas
 FROM public.clases c
 LEFT JOIN public.reservas_clase r ON r.clase_id = c.id
 WHERE c.fecha >= '2024-01-01'::timestamptz
   AND c.fecha <  '2026-01-01'::timestamptz
 GROUP BY 1
 ORDER BY 1;
