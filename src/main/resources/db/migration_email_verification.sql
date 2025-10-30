-- ============================================
-- Migración: Sistema de Verificación de Email
-- Base de datos: gimnasiodbM (PostgreSQL)
-- Fecha: 2024
-- ============================================

-- 1. Agregar columnas a la tabla personas para control de verificación
ALTER TABLE personas 
ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS email_verificado BOOLEAN DEFAULT FALSE;

-- 2. Crear tabla verification_tokens para almacenar tokens de verificación
CREATE TABLE IF NOT EXISTS verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    persona_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_verification_token_persona 
        FOREIGN KEY (persona_id) 
        REFERENCES personas(id) 
        ON DELETE CASCADE
);

-- 3. Crear índice para búsqueda rápida por token
CREATE INDEX IF NOT EXISTS idx_verification_token 
ON verification_tokens(token);

-- 4. Crear índice para búsqueda por persona
CREATE INDEX IF NOT EXISTS idx_verification_persona 
ON verification_tokens(persona_id);

-- 5. Agregar comentarios para documentación
COMMENT ON COLUMN personas.activo IS 'Indica si la cuenta está activa (email verificado). FALSE hasta que el usuario verifique su email';
COMMENT ON COLUMN personas.email_verificado IS 'Indica si el email fue verificado mediante el token enviado por correo';
COMMENT ON TABLE verification_tokens IS 'Tokens UUID de verificación de email. Expiran después de 24 horas';
COMMENT ON COLUMN verification_tokens.token IS 'Token UUID único generado para verificación';
COMMENT ON COLUMN verification_tokens.fecha_creacion IS 'Fecha y hora de creación del token';
COMMENT ON COLUMN verification_tokens.fecha_expiracion IS 'Fecha y hora de expiración (24h después de creación)';
COMMENT ON COLUMN verification_tokens.usado IS 'Indica si el token ya fue utilizado (tokens de un solo uso)';

-- ============================================
-- IMPORTANTE: Decisión sobre cuentas existentes
-- ============================================
-- Opción A: Mantener cuentas existentes ACTIVAS (recomendado para desarrollo)
-- Descomenta las siguientes líneas si quieres que las cuentas actuales sigan funcionando
/*
UPDATE personas 
SET activo = TRUE, 
    email_verificado = TRUE 
WHERE activo IS NULL OR activo = FALSE;
*/

-- Opción B: Requerir verificación para TODAS las cuentas (recomendado para producción nueva)
-- Las cuentas existentes quedarán inactivas hasta que verifiquen su email
-- No ejecutes nada, el DEFAULT FALSE ya está aplicado

-- ============================================
-- Verificación de la migración
-- ============================================
-- Ejecuta estas consultas para verificar que todo se creó correctamente:

-- Ver estructura de tabla personas actualizada
-- SELECT column_name, data_type, column_default, is_nullable
-- FROM information_schema.columns
-- WHERE table_name = 'personas' AND column_name IN ('activo', 'email_verificado');

-- Ver estructura de tabla verification_tokens
-- SELECT column_name, data_type, column_default, is_nullable
-- FROM information_schema.columns
-- WHERE table_name = 'verification_tokens';

-- Ver índices creados
-- SELECT indexname, indexdef
-- FROM pg_indexes
-- WHERE tablename = 'verification_tokens';

-- Contar tokens activos
-- SELECT COUNT(*) as tokens_activos FROM verification_tokens WHERE usado = FALSE AND fecha_expiracion > NOW();

-- Ver estado de cuentas de deportistas
-- SELECT id, nombre, apellido, email, activo, email_verificado, fecha_registro
-- FROM personas
-- ORDER BY fecha_registro DESC
-- LIMIT 10;

-- ============================================
-- Rollback (en caso de necesitar revertir)
-- ============================================
-- Si necesitas deshacer esta migración, ejecuta:
/*
DROP TABLE IF EXISTS verification_tokens CASCADE;
ALTER TABLE personas DROP COLUMN IF EXISTS activo;
ALTER TABLE personas DROP COLUMN IF EXISTS email_verificado;
*/

-- ============================================
-- FIN DE LA MIGRACIÓN
-- ============================================
