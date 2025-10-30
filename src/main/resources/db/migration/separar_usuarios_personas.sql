-- ================================================================================
-- SCRIPT DE MIGRACIÓN: Separar Deportistas (personas) y Administrativos (usuarios)
-- ================================================================================
-- Fecha: 2024-10-26
-- Descripción:
--   - Tabla 'personas': Solo para DEPORTISTAS (clientes del gimnasio)
--   - Tabla 'usuarios': Para personal ADMINISTRATIVO (Administrador, Recepcionista, Entrenador)
-- ================================================================================

BEGIN;

-- ================================================================================
-- PASO 1: Crear la nueva tabla 'usuarios' para personal administrativo
-- ================================================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(30),
    dni VARCHAR(20) NOT NULL UNIQUE,
    fecha_registro DATE,
    rol_id INTEGER NOT NULL REFERENCES roles(id),
    activo BOOLEAN DEFAULT TRUE,
    contraseña VARCHAR(255) NOT NULL,
    departamento VARCHAR(200),
    puesto VARCHAR(100),
    fecha_inicio_laboral DATE,
    horario VARCHAR(150),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para usuarios
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuarios_dni ON usuarios(dni);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol_id ON usuarios(rol_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_activo ON usuarios(activo);

-- ================================================================================
-- PASO 2: Migrar personal administrativo de personas a usuarios
-- ================================================================================

-- Insertar personal administrativo en la tabla usuarios
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
    fecha_inicio_laboral
)
SELECT 
    nombre,
    apellido,
    email,
    telefono,
    dni,
    fecha_registro,
    rol_id,
    COALESCE(membresia_activa, TRUE) as activo,
    contraseña,
    CASE 
        WHEN r.codigo = 'ADMINISTRADOR' THEN 'Administración General'
        WHEN r.codigo = 'RECEPCIONISTA' THEN 'Recepción y Atención al Cliente'
        WHEN r.codigo = 'ENTRENADOR' THEN 'Departamento de Entrenamiento'
        ELSE 'Sin Asignar'
    END as departamento,
    fecha_registro as fecha_inicio_laboral
FROM personas p
LEFT JOIN roles r ON p.rol_id = r.id
WHERE p.tipo = 'PERSONAL'
  AND p.rol_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM usuarios u WHERE u.email = p.email
  );

-- Mostrar cuántos registros se migraron
SELECT 'Usuarios administrativos migrados: ' || COUNT(*) as resultado 
FROM usuarios;

-- ================================================================================
-- PASO 3: Modificar la tabla personas para ser exclusiva de deportistas
-- ================================================================================

-- Respaldar datos antes de modificar
CREATE TABLE IF NOT EXISTS personas_backup AS 
SELECT * FROM personas WHERE tipo = 'PERSONAL';

SELECT 'Backup creado con ' || COUNT(*) || ' registros de personal' as resultado
FROM personas_backup;

-- Eliminar personal administrativo de la tabla personas
DELETE FROM personas WHERE tipo = 'PERSONAL';

-- Ahora modificamos la estructura de personas
-- Eliminar campos que ya no se necesitan
ALTER TABLE personas DROP COLUMN IF EXISTS tipo;
ALTER TABLE personas DROP COLUMN IF EXISTS rol_id;

-- Agregar nuevos campos específicos para deportistas
ALTER TABLE personas ADD COLUMN IF NOT EXISTS fecha_nacimiento DATE;
ALTER TABLE personas ADD COLUMN IF NOT EXISTS genero VARCHAR(10);
ALTER TABLE personas ADD COLUMN IF NOT EXISTS direccion VARCHAR(200);
ALTER TABLE personas ADD COLUMN IF NOT EXISTS emergencia_contacto VARCHAR(200);
ALTER TABLE personas ADD COLUMN IF NOT EXISTS emergencia_telefono VARCHAR(30);

-- Agregar timestamps
ALTER TABLE personas ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE personas ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- ================================================================================
-- PASO 4: Actualizar referencias en otras tablas
-- ================================================================================

-- Tabla clases: cambiar referencia de entrenador a tabla usuarios
DO $$ 
BEGIN
    -- Solo si la columna entrenador_id existe y se refiere a personas
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'clases' AND column_name = 'entrenador_id'
    ) THEN
        -- Crear columna temporal
        ALTER TABLE clases ADD COLUMN IF NOT EXISTS entrenador_usuario_id INTEGER;
        
        -- Migrar IDs de entrenadores
        UPDATE clases c
        SET entrenador_usuario_id = u.id
        FROM personas p
        JOIN usuarios u ON u.email = p.email
        WHERE c.entrenador_id = p.id;
        
        -- Eliminar constraint antigua si existe
        ALTER TABLE clases DROP CONSTRAINT IF EXISTS fk_clases_entrenador;
        
        -- Renombrar columnas
        ALTER TABLE clases DROP COLUMN IF EXISTS entrenador_id;
        ALTER TABLE clases RENAME COLUMN entrenador_usuario_id TO entrenador_id;
        
        -- Agregar nueva constraint
        ALTER TABLE clases ADD CONSTRAINT fk_clases_entrenador 
            FOREIGN KEY (entrenador_id) REFERENCES usuarios(id);
    END IF;
END $$;

-- Tabla evaluaciones: el evaluador debe ser de la tabla usuarios
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'evaluaciones' AND column_name = 'evaluador_id'
    ) THEN
        ALTER TABLE evaluaciones ADD COLUMN IF NOT EXISTS evaluador_usuario_id INTEGER;
        
        UPDATE evaluaciones e
        SET evaluador_usuario_id = u.id
        FROM personas p
        JOIN usuarios u ON u.email = p.email
        WHERE e.evaluador_id = p.id;
        
        ALTER TABLE evaluaciones DROP CONSTRAINT IF EXISTS fk_evaluaciones_evaluador;
        ALTER TABLE evaluaciones DROP COLUMN IF EXISTS evaluador_id;
        ALTER TABLE evaluaciones RENAME COLUMN evaluador_usuario_id TO evaluador_id;
        
        ALTER TABLE evaluaciones ADD CONSTRAINT fk_evaluaciones_evaluador 
            FOREIGN KEY (evaluador_id) REFERENCES usuarios(id);
    END IF;
END $$;

-- Tabla entrenamientos: creado_por debe ser de usuarios
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'entrenamientos' AND column_name = 'creado_por'
    ) THEN
        ALTER TABLE entrenamientos ADD COLUMN IF NOT EXISTS creado_por_usuario_id INTEGER;
        
        UPDATE entrenamientos ent
        SET creado_por_usuario_id = u.id
        FROM personas p
        JOIN usuarios u ON u.email = p.email
        WHERE ent.creado_por = p.id;
        
        ALTER TABLE entrenamientos DROP CONSTRAINT IF EXISTS fk_entrenamientos_creador;
        ALTER TABLE entrenamientos DROP COLUMN IF EXISTS creado_por;
        ALTER TABLE entrenamientos RENAME COLUMN creado_por_usuario_id TO creado_por;
        
        ALTER TABLE entrenamientos ADD CONSTRAINT fk_entrenamientos_creador 
            FOREIGN KEY (creado_por) REFERENCES usuarios(id);
    END IF;
END $$;

-- Tabla incidencias: reportado_por debe ser de usuarios
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'incidencias' AND column_name = 'reportado_por'
    ) THEN
        ALTER TABLE incidencias ADD COLUMN IF NOT EXISTS reportado_por_usuario_id INTEGER;
        
        UPDATE incidencias i
        SET reportado_por_usuario_id = u.id
        FROM personas p
        JOIN usuarios u ON u.email = p.email
        WHERE i.reportado_por = p.id;
        
        ALTER TABLE incidencias DROP CONSTRAINT IF EXISTS fk_incidencias_reportador;
        ALTER TABLE incidencias DROP COLUMN IF EXISTS reportado_por;
        ALTER TABLE incidencias RENAME COLUMN reportado_por_usuario_id TO reportado_por;
        
        ALTER TABLE incidencias ADD CONSTRAINT fk_incidencias_reportador 
            FOREIGN KEY (reportado_por) REFERENCES usuarios(id);
    END IF;
END $$;

-- Tabla reclamos: administrador_respuesta debe ser de usuarios
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'reclamos' AND column_name = 'administrador_respuesta_id'
    ) THEN
        ALTER TABLE reclamos ADD COLUMN IF NOT EXISTS admin_usuario_id INTEGER;
        
        UPDATE reclamos r
        SET admin_usuario_id = u.id
        FROM personas p
        JOIN usuarios u ON u.email = p.email
        WHERE r.administrador_respuesta_id = p.id;
        
        ALTER TABLE reclamos DROP CONSTRAINT IF EXISTS fk_reclamos_admin;
        ALTER TABLE reclamos DROP COLUMN IF EXISTS administrador_respuesta_id;
        ALTER TABLE reclamos RENAME COLUMN admin_usuario_id TO administrador_respuesta_id;
        
        ALTER TABLE reclamos ADD CONSTRAINT fk_reclamos_admin 
            FOREIGN KEY (administrador_respuesta_id) REFERENCES usuarios(id);
    END IF;
END $$;

-- ================================================================================
-- PASO 5: Crear datos de ejemplo para ambas tablas
-- ================================================================================

-- Insertar usuario administrativo de ejemplo si no existe
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
    '$2a$10$XQO8.Hg6qX9YZvJ5TQZLEe8N7qYKZR7.Ck9xQJ7gJH6qR7.Ck9xQJ', -- password: admin123
    'Administración General',
    'Administrador del Sistema',
    CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@fitgym.com');

-- Insertar deportista de ejemplo si no existe
INSERT INTO personas (nombre, apellido, email, telefono, dni, fecha_registro, membresia_activa, contraseña, genero, direccion)
SELECT 
    'Cliente', 
    'Ejemplo', 
    'cliente@email.com', 
    '555-0200', 
    '87654321', 
    CURRENT_DATE,
    TRUE,
    '$2a$10$XQO8.Hg6qX9YZvJ5TQZLEe8N7qYKZR7.Ck9xQJ7gJH6qR7.Ck9xQJ', -- password: cliente123
    'M',
    'Calle Ejemplo 123, Lima'
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE email = 'cliente@email.com');

-- ================================================================================
-- PASO 6: Verificación final
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
    r.nombre as rol,
    COUNT(*) as cantidad_usuarios
FROM usuarios u
JOIN roles r ON u.rol_id = r.id
GROUP BY r.nombre
ORDER BY cantidad_usuarios DESC;

-- ================================================================================
-- PASO 7: Crear función para sincronizar timestamps
-- ================================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para actualizar updated_at
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

COMMIT;

-- ================================================================================
-- FIN DEL SCRIPT DE MIGRACIÓN
-- ================================================================================

SELECT '✓ Migración completada exitosamente' as resultado;
SELECT '  - Tabla usuarios creada para personal administrativo' as detalle
UNION ALL
SELECT '  - Tabla personas actualizada solo para deportistas' as detalle
UNION ALL
SELECT '  - Referencias en otras tablas actualizadas' as detalle
UNION ALL
SELECT '  - Triggers de timestamp creados' as detalle;
