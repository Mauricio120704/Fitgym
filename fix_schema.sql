-- ============================================
-- SCRIPT PARA CORREGIR ERRORES EN EL SCHEMA
-- ============================================

-- 1. CORREGIR TABLA ROLES (eliminar columna id_rol duplicada)
ALTER TABLE public.roles DROP COLUMN IF EXISTS id_rol;

-- 2. CORREGIR TABLA INCIDENCIAS (eliminar foreign keys conflictivas)
ALTER TABLE public.incidencias 
DROP CONSTRAINT IF EXISTS fk8vj8oh395ctey555mqkauqvfj;

ALTER TABLE public.incidencias 
DROP CONSTRAINT IF EXISTS fkiko55nv19535qieb92b2olmpy;

-- Mantener solo las referencias correctas a usuarios
ALTER TABLE public.incidencias 
ADD CONSTRAINT fk_incidencias_reportador FOREIGN KEY (reportado_por) REFERENCES public.usuarios(id) ON DELETE SET NULL;

ALTER TABLE public.incidencias 
ADD CONSTRAINT fk_incidencias_asignado FOREIGN KEY (asignado_a) REFERENCES public.usuarios(id) ON DELETE SET NULL;

-- 3. CORREGIR TABLA ENTRENAMIENTOS (eliminar foreign key duplicada)
ALTER TABLE public.entrenamientos 
DROP CONSTRAINT IF EXISTS fkgwj391qd4s3m5d2xrul4ykmcg;

-- Mantener solo la referencia correcta
ALTER TABLE public.entrenamientos 
ADD CONSTRAINT fk_entrenamientos_creador FOREIGN KEY (creado_por) REFERENCES public.usuarios(id) ON DELETE SET NULL;

-- 4. CORREGIR TABLA PAGOS (eliminar foreign key duplicada)
ALTER TABLE public.pagos 
DROP CONSTRAINT IF EXISTS fkaop6c8ck872586bifjwm4b5m4;

-- Mantener solo la referencia correcta
ALTER TABLE public.pagos 
ADD CONSTRAINT fk_pagos_registrador FOREIGN KEY (registrado_por) REFERENCES public.usuarios(id) ON DELETE SET NULL;

-- 5. CORREGIR TABLA RECLAMOS (eliminar foreign key duplicada)
ALTER TABLE public.reclamos 
DROP CONSTRAINT IF EXISTS fk2lcoc11rndlhxkoqv27jwt739;

-- Mantener solo la referencia correcta
ALTER TABLE public.reclamos 
ADD CONSTRAINT reclamos_atendido_por_fkey FOREIGN KEY (atendido_por) REFERENCES public.usuarios(id) ON DELETE SET NULL;

-- ============================================
-- VERIFICACIÓN: Listar todas las constraints
-- ============================================
-- SELECT constraint_name, table_name, column_name 
-- FROM information_schema.key_column_usage 
-- WHERE table_schema = 'public' 
-- ORDER BY table_name;
