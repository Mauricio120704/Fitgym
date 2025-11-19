-- Migration: Create equipment and maintenance tables
-- Created: 2025-11-19
-- Description: Create tables for equipment management and maintenance tracking

-- Create equipos table
CREATE TABLE IF NOT EXISTS equipos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    tipo VARCHAR(100) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    numero_serie VARCHAR(100) NOT NULL UNIQUE,
    ubicacion VARCHAR(255),
    fecha_adquisicion DATE,
    estado VARCHAR(50) NOT NULL DEFAULT 'OPERATIVO',
    descripcion TEXT,
    ultimo_mantenimiento DATE,
    proximo_mantenimiento DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create mantenimientos table
CREATE TABLE IF NOT EXISTS mantenimientos (
    id BIGSERIAL PRIMARY KEY,
    equipo_id BIGINT NOT NULL,
    fecha_servicio DATE NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_servicio VARCHAR(50) NOT NULL,
    tecnico VARCHAR(255),
    descripcion_servicio TEXT,
    costo DECIMAL(10,2),
    piezas_reemplazadas TEXT,
    proximo_mantenimiento_sugerido DATE,
    estado VARCHAR(50) NOT NULL DEFAULT 'COMPLETADO',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipo_id) REFERENCES equipos(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_equipos_tipo ON equipos(tipo);
CREATE INDEX IF NOT EXISTS idx_equipos_estado ON equipos(estado);
CREATE INDEX IF NOT EXISTS idx_equipos_ubicacion ON equipos(ubicacion);
CREATE INDEX IF NOT EXISTS idx_mantenimientos_equipo_id ON mantenimientos(equipo_id);
CREATE INDEX IF NOT EXISTS idx_mantenimientos_fecha_servicio ON mantenimientos(fecha_servicio);
CREATE INDEX IF NOT EXISTS idx_mantenimientos_tipo_servicio ON mantenimientos(tipo_servicio);

-- Add comments for documentation
COMMENT ON TABLE equipos IS 'Tabla para almacenar información de equipos del gimnasio';
COMMENT ON TABLE mantenimientos IS 'Tabla para registrar el historial de mantenimiento de equipos';

COMMENT ON COLUMN equipos.estado IS 'Estado del equipo: OPERATIVO, EN_MANTENIMIENTO, DAÑADO, OBSOLETO';
COMMENT ON COLUMN mantenimientos.tipo_servicio IS 'Tipo de servicio: PREVENTIVO, CORRECTIVO, EMERGENCIA';
COMMENT ON COLUMN mantenimientos.estado IS 'Estado del mantenimiento: PENDIENTE, EN_PROGRESO, COMPLETADO, CANCELADO';
