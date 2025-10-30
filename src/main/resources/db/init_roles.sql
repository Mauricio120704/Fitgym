-- Script para inicializar los roles en la base de datos
-- Ejecutar este script antes de iniciar la aplicación

-- Crear tabla de roles si no existe (opcional, ya debería existir)
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(50) NOT NULL
);

-- Insertar roles del sistema con IDs específicos
-- IMPORTANTE: El rol DEPORTISTA debe tener id = 4
INSERT INTO roles (id, codigo, nombre) VALUES 
(1, 'ADMINISTRADOR', 'Administrador'),
(2, 'RECEPCIONISTA', 'Recepcionista'),
(3, 'ENTRENADOR', 'Entrenador'),
(4, 'DEPORTISTA', 'Deportista')
ON CONFLICT (id) DO NOTHING;

-- Verificar que los roles se insertaron correctamente
SELECT * FROM roles ORDER BY id;
