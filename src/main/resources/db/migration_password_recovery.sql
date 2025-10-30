-- =====================================================
-- Script de Migración: Sistema de Recuperación de Contraseña
-- Descripción: Crea la tabla para almacenar tokens de
--              recuperación de contraseña con expiración
-- Fecha: 2024
-- =====================================================

-- Crear tabla password_reset_tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    token VARCHAR(6) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Índices para optimizar búsquedas
    CONSTRAINT idx_email_token UNIQUE (email, token)
);

-- Crear índices adicionales para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_password_reset_email ON password_reset_tokens(email);
CREATE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_expiracion ON password_reset_tokens(fecha_expiracion);
CREATE INDEX IF NOT EXISTS idx_password_reset_usado ON password_reset_tokens(usado);

-- Comentarios sobre la tabla
COMMENT ON TABLE password_reset_tokens IS 'Tabla para almacenar tokens de recuperación de contraseña con código de 6 dígitos';
COMMENT ON COLUMN password_reset_tokens.email IS 'Email del usuario que solicitó la recuperación';
COMMENT ON COLUMN password_reset_tokens.token IS 'Código de 6 dígitos enviado por email';
COMMENT ON COLUMN password_reset_tokens.fecha_creacion IS 'Fecha y hora de creación del token';
COMMENT ON COLUMN password_reset_tokens.fecha_expiracion IS 'Fecha y hora de expiración del token (15 minutos después de creación)';
COMMENT ON COLUMN password_reset_tokens.usado IS 'Indica si el token ya fue utilizado para restablecer la contraseña';

-- =====================================================
-- Script de Rollback (para deshacer los cambios)
-- =====================================================
-- Para ejecutar el rollback, descomenta las siguientes líneas:
--
-- DROP INDEX IF EXISTS idx_password_reset_usado;
-- DROP INDEX IF EXISTS idx_password_reset_expiracion;
-- DROP INDEX IF EXISTS idx_password_reset_token;
-- DROP INDEX IF EXISTS idx_password_reset_email;
-- DROP TABLE IF EXISTS password_reset_tokens;

-- =====================================================
-- Verificación
-- =====================================================
-- Para verificar que la tabla se creó correctamente:
-- SELECT * FROM information_schema.tables WHERE table_name = 'password_reset_tokens';
-- SELECT * FROM password_reset_tokens;
