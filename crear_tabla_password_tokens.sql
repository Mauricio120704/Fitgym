-- =====================================================
-- SCRIPT URGENTE: Crear tabla password_reset_tokens
-- =====================================================
-- IMPORTANTE: Ejecutar este script ANTES de usar la recuperación de contraseña
-- Base de datos: gimnasioBD
-- =====================================================

-- Verificar si la tabla ya existe
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'password_reset_tokens'
    ) THEN
        RAISE NOTICE 'Creando tabla password_reset_tokens...';
        
        -- Crear la tabla
        CREATE TABLE password_reset_tokens (
            id BIGSERIAL PRIMARY KEY,
            email VARCHAR(150) NOT NULL,
            token VARCHAR(6) NOT NULL,
            fecha_creacion TIMESTAMP NOT NULL,
            fecha_expiracion TIMESTAMP NOT NULL,
            usado BOOLEAN NOT NULL DEFAULT FALSE
        );
        
        -- Crear índices para mejorar el rendimiento
        CREATE INDEX idx_password_reset_email ON password_reset_tokens(email);
        CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
        CREATE INDEX idx_password_reset_expiracion ON password_reset_tokens(fecha_expiracion);
        CREATE INDEX idx_password_reset_usado ON password_reset_tokens(usado);
        
        RAISE NOTICE 'Tabla password_reset_tokens creada exitosamente!';
    ELSE
        RAISE NOTICE 'La tabla password_reset_tokens ya existe.';
    END IF;
END $$;

-- Verificar que la tabla se creó correctamente
SELECT 
    'Tabla creada correctamente con ' || COUNT(*) || ' registros.' as resultado
FROM password_reset_tokens;

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================
