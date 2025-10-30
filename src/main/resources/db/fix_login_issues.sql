-- Script para diagnosticar y corregir problemas de login
-- Ejecutar en PostgreSQL

-- ========================================
-- 1. VERIFICAR ROLES
-- ========================================
SELECT 'Verificando roles...' as status;
SELECT * FROM roles ORDER BY id;

-- Si el rol DEPORTISTA no existe, crearlo
INSERT INTO roles (id, codigo, nombre) VALUES 
(4, 'DEPORTISTA', 'Deportista')
ON CONFLICT (id) DO UPDATE SET codigo = 'DEPORTISTA', nombre = 'Deportista';

-- ========================================
-- 2. VERIFICAR USUARIOS DEPORTISTAS
-- ========================================
SELECT 'Verificando usuarios deportistas...' as status;
SELECT 
    id,
    nombre,
    apellido,
    email,
    tipo,
    rol_id,
    membresia_activa,
    LEFT(contraseña, 10) as password_preview,
    fecha_registro
FROM personas 
WHERE tipo = 'DEPORTISTA'
ORDER BY fecha_registro DESC
LIMIT 10;

-- ========================================
-- 3. CORREGIR USUARIOS SIN ROL
-- ========================================
SELECT 'Corrigiendo usuarios sin rol...' as status;
UPDATE personas 
SET rol_id = (SELECT id FROM roles WHERE codigo = 'DEPORTISTA')
WHERE tipo = 'DEPORTISTA' AND rol_id IS NULL;

SELECT 'Usuarios actualizados: ' || ROW_COUNT() as result;

-- ========================================
-- 4. CORREGIR MEMBRESÍA INACTIVA
-- ========================================
SELECT 'Corrigiendo membresías inactivas...' as status;
UPDATE personas 
SET membresia_activa = true
WHERE tipo = 'DEPORTISTA' AND (membresia_activa IS NULL OR membresia_activa = false);

SELECT 'Membresías activadas: ' || ROW_COUNT() as result;

-- ========================================
-- 5. VERIFICAR CONTRASEÑAS ENCRIPTADAS
-- ========================================
SELECT 'Verificando contraseñas...' as status;
SELECT 
    email,
    CASE 
        WHEN contraseña LIKE '$2a$%' THEN 'BCrypt ✓'
        WHEN contraseña LIKE '$2b$%' THEN 'BCrypt ✓'
        WHEN contraseña LIKE '$2y$%' THEN 'BCrypt ✓'
        ELSE 'NO ENCRIPTADA ✗'
    END as password_status,
    LENGTH(contraseña) as password_length
FROM personas 
WHERE tipo = 'DEPORTISTA'
ORDER BY fecha_registro DESC
LIMIT 10;

-- ========================================
-- 6. RESUMEN FINAL
-- ========================================
SELECT 'RESUMEN FINAL' as status;

SELECT 
    'Total Deportistas' as metric,
    COUNT(*) as value
FROM personas 
WHERE tipo = 'DEPORTISTA'
UNION ALL
SELECT 
    'Con Rol Asignado' as metric,
    COUNT(*) as value
FROM personas 
WHERE tipo = 'DEPORTISTA' AND rol_id IS NOT NULL
UNION ALL
SELECT 
    'Con Membresía Activa' as metric,
    COUNT(*) as value
FROM personas 
WHERE tipo = 'DEPORTISTA' AND membresia_activa = true
UNION ALL
SELECT 
    'Con Password BCrypt' as metric,
    COUNT(*) as value
FROM personas 
WHERE tipo = 'DEPORTISTA' AND contraseña LIKE '$2%';

-- ========================================
-- 7. USUARIOS PROBLEMÁTICOS
-- ========================================
SELECT 'USUARIOS CON PROBLEMAS' as status;

SELECT 
    email,
    CASE WHEN rol_id IS NULL THEN 'Sin rol' ELSE 'OK' END as rol_status,
    CASE WHEN membresia_activa IS NULL OR membresia_activa = false THEN 'Inactiva' ELSE 'OK' END as membresia_status,
    CASE WHEN contraseña NOT LIKE '$2%' THEN 'No encriptada' ELSE 'OK' END as password_status
FROM personas 
WHERE tipo = 'DEPORTISTA'
  AND (rol_id IS NULL 
       OR membresia_activa IS NULL 
       OR membresia_activa = false 
       OR contraseña NOT LIKE '$2%')
ORDER BY fecha_registro DESC;

-- ========================================
-- 8. VERIFICAR UN USUARIO ESPECÍFICO
-- ========================================
-- Descomentar y reemplazar el email para verificar un usuario específico
/*
SELECT 
    'DETALLE DE USUARIO' as status,
    p.id,
    p.nombre || ' ' || p.apellido as nombre_completo,
    p.email,
    p.tipo,
    r.codigo as rol_codigo,
    r.nombre as rol_nombre,
    p.membresia_activa,
    LEFT(p.contraseña, 20) as password_preview,
    CASE 
        WHEN p.contraseña LIKE '$2a$%' THEN 'BCrypt válido'
        WHEN p.contraseña LIKE '$2b$%' THEN 'BCrypt válido'
        WHEN p.contraseña LIKE '$2y$%' THEN 'BCrypt válido'
        ELSE 'NO VÁLIDO'
    END as password_validation,
    p.fecha_registro
FROM personas p
LEFT JOIN roles r ON p.rol_id = r.id
WHERE p.email = 'tu@email.com';
*/

-- ========================================
-- FIN DEL SCRIPT
-- ========================================
SELECT '✓ Script completado' as status;
