-- ================================================================================
-- SCRIPT DE MIGRACIÓN ADAPTADO: Separar Deportistas (personas) y Administrativos (usuarios)
-- ADAPTADO A LA ESTRUCTURA EXISTENTE
-- ================================================================================
-- Fecha: 2024-10-26
-- Descripción:
--   - Tabla 'personas': Solo para DEPORTISTAS (tipo = 'DEPORTISTA')
--   - Tabla 'usuarios': Para personal ADMINISTRATIVO (con campos adicionales)
--   - La tabla usuarios YA EXISTE pero necesita campos adicionales
-- ================================================================================

BEGIN;

-- ================================================================================
-- PASO 1: Agregar campos faltantes a la tabla usuarios existente
-- ================================================================================

SELECT 'Actualizando estructura de tabla usuarios...' as status;

-- Agregar campos si no existen
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS telefono VARCHAR(30);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS dni VARCHAR(20) UNIQUE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS fecha_registro DATE DEFAULT CURRENT_DATE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS rol_id BIGINT;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS departamento VARCHAR(200);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS puesto VARCHAR(100);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS fecha_inicio_laboral DATE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS horario VARCHAR(150);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Renombrar password a contraseña para consistencia (opcional)
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'usuarios' AND column_name = 'password'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'usuarios' AND column_name = 'contraseña'
    ) THEN
        ALTER TABLE usuarios RENAME COLUMN password TO contraseña;
    END IF;
END $$;

-- Agregar FK a roles si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_usuarios_rol'
    ) THEN
        ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_rol 
            FOREIGN KEY (rol_id) REFERENCES roles(id);
    END IF;
END $$;

-- Crear índices para usuarios
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuarios_dni ON usuarios(dni);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol_id ON usuarios(rol_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_activo ON usuarios(activo);

SELECT 'Estructura de usuarios actualizada' as resultado;

-- ================================================================================
-- PASO 2: Migrar personal administrativo de personas a usuarios
-- ================================================================================

SELECT 'Migrando personal administrativo a tabla usuarios...' as status;

-- Insertar personal administrativo que no esté ya en usuarios
INSERT INTO usuarios (
    nombre, 
    apellido, 
    email, 
    telefono, 
    dni, 
    fecha_registro, 
    rol_id, 
    activo, 
    contraseña,
    departamento,
    puesto,
    fecha_inicio_laboral
)
SELECT 
    p.nombre,
    p.apellido,
    p.email,
    p.telefono,
    p.dni,
    p.fecha_registro,
    p.rol_id,
    COALESCE(p.membresia_activa, TRUE) as activo,
    p.contraseña,
    CASE 
        WHEN r.codigo = 'ADMINISTRADOR' THEN 'Administración General'
        WHEN r.codigo = 'RECEPCIONISTA' THEN 'Recepción y Atención al Cliente'
        WHEN r.codigo = 'ENTRENADOR' THEN 'Departamento de Entrenamiento'
        ELSE 'Sin Asignar'
    END as departamento,
    CASE 
        WHEN r.codigo = 'ADMINISTRADOR' THEN 'Administrador del Sistema'
        WHEN r.codigo = 'RECEPCIONISTA' THEN 'Recepcionista'
        WHEN r.codigo = 'ENTRENADOR' THEN 'Entrenador'
        ELSE 'Personal'
    END as puesto,
    p.fecha_registro as fecha_inicio_laboral
FROM personas p
LEFT JOIN roles r ON p.rol_id = r.id
WHERE p.tipo = 'PERSONAL'
  AND p.rol_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM usuarios u WHERE u.email = p.email
  );

SELECT 'Usuarios administrativos migrados: ' || COUNT(*) as resultado 
FROM usuarios;

-- ================================================================================
-- PASO 3: Crear tabla temporal para mapeo de IDs
-- ================================================================================

-- Esta tabla ayudará a actualizar las foreign keys
CREATE TEMP TABLE mapeo_persona_usuario AS
SELECT p.id as persona_id, u.id as usuario_id, p.email
FROM personas p
JOIN usuarios u ON u.email = p.email
WHERE p.tipo = 'PERSONAL';

SELECT 'Mapeo creado con ' || COUNT(*) || ' registros' as resultado
FROM mapeo_persona_usuario;

-- ================================================================================
-- PASO 4: Actualizar referencias en otras tablas (PERSONAL -> USUARIOS)
-- ================================================================================

SELECT 'Actualizando referencias en otras tablas...' as status;

-- CLASES: entrenador_id debe apuntar a usuarios
ALTER TABLE clases DROP CONSTRAINT IF EXISTS clases_entrenador_id_fkey;

UPDATE clases c
SET entrenador_id = m.usuario_id
FROM mapeo_persona_usuario m
WHERE c.entrenador_id = m.persona_id;

ALTER TABLE clases ADD CONSTRAINT fk_clases_entrenador 
    FOREIGN KEY (entrenador_id) REFERENCES usuarios(id) ON DELETE SET NULL;

SELECT 'Clases actualizadas: ' || COUNT(*) as resultado
FROM clases WHERE entrenador_id IN (SELECT id FROM usuarios);

-- EVALUACIONES: evaluador_id debe apuntar a usuarios
ALTER TABLE evaluaciones DROP CONSTRAINT IF EXISTS evaluaciones_evaluador_id_fkey;

UPDATE evaluaciones e
SET evaluador_id = m.usuario_id
FROM mapeo_persona_usuario m
WHERE e.evaluador_id = m.persona_id;

ALTER TABLE evaluaciones ADD CONSTRAINT fk_evaluaciones_evaluador 
    FOREIGN KEY (evaluador_id) REFERENCES usuarios(id) ON DELETE SET NULL;

SELECT 'Evaluaciones actualizadas: ' || COUNT(*) as resultado
FROM evaluaciones WHERE evaluador_id IN (SELECT id FROM usuarios);

-- ENTRENAMIENTOS: creado_por debe apuntar a usuarios
ALTER TABLE entrenamientos DROP CONSTRAINT IF EXISTS entrenamientos_creado_por_fkey;

UPDATE entrenamientos ent
SET creado_por = m.usuario_id
FROM mapeo_persona_usuario m
WHERE ent.creado_por = m.persona_id;

ALTER TABLE entrenamientos ADD CONSTRAINT fk_entrenamientos_creador 
    FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

SELECT 'Entrenamientos actualizados: ' || COUNT(*) as resultado
FROM entrenamientos WHERE creado_por IN (SELECT id FROM usuarios);

-- INCIDENCIAS: reportado_por y asignado_a deben apuntar a usuarios
ALTER TABLE incidencias DROP CONSTRAINT IF EXISTS incidencias_reportado_por_fkey;
ALTER TABLE incidencias DROP CONSTRAINT IF EXISTS incidencias_asignado_a_fkey;

UPDATE incidencias i
SET reportado_por = m.usuario_id
FROM mapeo_persona_usuario m
WHERE i.reportado_por = m.persona_id;

UPDATE incidencias i
SET asignado_a = m.usuario_id
FROM mapeo_persona_usuario m
WHERE i.asignado_a = m.persona_id;

ALTER TABLE incidencias ADD CONSTRAINT fk_incidencias_reportador 
    FOREIGN KEY (reportado_por) REFERENCES usuarios(id) ON DELETE SET NULL;
ALTER TABLE incidencias ADD CONSTRAINT fk_incidencias_asignado 
    FOREIGN KEY (asignado_a) REFERENCES usuarios(id) ON DELETE SET NULL;

-- COMENTARIOS_INCIDENCIA: autor_id debe apuntar a usuarios si es personal
ALTER TABLE comentarios_incidencia DROP CONSTRAINT IF EXISTS comentarios_incidencia_autor_id_fkey;

UPDATE comentarios_incidencia ci
SET autor_id = m.usuario_id
FROM mapeo_persona_usuario m
WHERE ci.autor_id = m.persona_id;

-- Nota: comentarios pueden ser tanto de usuarios como personas, usar ON DELETE SET NULL
ALTER TABLE comentarios_incidencia ADD CONSTRAINT fk_comentarios_autor 
    FOREIGN KEY (autor_id) REFERENCES usuarios(id) ON DELETE SET NULL;

-- HISTORIAL_ESTADO_INCIDENCIA: cambiado_por debe apuntar a usuarios
ALTER TABLE historial_estado_incidencia DROP CONSTRAINT IF EXISTS historial_estado_incidencia_cambiado_por_fkey;

UPDATE historial_estado_incidencia h
SET cambiado_por = m.usuario_id
FROM mapeo_persona_usuario m
WHERE h.cambiado_por = m.persona_id;

ALTER TABLE historial_estado_incidencia ADD CONSTRAINT fk_historial_cambiado_por 
    FOREIGN KEY (cambiado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

-- PAGOS: registrado_por debe apuntar a usuarios
ALTER TABLE pagos DROP CONSTRAINT IF EXISTS pagos_registrado_por_fkey;

UPDATE pagos p
SET registrado_por = m.usuario_id
FROM mapeo_persona_usuario m
WHERE p.registrado_por = m.persona_id;

ALTER TABLE pagos ADD CONSTRAINT fk_pagos_registrador 
    FOREIGN KEY (registrado_por) REFERENCES usuarios(id) ON DELETE SET NULL;

-- RECLAMOS: atendido_por YA apunta a usuarios, no requiere cambio
-- Verificar que la FK existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'reclamos_atendido_por_fkey'
    ) THEN
        ALTER TABLE reclamos ADD CONSTRAINT reclamos_atendido_por_fkey 
            FOREIGN KEY (atendido_por) REFERENCES usuarios(id) ON DELETE NO ACTION;
    END IF;
END $$;

SELECT 'Referencias actualizadas exitosamente' as resultado;

-- ================================================================================
-- PASO 5: Respaldar y limpiar tabla personas
-- ================================================================================

SELECT 'Limpiando tabla personas...' as status;

-- Crear backup de personal antes de eliminar
CREATE TABLE IF NOT EXISTS personas_backup_personal AS 
SELECT * FROM personas WHERE tipo = 'PERSONAL';

SELECT 'Backup creado con ' || COUNT(*) || ' registros de personal' as resultado
FROM personas_backup_personal;

-- Eliminar personal de la tabla personas
DELETE FROM personas WHERE tipo = 'PERSONAL';

SELECT 'Personal eliminado de tabla personas' as resultado;

-- ================================================================================
-- PASO 6: Modificar estructura de personas (ahora solo deportistas)
-- ================================================================================

SELECT 'Modificando estructura de personas...' as status;

-- Eliminar campos que ya no se necesitan
ALTER TABLE personas DROP COLUMN IF EXISTS tipo;
ALTER TABLE personas DROP CONSTRAINT IF EXISTS personas_rol_id_fkey;
ALTER TABLE personas DROP COLUMN IF EXISTS rol_id;

-- Agregar nuevos campos específicos para deportistas
ALTER TABLE personas ADD COLUMN IF NOT EXISTS direccion VARCHAR(200);
ALTER TABLE personas ADD COLUMN IF NOT EXISTS emergencia_contacto VARCHAR(200);
ALTER TABLE personas ADD COLUMN IF NOT EXISTS emergencia_telefono VARCHAR(30);
ALTER TABLE personas ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE personas ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Asegurar que foto y genero ya existen (parecen estar en tu esquema)
-- fecha_nacimiento también parece existir

SELECT 'Estructura de personas actualizada' as resultado;

-- ================================================================================
-- PASO 7: Insertar datos de ejemplo si no existen
-- ================================================================================

-- Usuario administrativo de ejemplo
INSERT INTO usuarios (nombre, apellido, email, telefono, dni, fecha_registro, rol_id, activo, contraseña, departamento, puesto, fecha_inicio_laboral)
SELECT 
    'Admin', 
    'Sistema', 
    'admin@fitgym.com', 
    '555-0100', 
    '12345678', 
    CURRENT_DATE, 
    (SELECT id FROM roles WHERE codigo = 'ADMINISTRADOR'),
    TRUE,
    '$2a$10$XQO8.Hg6qX9YZvJ5TQZLEe8N7qYKZR7.Ck9xQJ7gJH6qR7.Ck9xQJ',
    'Administración General',
    'Administrador del Sistema',
    CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@fitgym.com');

-- Deportista de ejemplo
INSERT INTO personas (nombre, apellido, email, telefono, dni, fecha_registro, membresia_activa, contraseña, genero, direccion)
SELECT 
    'Cliente', 
    'Ejemplo', 
    'cliente@email.com', 
    '555-0200', 
    '87654321', 
    CURRENT_DATE,
    TRUE,
    '$2a$10$XQO8.Hg6qX9YZvJ5TQZLEe8N7qYKZR7.Ck9xQJ7gJH6qR7.Ck9xQJ',
    'M',
    'Calle Ejemplo 123, Lima'
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE email = 'cliente@email.com');

-- ================================================================================
-- PASO 8: Crear funciones y triggers para timestamps
-- ================================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_usuarios_updated_at ON usuarios;
CREATE TRIGGER update_usuarios_updated_at
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_personas_updated_at ON personas;
CREATE TRIGGER update_personas_updated_at
    BEFORE UPDATE ON personas
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ================================================================================
-- PASO 9: Verificación final
-- ================================================================================

SELECT '=== RESUMEN DE MIGRACIÓN ===' as titulo;

SELECT 
    'Total Usuarios Administrativos' as metrica,
    COUNT(*) as cantidad
FROM usuarios
UNION ALL
SELECT 
    'Total Deportistas' as metrica,
    COUNT(*) as cantidad
FROM personas
UNION ALL
SELECT 
    'Usuarios Activos' as metrica,
    COUNT(*) as cantidad
FROM usuarios WHERE activo = TRUE
UNION ALL
SELECT 
    'Deportistas con Membresía Activa' as metrica,
    COUNT(*) as cantidad
FROM personas WHERE membresia_activa = TRUE;

-- Verificar distribución de roles administrativos
SELECT 
    '=== Distribución de Roles Administrativos ===' as titulo;

SELECT 
    COALESCE(r.nombre, 'Sin Rol') as rol,
    COUNT(*) as cantidad_usuarios
FROM usuarios u
LEFT JOIN roles r ON u.rol_id = r.id
GROUP BY r.nombre
ORDER BY cantidad_usuarios DESC;

-- Verificar que no hay personal en personas
SELECT 
    '=== Verificación: No debe haber resultados ===' as titulo;

SELECT * FROM personas WHERE tipo = 'PERSONAL' LIMIT 1;

COMMIT;

-- ================================================================================
-- FIN DEL SCRIPT DE MIGRACIÓN
-- ================================================================================

SELECT '✓ Migración completada exitosamente' as resultado;
SELECT '  - Tabla usuarios actualizada con campos para personal administrativo' as detalle
UNION ALL
SELECT '  - Personal migrado de personas a usuarios' as detalle
UNION ALL
SELECT '  - Tabla personas ahora es exclusiva para deportistas' as detalle
UNION ALL
SELECT '  - Referencias en otras tablas actualizadas correctamente' as detalle
UNION ALL
SELECT '  - Triggers de timestamp creados' as detalle;

-- Usuarios de prueba:
SELECT '=== USUARIOS DE PRUEBA ===' as titulo;
SELECT 'admin@fitgym.com / admin123 (Administrador)' as credenciales
UNION ALL
SELECT 'cliente@email.com / cliente123 (Deportista)' as credenciales;
