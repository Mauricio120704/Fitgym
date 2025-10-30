-- Script de migración para agregar la columna invitado_por_persona_id a la tabla visitantes
-- Esta columna permite asociar visitantes/invitados con miembros del gimnasio (personas/deportistas)

-- Agregar la columna invitado_por_persona_id a la tabla visitantes
ALTER TABLE visitantes 
ADD COLUMN invitado_por_persona_id BIGINT NULL;

-- Agregar la clave foránea que referencia a la tabla personas
ALTER TABLE visitantes 
ADD CONSTRAINT fk_visitante_invitado_por_persona 
FOREIGN KEY (invitado_por_persona_id) 
REFERENCES personas(id) 
ON DELETE SET NULL;

-- Crear índice para mejorar el rendimiento de las consultas
CREATE INDEX idx_visitantes_invitado_por_persona 
ON visitantes(invitado_por_persona_id);

-- Comentarios para documentación
COMMENT ON COLUMN visitantes.invitado_por_persona_id IS 'ID de la persona (miembro/deportista) que invita al visitante';
