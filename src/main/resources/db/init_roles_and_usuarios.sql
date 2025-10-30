-- init_roles_and_usuarios.sql
-- Inserta roles básicos y usuarios administrativos seguros (ON CONFLICT DO NOTHING)
-- Uso: ejecutar manualmente con psql o integrarlo en el proceso de despliegue.

BEGIN;

-- 1) Crear roles base si no existen
INSERT INTO roles (codigo, nombre) VALUES
  ('ADMINISTRADOR', 'Administrador'),
  ('RECEPCIONISTA', 'Recepcionista'),
  ('ENTRENADOR', 'Entrenador')
ON CONFLICT (codigo) DO NOTHING;

-- 2) Insertar usuarios administrativos de ejemplo (contraseñas en bcrypt)
-- Reemplaza los hashes por otros si quieres contraseñas diferentes.
-- Nota: usamos subselect para rol_id; si el role no existe, el INSERT fallará (asegúrate de ejecutar la sección de roles primero).

INSERT INTO usuarios (nombre, apellido, email, telefono, dni, fecha_registro, rol_id, activo, contraseña, departamento, puesto, fecha_inicio_laboral)
VALUES
  (
    'Admin', 'Sistema', 'admin@fitgym.com', '555-0100', '00000001', CURRENT_DATE,
    (SELECT id FROM roles WHERE codigo = 'ADMINISTRADOR'),
    TRUE,
    '$2a$10$XQO8.Hg6qX9YZvJ5TQZLEe8N7qYKZR7.Ck9xQJ7gJH6qR7.Ck9xQJ', -- ejemplo: admin123 (bcrypt)
    'Administración General', 'Administrador del Sistema', CURRENT_DATE
  ),
  (
    'Recepcion', 'Principal', 'recepcion@fitgym.com', '555-0200', '00000002', CURRENT_DATE,
    (SELECT id FROM roles WHERE codigo = 'RECEPCIONISTA'),
    TRUE,
    '$2a$10$XQO8.Hg6qX9YZvJ5TQZLEe8N7qYKZR7.Ck9xQJ7gJH6qR7.Ck9xQJ', -- reutiliza hash de ejemplo
    'Recepción y Atención al Cliente', 'Recepcionista', CURRENT_DATE
  ),
  (
    'Entrenador', 'Principal', 'coach@fitgym.com', '555-0300', '00000003', CURRENT_DATE,
    (SELECT id FROM roles WHERE codigo = 'ENTRENADOR'),
    TRUE,
    '$2a$10$XQO8.Hg6qX9YZvJ5TQZLEe8N7qYKZR7.Ck9xQJ7gJH6qR7.Ck9xQJ', -- reutiliza hash de ejemplo
    'Departamento de Entrenamiento', 'Entrenador', CURRENT_DATE
  )
ON CONFLICT (email) DO NOTHING;

COMMIT;

-- Fin
