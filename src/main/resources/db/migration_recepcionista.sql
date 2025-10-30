-- ============================================
-- Migración: Funcionalidades de Recepcionista
-- Base de datos: gimnasiodbM (PostgreSQL)
-- Fecha: 2024
-- ============================================

-- 1. Crear tabla asistencias para registro de entrada/salida
CREATE TABLE IF NOT EXISTS asistencias (
    id BIGSERIAL PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    fecha_hora_ingreso TIMESTAMP NOT NULL,
    fecha_hora_salida TIMESTAMP,
    CONSTRAINT fk_asistencia_persona 
        FOREIGN KEY (persona_id) 
        REFERENCES personas(id) 
        ON DELETE CASCADE
);

-- 2. Crear índices para asistencias
CREATE INDEX IF NOT EXISTS idx_asistencia_persona 
ON asistencias(persona_id);

CREATE INDEX IF NOT EXISTS idx_asistencia_ingreso 
ON asistencias(fecha_hora_ingreso);

CREATE INDEX IF NOT EXISTS idx_asistencia_salida 
ON asistencias(fecha_hora_salida);

-- 3. Crear tabla lockers para gestión de casilleros
CREATE TABLE IF NOT EXISTS lockers (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(20) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    persona_id BIGINT,
    fecha_asignacion TIMESTAMP,
    CONSTRAINT fk_locker_persona 
        FOREIGN KEY (persona_id) 
        REFERENCES personas(id) 
        ON DELETE SET NULL,
    CONSTRAINT chk_estado_locker 
        CHECK (estado IN ('DISPONIBLE', 'OCUPADO', 'MANTENIMIENTO'))
);

-- 4. Crear índices para lockers
CREATE INDEX IF NOT EXISTS idx_locker_numero 
ON lockers(numero);

CREATE INDEX IF NOT EXISTS idx_locker_estado 
ON lockers(estado);

CREATE INDEX IF NOT EXISTS idx_locker_persona 
ON lockers(persona_id);

-- 5. Insertar lockers de ejemplo (20 lockers numerados)
INSERT INTO lockers (numero, estado) VALUES
('L001', 'DISPONIBLE'),
('L002', 'DISPONIBLE'),
('L003', 'DISPONIBLE'),
('L004', 'DISPONIBLE'),
('L005', 'DISPONIBLE'),
('L006', 'DISPONIBLE'),
('L007', 'DISPONIBLE'),
('L008', 'DISPONIBLE'),
('L009', 'DISPONIBLE'),
('L010', 'DISPONIBLE'),
('L011', 'DISPONIBLE'),
('L012', 'DISPONIBLE'),
('L013', 'DISPONIBLE'),
('L014', 'DISPONIBLE'),
('L015', 'DISPONIBLE'),
('L016', 'DISPONIBLE'),
('L017', 'DISPONIBLE'),
('L018', 'DISPONIBLE'),
('L019', 'DISPONIBLE'),
('L020', 'DISPONIBLE')
ON CONFLICT (numero) DO NOTHING;

-- 6. Agregar comentarios para documentación
COMMENT ON TABLE asistencias IS 'Registros de entrada y salida de deportistas al gimnasio';
COMMENT ON COLUMN asistencias.fecha_hora_ingreso IS 'Fecha y hora de ingreso al gimnasio';
COMMENT ON COLUMN asistencias.fecha_hora_salida IS 'Fecha y hora de salida del gimnasio (NULL si aún está dentro)';

COMMENT ON TABLE lockers IS 'Gestión de casilleros del gimnasio';
COMMENT ON COLUMN lockers.numero IS 'Número identificador del locker';
COMMENT ON COLUMN lockers.estado IS 'Estado del locker: DISPONIBLE, OCUPADO o MANTENIMIENTO';
COMMENT ON COLUMN lockers.persona_id IS 'Persona que tiene asignado el locker (NULL si está disponible)';
COMMENT ON COLUMN lockers.fecha_asignacion IS 'Fecha y hora en que se asignó el locker';

-- ============================================
-- Verificación de la migración
-- ============================================
-- Ejecuta estas consultas para verificar que todo se creó correctamente:

-- Ver asistencias activas (personas dentro del gimnasio)
-- SELECT p.nombre, p.apellido, a.fecha_hora_ingreso
-- FROM asistencias a
-- JOIN personas p ON a.persona_id = p.id
-- WHERE a.fecha_hora_salida IS NULL
-- ORDER BY a.fecha_hora_ingreso DESC;

-- Ver lockers ocupados
-- SELECT l.numero, p.nombre, p.apellido, l.fecha_asignacion
-- FROM lockers l
-- LEFT JOIN personas p ON l.persona_id = p.id
-- WHERE l.estado = 'OCUPADO'
-- ORDER BY l.numero;

-- ============================================
-- Rollback (en caso de necesitar revertir)
-- ============================================
-- Si necesitas deshacer esta migración, ejecuta:
/*
DROP TABLE IF EXISTS asistencias CASCADE;
DROP TABLE IF NOT EXISTS lockers CASCADE;
*/

-- ============================================
-- FIN DE LA MIGRACIÓN
-- ============================================
