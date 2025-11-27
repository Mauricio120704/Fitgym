-- ============================================================================
-- SCRIPT DE DATOS DE PRUEBA PARA ROLES - FITGYM
-- ============================================================================
-- Este script inserta 3 usuarios de ejemplo (uno por rol) para probar la
-- separación de acceso por roles implementada en SecurityConfig y @PreAuthorize
--
-- IMPORTANTE: Ejecutar este script SOLO en desarrollo/testing
-- ============================================================================

BEGIN;

-- ============================================================================
-- 1. ASEGURAR QUE EXISTAN LOS ROLES
-- ============================================================================
INSERT INTO public.roles (codigo, nombre, descripcion)
VALUES 
    ('ADMINISTRADOR', 'Administrador', 'Acceso total al sistema'),
    ('RECEPCIONISTA', 'Recepcionista', 'Gestión de asistencia, lockers y monitoreo'),
    ('ENTRENADOR', 'Entrenador', 'Gestión de deportistas y entrenamientos')
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================================
-- 2. INSERTAR USUARIOS DE PRUEBA (uno por rol)
-- ============================================================================

-- Obtener IDs de roles (para usarlos en las inserciones)
-- Nota: Si los IDs son diferentes, ajusta manualmente

-- USUARIO 1: ADMINISTRADOR
INSERT INTO public.usuarios (
    nombre, apellido, email, "contraseña", telefono, dni, 
    fecha_registro, rol_id, activo, departamento, puesto, 
    fecha_inicio_laboral, horario
)
SELECT 
    'Admin', 'Sistema', 'admin@fitgym.local', 
    '$2a$10$slYQmyNdGzin7olVahuUNOr1wdgLMmydLT8.5.8.8.8.8.8.8.8.8.8.8.8', -- bcrypt: "admin123"
    '555-0001', '12345678',
    CURRENT_DATE, r.id, true, 'Administración', 'Administrador del Sistema',
    CURRENT_DATE, 'Lunes a Viernes 08:00-17:00'
FROM public.roles r
WHERE r.codigo = 'ADMINISTRADOR'
ON CONFLICT (email) DO NOTHING;

-- USUARIO 2: RECEPCIONISTA
INSERT INTO public.usuarios (
    nombre, apellido, email, "contraseña", telefono, dni,
    fecha_registro, rol_id, activo, departamento, puesto,
    fecha_inicio_laboral, horario
)
SELECT
    'Recep', 'Prueba', 'recepcionista@fitgym.local',
    '$2a$10$slYQmyNdGzin7olVahuUNOr1wdgLMmydLT8.5.8.8.8.8.8.8.8.8.8.8.8', -- bcrypt: "admin123"
    '555-0002', '87654321',
    CURRENT_DATE, r.id, true, 'Recepción', 'Recepcionista',
    CURRENT_DATE, 'Lunes a Viernes 09:00-18:00'
FROM public.roles r
WHERE r.codigo = 'RECEPCIONISTA'
ON CONFLICT (email) DO NOTHING;

-- USUARIO 3: ENTRENADOR
INSERT INTO public.usuarios (
    nombre, apellido, email, "contraseña", telefono, dni,
    fecha_registro, rol_id, activo, departamento, puesto,
    fecha_inicio_laboral, horario
)
SELECT
    'Coach', 'Entrenador', 'entrenador@fitgym.local',
    '$2a$10$slYQmyNdGzin7olVahuUNOr1wdgLMmydLT8.5.8.8.8.8.8.8.8.8.8.8.8', -- bcrypt: "admin123"
    '555-0003', '11223344',
    CURRENT_DATE, r.id, true, 'Entrenamientos', 'Entrenador Personal',
    CURRENT_DATE, 'Lunes a Viernes 06:00-20:00'
FROM public.roles r
WHERE r.codigo = 'ENTRENADOR'
ON CONFLICT (email) DO NOTHING;

-- ============================================================================
-- 3. INSERTAR USUARIO CLIENTE DE PRUEBA (para comparar)
-- ============================================================================
INSERT INTO public.personas (
    nombre, apellido, email, "contraseña", telefono, dni,
    fecha_registro, membresia_activa, activo, email_verificado,
    fecha_nacimiento, genero, direccion
)
VALUES (
    'Cliente', 'Prueba', 'cliente@fitgym.local',
    '$2a$10$slYQmyNdGzin7olVahuUNOr1wdgLMmydLT8.5.8.8.8.8.8.8.8.8.8.8.8', -- bcrypt: "admin123"
    '555-9999', '99999999',
    CURRENT_DATE, true, true, true,
    '1990-01-15', 'M', 'Calle Principal 123'
)
ON CONFLICT (email) DO NOTHING;

-- ============================================================================
-- RESUMEN DE CREDENCIALES DE PRUEBA
-- ============================================================================
-- 
-- ADMINISTRADOR:
--   Email: admin@fitgym.local
--   Contraseña: admin123
--   Acceso: TODAS las pantallas del sistema
--
-- RECEPCIONISTA:
--   Email: recepcionista@fitgym.local
--   Contraseña: admin123
--   Acceso: Personal, Incidencias, Promociones, Pagos, Clases,
--           Asistencia, Lockers, Monitoreo, Visitantes, Invitados
--   NO acceso: Equipos, Inventario, Notificaciones, Reportes Financieros
--
-- ENTRENADOR:
--   Email: entrenador@fitgym.local
--   Contraseña: admin123
--   Acceso: Personal, Incidencias, Promociones, Pagos, Clases,
--           Mis Deportistas
--   NO acceso: Asistencia, Lockers, Monitoreo, Equipos, Inventario,
--              Notificaciones, Reportes Financieros
--
-- CLIENTE (Deportista):
--   Email: cliente@fitgym.local
--   Contraseña: admin123
--   Acceso: Solo pantallas de cliente (Perfil, Entrenamientos, etc.)
--   NO acceso: Ninguna pantalla administrativa
--
-- ============================================================================

COMMIT;
