-- Crear tabla de historial de promociones
-- Esta tabla registra todas las acciones realizadas sobre las promociones

CREATE TABLE IF NOT EXISTS promocion_historial (
    id BIGSERIAL PRIMARY KEY,
    promocion_id BIGINT NOT NULL,
    accion VARCHAR(20) NOT NULL,
    estado_anterior VARCHAR(15),
    estado_nuevo VARCHAR(15),
    detalle TEXT,
    realizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(120),
    CONSTRAINT fk_promocion_historial_promocion 
        FOREIGN KEY (promocion_id) 
        REFERENCES promociones(id) 
        ON DELETE CASCADE
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_promocion_historial_promocion_id ON promocion_historial(promocion_id);
CREATE INDEX IF NOT EXISTS idx_promocion_historial_realizado_en ON promocion_historial(realizado_en);
CREATE INDEX IF NOT EXISTS idx_promocion_historial_accion ON promocion_historial(accion);

-- Añadir comentario a la tabla
COMMENT ON TABLE promocion_historial IS 'Historial de cambios realizados sobre las promociones';

-- Comentarios sobre las columnas importantes
COMMENT ON COLUMN promocion_historial.promocion_id IS 'ID de la promoción afectada';
COMMENT ON COLUMN promocion_historial.accion IS 'Tipo de acción: CREAR, EDITAR, TOGGLE, REACTIVAR, ELIMINAR';
COMMENT ON COLUMN promocion_historial.estado_anterior IS 'Estado de la promoción antes del cambio';
COMMENT ON COLUMN promocion_historial.estado_nuevo IS 'Estado de la promoción después del cambio';
COMMENT ON COLUMN promocion_historial.detalle IS 'Detalles adicionales sobre el cambio realizado';
COMMENT ON COLUMN promocion_historial.realizado_en IS 'Fecha y hora cuando se realizó la acción';
COMMENT ON COLUMN promocion_historial.usuario IS 'Usuario que realizó la acción';
