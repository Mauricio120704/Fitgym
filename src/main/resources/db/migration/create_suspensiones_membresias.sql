CREATE TABLE IF NOT EXISTS suspensiones_membresias (
  id BIGSERIAL PRIMARY KEY,
  suscripcion_id BIGINT NOT NULL REFERENCES suscripciones(id) ON DELETE CASCADE,
  usuario_id BIGINT NOT NULL REFERENCES personas(id) ON DELETE CASCADE,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  motivo TEXT NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'pendiente',
  aprobado_por BIGINT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_suspensiones_membresias_usuario ON suspensiones_membresias(usuario_id);
CREATE INDEX IF NOT EXISTS idx_suspensiones_membresias_suscripcion ON suspensiones_membresias(suscripcion_id);
