-- Vistas para Power BI: Asistencia y Ocupación de Clases
-- Ejecuta este script en PostgreSQL antes de conectar Power BI

-- Base de asistencia con enriquecimiento temporal
CREATE OR REPLACE VIEW public.vw_asistencia_base AS
SELECT 
  a.id                             AS asistencia_id,
  a.persona_id,
  p.genero,
  COALESCE(p.membresia_activa, TRUE) AS membresia_activa,
  a.fecha_hora_ingreso            AS ingreso,
  a.fecha_hora_salida             AS salida,
  (a.fecha_hora_salida - a.fecha_hora_ingreso)                        AS duracion_raw,
  (EXTRACT(EPOCH FROM (a.fecha_hora_salida - a.fecha_hora_ingreso))/60.0) AS duracion_min,
  date_trunc('day', a.fecha_hora_ingreso)::date AS fecha,
  date_trunc('week', a.fecha_hora_ingreso)::date AS semana_inicio,
  to_char(a.fecha_hora_ingreso, 'IYYY-IW')      AS iso_anio_semana,
  date_trunc('month', a.fecha_hora_ingreso)::date AS mes,
  EXTRACT(isodow FROM a.fecha_hora_ingreso)::int AS dia_semana_iso,
  to_char(a.fecha_hora_ingreso, 'Dy')           AS dia_semana_txt,
  EXTRACT(hour FROM a.fecha_hora_ingreso)::int  AS hora
FROM public.asistencias a
LEFT JOIN public.personas p ON p.id = a.persona_id;

-- Agregado diario
CREATE OR REPLACE VIEW public.vw_asistencia_diaria AS
SELECT 
  ab.fecha,
  COUNT(*) AS total_asistencias,
  COUNT(DISTINCT ab.persona_id) AS deportistas_unicos,
  AVG(NULLIF(ab.duracion_min, NULL)) AS promedio_estadia_min,
  SUM(CASE WHEN ab.genero = 'M' THEN 1 ELSE 0 END) AS total_hombres,
  SUM(CASE WHEN ab.genero = 'F' THEN 1 ELSE 0 END) AS total_mujeres
FROM public.vw_asistencia_base ab
GROUP BY ab.fecha
ORDER BY ab.fecha;

-- Agregado semanal (ISO)
CREATE OR REPLACE VIEW public.vw_asistencia_semanal AS
SELECT 
  ab.iso_anio_semana,
  ab.semana_inicio,
  COUNT(*) AS total_asistencias,
  COUNT(DISTINCT ab.persona_id) AS deportistas_unicos,
  AVG(NULLIF(ab.duracion_min, NULL)) AS promedio_estadia_min
FROM public.vw_asistencia_base ab
GROUP BY ab.iso_anio_semana, ab.semana_inicio
ORDER BY ab.semana_inicio;

-- Agregado mensual
CREATE OR REPLACE VIEW public.vw_asistencia_mensual AS
SELECT 
  ab.mes,
  COUNT(*) AS total_asistencias,
  COUNT(DISTINCT ab.persona_id) AS deportistas_unicos,
  AVG(NULLIF(ab.duracion_min, NULL)) AS promedio_estadia_min
FROM public.vw_asistencia_base ab
GROUP BY ab.mes
ORDER BY ab.mes;

-- Afluencia por hora (heatmap de horas y fechas)
CREATE OR REPLACE VIEW public.vw_afluencia_horaria AS
SELECT 
  ab.fecha,
  ab.hora,
  COUNT(*) AS total_asistencias,
  COUNT(DISTINCT ab.persona_id) AS deportistas_unicos
FROM public.vw_asistencia_base ab
GROUP BY ab.fecha, ab.hora
ORDER BY ab.fecha, ab.hora;

-- Ocupación de clases a nivel de clase
CREATE OR REPLACE VIEW public.vw_ocupacion_clases AS
SELECT 
  c.id AS clase_id,
  c.nombre,
  c.descripcion,
  c.capacidad,
  c.fecha,
  c.duracion_minutos,
  c.estado,
  tc.nombre AS tipo_clase,
  COALESCE(rc.total_reservas, 0) AS reservas_totales,
  COALESCE(rc.asistencias, 0)    AS asistencias,
  CASE WHEN c.capacidad > 0 THEN ROUND(COALESCE(rc.total_reservas,0)::numeric / c.capacidad, 3) ELSE NULL END AS tasa_reserva,
  CASE WHEN c.capacidad > 0 THEN ROUND(COALESCE(rc.asistencias,0)::numeric     / c.capacidad, 3) ELSE NULL END AS tasa_ocupacion
FROM public.clases c
LEFT JOIN public.tipos_clase tc ON tc.id = c.tipo_clase_id
LEFT JOIN (
  SELECT 
    r.clase_id,
    COUNT(*) AS total_reservas,
    SUM(CASE WHEN r.estado ILIKE 'Asistió' THEN 1 ELSE 0 END) AS asistencias
  FROM public.reservas_clase r
  GROUP BY r.clase_id
) rc ON rc.clase_id = c.id;

-- Ocupación agregada mensual de clases
CREATE OR REPLACE VIEW public.vw_ocupacion_mensual AS
SELECT 
  date_trunc('month', c.fecha)::date AS mes,
  COUNT(*) FILTER (WHERE c.fecha < now()) AS clases_programadas,
  SUM(oc.reservas_totales) AS reservas_totales,
  SUM(oc.asistencias)      AS asistencias_totales,
  AVG(oc.tasa_reserva)     AS reserva_promedio,
  AVG(oc.tasa_ocupacion)   AS ocupacion_promedio
FROM public.vw_ocupacion_clases oc
JOIN public.clases c ON c.id = oc.clase_id
GROUP BY 1
ORDER BY 1;
