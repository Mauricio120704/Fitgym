-- Script de Verificación del Módulo de Invitados
-- Ejecutar este script después de instalar la funcionalidad para verificar que todo esté correcto

-- ============================================
-- 1. VERIFICAR ESTRUCTURA DE LA TABLA
-- ============================================
SELECT 
    'Verificando estructura de tabla visitantes...' AS paso;

SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'visitantes' 
ORDER BY ordinal_position;

-- ============================================
-- 2. VERIFICAR COLUMNA INVITADO_POR_PERSONA_ID
-- ============================================
SELECT 
    'Verificando columna invitado_por_persona_id...' AS paso;

SELECT 
    column_name, 
    data_type, 
    is_nullable 
FROM information_schema.columns 
WHERE table_name = 'visitantes' 
  AND column_name = 'invitado_por_persona_id';

-- Resultado esperado: 1 fila con data_type = 'bigint' y is_nullable = 'YES'

-- ============================================
-- 3. VERIFICAR CLAVE FORÁNEA
-- ============================================
SELECT 
    'Verificando clave foránea...' AS paso;

SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name = 'visitantes'
  AND kcu.column_name = 'invitado_por_persona_id';

-- Resultado esperado: 1 fila con constraint_name = 'fk_visitante_invitado_por_persona'

-- ============================================
-- 4. VERIFICAR ÍNDICE
-- ============================================
SELECT 
    'Verificando índice...' AS paso;

SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'visitantes'
  AND indexname = 'idx_visitantes_invitado_por_persona';

-- Resultado esperado: 1 fila con el índice creado

-- ============================================
-- 5. VERIFICAR DATOS DE PERSONAS (MIEMBROS)
-- ============================================
SELECT 
    'Verificando personas/miembros disponibles...' AS paso;

SELECT 
    COUNT(*) AS total_personas,
    COUNT(CASE WHEN membresia_activa = true THEN 1 END) AS con_membresia_activa,
    COUNT(CASE WHEN membresia_activa = false THEN 1 END) AS sin_membresia_activa
FROM personas;

-- ============================================
-- 6. VERIFICAR VISITANTES EXISTENTES
-- ============================================
SELECT 
    'Verificando visitantes existentes...' AS paso;

SELECT 
    COUNT(*) AS total_visitantes,
    COUNT(CASE WHEN invitado_por_persona_id IS NOT NULL THEN 1 END) AS invitados,
    COUNT(CASE WHEN invitado_por_persona_id IS NULL THEN 1 END) AS visitantes_regulares,
    COUNT(CASE WHEN estado = 'ACTIVO' THEN 1 END) AS activos,
    COUNT(CASE WHEN estado = 'FINALIZADO' THEN 1 END) AS finalizados
FROM visitantes;

-- ============================================
-- 7. LISTAR INVITADOS CON SUS INVITANTES
-- ============================================
SELECT 
    'Listando invitados con sus invitantes...' AS paso;

SELECT 
    v.id AS visitante_id,
    v.nombre_completo AS invitado,
    v.documento_identidad AS doc_invitado,
    v.codigo_pase,
    v.estado,
    v.fecha_hora_ingreso,
    p.nombre || ' ' || p.apellido AS invitado_por,
    p.dni AS dni_invitante,
    p.email AS email_invitante
FROM visitantes v
LEFT JOIN personas p ON v.invitado_por_persona_id = p.id
WHERE v.invitado_por_persona_id IS NOT NULL
ORDER BY v.fecha_hora_ingreso DESC
LIMIT 10;

-- ============================================
-- 8. ESTADÍSTICAS POR MIEMBRO
-- ============================================
SELECT 
    'Estadísticas de invitados por miembro...' AS paso;

SELECT 
    p.nombre || ' ' || p.apellido AS miembro,
    p.dni,
    p.email,
    COUNT(v.id) AS total_invitados,
    COUNT(CASE WHEN v.estado = 'ACTIVO' THEN 1 END) AS invitados_activos,
    COUNT(CASE WHEN v.estado = 'FINALIZADO' THEN 1 END) AS invitados_finalizados,
    MAX(v.fecha_hora_ingreso) AS ultimo_invitado
FROM personas p
LEFT JOIN visitantes v ON p.id = v.invitado_por_persona_id
GROUP BY p.id, p.nombre, p.apellido, p.dni, p.email
HAVING COUNT(v.id) > 0
ORDER BY total_invitados DESC;

-- ============================================
-- 9. VERIFICAR CÓDIGOS DE PASE
-- ============================================
SELECT 
    'Verificando códigos de pase de invitados...' AS paso;

SELECT 
    codigo_pase,
    nombre_completo,
    estado,
    CASE 
        WHEN codigo_pase LIKE 'INV-%' THEN 'Formato correcto'
        ELSE 'Formato incorrecto'
    END AS validacion_codigo
FROM visitantes
WHERE invitado_por_persona_id IS NOT NULL
ORDER BY fecha_hora_ingreso DESC
LIMIT 10;

-- ============================================
-- 10. RESUMEN FINAL
-- ============================================
SELECT 
    'RESUMEN FINAL DE VERIFICACIÓN' AS paso;

SELECT 
    'Instalación completada correctamente' AS estado,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'visitantes' AND column_name = 'invitado_por_persona_id') AS columna_creada,
    (SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_name = 'fk_visitante_invitado_por_persona') AS fk_creada,
    (SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'idx_visitantes_invitado_por_persona') AS indice_creado,
    (SELECT COUNT(*) FROM personas) AS total_miembros,
    (SELECT COUNT(*) FROM visitantes WHERE invitado_por_persona_id IS NOT NULL) AS total_invitados;

-- Resultado esperado:
-- columna_creada = 1
-- fk_creada = 1
-- indice_creado = 1
-- total_miembros > 0
-- total_invitados >= 0

-- ============================================
-- FIN DE LA VERIFICACIÓN
-- ============================================
SELECT 
    '✅ Verificación completada. Revisar los resultados anteriores.' AS mensaje;
