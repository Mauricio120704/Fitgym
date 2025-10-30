-- Script para crear la tabla reclamos si no existe
-- Este script es compatible con PostgreSQL

CREATE TABLE IF NOT EXISTS reclamos (
    id SERIAL PRIMARY KEY,
    deportista_id INTEGER NOT NULL REFERENCES personas(id) ON DELETE CASCADE,
    categoria VARCHAR(100) NOT NULL,
    asunto VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    estado VARCHAR(30) DEFAULT 'En proceso' CHECK (estado IN ('Pendiente', 'En proceso', 'Resuelto', 'Cerrado')),
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE,
    prioridad VARCHAR(20) DEFAULT 'Normal' CHECK (prioridad IN ('Baja', 'Normal', 'Alta', 'Crítica')),
    respuesta_admin TEXT,
    atendido_por INTEGER REFERENCES personas(id),
    activo BOOLEAN DEFAULT TRUE
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_reclamos_deportista ON reclamos(deportista_id);
CREATE INDEX IF NOT EXISTS idx_reclamos_estado ON reclamos(estado);
CREATE INDEX IF NOT EXISTS idx_reclamos_fecha_creacion ON reclamos(fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_reclamos_activo ON reclamos(activo);

-- Comentarios para documentación
COMMENT ON TABLE reclamos IS 'Tabla para almacenar los reclamos y quejas de los deportistas';
COMMENT ON COLUMN reclamos.deportista_id IS 'ID del deportista que realiza el reclamo';
COMMENT ON COLUMN reclamos.estado IS 'Estado actual del reclamo: Pendiente, En proceso, Resuelto, Cerrado';
COMMENT ON COLUMN reclamos.prioridad IS 'Nivel de prioridad del reclamo: Baja, Normal, Alta, Crítica';
COMMENT ON COLUMN reclamos.atendido_por IS 'ID del personal que atiende el reclamo';
COMMENT ON COLUMN reclamos.activo IS 'Indica si el reclamo está activo o ha sido eliminado lógicamente';
