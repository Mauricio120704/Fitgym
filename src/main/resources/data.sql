-- =========================
-- Datos de ejemplo para gimnasiov1 (PostgreSQL)
-- =========================

-- Roles base
INSERT INTO roles (codigo, nombre) VALUES
('ADMINISTRADOR','Administrador'),
('RECEPCIONISTA','Recepcionista'),
('ENTRENADOR','Entrenador'),
('DEPORTISTA','Deportista')
ON CONFLICT (codigo) DO NOTHING;

-- Personal de ejemplo
INSERT INTO usuarios (nombre, apellido, email, telefono, dni, rol_id, activo, fecha_registro, contraseña) VALUES
('Laura','Gómez','laura.gomez@email.com','555-0201','74125896',
 (SELECT id FROM roles WHERE codigo='ADMINISTRADOR'), TRUE, DATE '2024-01-14', '$2a$10$encrypted_password')
ON CONFLICT (email) DO NOTHING;

INSERT INTO usuarios (nombre, apellido, email, telefono, dni, rol_id, activo, fecha_registro, contraseña) VALUES
('María','García','maria.garcia@email.com','555-0202','70234561',
 (SELECT id FROM roles WHERE codigo='RECEPCIONISTA'), TRUE, DATE '2023-06-01', '$2a$10$encrypted_password')
ON CONFLICT (email) DO NOTHING;

INSERT INTO usuarios (nombre, apellido, email, telefono, dni, rol_id, activo, fecha_registro, contraseña) VALUES
('Carlos','López','carlos.lopez@email.com','555-0203','78901234',
 (SELECT id FROM roles WHERE codigo='ENTRENADOR'), TRUE, DATE '2024-03-10', '$2a$10$encrypted_password')
ON CONFLICT (email) DO NOTHING;

INSERT INTO usuarios (nombre, apellido, email, telefono, dni, rol_id, activo, fecha_registro, contraseña) VALUES
('Ana','Martínez','ana.martinez@email.com','555-0204','77889966',
 (SELECT id FROM roles WHERE codigo='RECEPCIONISTA'), TRUE, DATE '2023-08-20', '$2a$10$encrypted_password')
ON CONFLICT (email) DO NOTHING;

-- Deportista
INSERT INTO personas (nombre, apellido, email, telefono, dni, membresia_activa, activo, fecha_registro, contraseña) VALUES
('Juan','Pérez','juan.perez@email.com','555-0301','55667788', TRUE, FALSE, CURRENT_DATE, '$2a$10$encrypted_password')
ON CONFLICT (email) DO NOTHING;

-- Planes
INSERT INTO planes (nombre, precio, frecuencia) VALUES
('Membresía Básica', 599.00, 'Mensual'),
('Membresía Premium', 899.00, 'Mensual'),
('Membresía VIP', 1299.00, 'Mensual')
ON CONFLICT DO NOTHING;

-- Suscripción activa para la tarjeta del historial
INSERT INTO suscripciones (deportista_id, plan_id, estado, fecha_inicio, proximo_pago)
SELECT p.id, pl.id, 'Activa', (CURRENT_DATE - INTERVAL '20 days')::date, (CURRENT_DATE + INTERVAL '15 days')::date
FROM personas p
JOIN planes pl ON pl.nombre = 'Membresía Premium'
WHERE p.email = 'juan.perez@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM suscripciones s
    WHERE s.deportista_id = p.id AND s.estado = 'Activa'
  );

-- Pagos del deportista
INSERT INTO pagos (codigo_pago, deportista_id, fecha, metodo_pago, monto, estado, plan_servicio)
SELECT 'PAY-001', p.id, (CURRENT_DATE - INTERVAL '30 days')::date, 'Tarjeta de Crédito', 899.00, 'Completado', 'Membresía Premium'
FROM personas p WHERE p.email='juan.perez@email.com'
ON CONFLICT (codigo_pago) DO NOTHING;

INSERT INTO pagos (codigo_pago, deportista_id, fecha, metodo_pago, monto, estado, plan_servicio)
SELECT 'PAY-002', p.id, (CURRENT_DATE - INTERVAL '1 days')::date, 'Tarjeta de Crédito', 899.00, 'Completado', 'Membresía Premium'
FROM personas p WHERE p.email='juan.perez@email.com'
ON CONFLICT (codigo_pago) DO NOTHING;

-- Incidencia de ejemplo
INSERT INTO incidencias (titulo, descripcion, categoria, prioridad, estado, reportado_por)
SELECT 'Fuga en ducha', 'Se detectó una fuga en la ducha del vestuario masculino.', 'Mantenimiento', 'Alta', 'Abierta',
       (SELECT id FROM personas WHERE email='laura.gomez@email.com')
WHERE NOT EXISTS (SELECT 1 FROM incidencias WHERE titulo='Fuga en ducha');

-- Clase y reserva de ejemplo (deshabilitado)
-- INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
-- SELECT 'Spinning', 'Clase de alta intensidad en bicicleta estática.',
--        (SELECT id FROM personas WHERE email='carlos.lopez@email.com'),
--        20, NOW() + INTERVAL '3 days', 60, 'Programada'
-- WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Spinning');

-- INSERT INTO reservas_clase (clase_id, deportista_id, estado)
-- SELECT c.id, p.id, 'Reservado'
-- FROM clases c, personas p
-- WHERE c.nombre='Spinning' AND p.email='juan.perez@email.com'
--   AND NOT EXISTS (
--     SELECT 1 FROM reservas_clase rc WHERE rc.clase_id=c.id AND rc.deportista_id=p.id
--   );

-- Evaluación de ejemplo
INSERT INTO evaluaciones (deportista_id, evaluador_id, peso_kg, estatura_cm, imc, grasa_corporal_pct, observaciones)
SELECT p.id, (SELECT id FROM personas WHERE email='laura.gomez@email.com'), 78.5, 175.0, 25.6, 18.2,
       'Buen progreso, mantener plan actual'
FROM personas p WHERE p.email='juan.perez@email.com'
AND NOT EXISTS (SELECT 1 FROM evaluaciones WHERE deportista_id=p.id);

-- Entrenamiento y ejercicios de ejemplo
INSERT INTO entrenamientos (nombre, descripcion, creado_por)
SELECT 'Plan Fuerza 8 semanas', 'Rutina progresiva de fuerza para cuerpo completo',
       (SELECT id FROM personas WHERE email='carlos.lopez@email.com')
WHERE NOT EXISTS (SELECT 1 FROM entrenamientos WHERE nombre='Plan Fuerza 8 semanas');

INSERT INTO ejercicios (nombre, grupo_muscular)
VALUES ('Sentadilla', 'Piernas'), ('Press banca', 'Pecho'), ('Peso muerto', 'Espalda')
ON CONFLICT DO NOTHING;

-- Vincular ejercicios al entrenamiento con orden
INSERT INTO entrenamiento_ejercicio (entrenamiento_id, ejercicio_id, orden, series, repeticiones, descanso_segundos)
SELECT e.id, x.id, 1, 5, '5', 120
FROM entrenamientos e, ejercicios x
WHERE e.nombre='Plan Fuerza 8 semanas' AND x.nombre='Sentadilla'
  AND NOT EXISTS (SELECT 1 FROM entrenamiento_ejercicio WHERE entrenamiento_id=e.id AND orden=1);

INSERT INTO entrenamiento_ejercicio (entrenamiento_id, ejercicio_id, orden, series, repeticiones, descanso_segundos)
SELECT e.id, x.id, 2, 5, '5', 120
FROM entrenamientos e, ejercicios x
WHERE e.nombre='Plan Fuerza 8 semanas' AND x.nombre='Press banca'
  AND NOT EXISTS (SELECT 1 FROM entrenamiento_ejercicio WHERE entrenamiento_id=e.id AND orden=2);

INSERT INTO entrenamiento_ejercicio (entrenamiento_id, ejercicio_id, orden, series, repeticiones, descanso_segundos)
SELECT e.id, x.id, 3, 5, '5', 120
FROM entrenamientos e, ejercicios x
WHERE e.nombre='Plan Fuerza 8 semanas' AND x.nombre='Peso muerto'
  AND NOT EXISTS (SELECT 1 FROM entrenamiento_ejercicio WHERE entrenamiento_id=e.id AND orden=3);

-- Asignación de plan al deportista
INSERT INTO plan_deportista (deportista_id, entrenamiento_id, fecha_inicio, estado)
SELECT p.id, e.id, CURRENT_DATE, 'Activo'
FROM personas p, entrenamientos e
WHERE p.email='juan.perez@email.com' AND e.nombre='Plan Fuerza 8 semanas'
  AND NOT EXISTS (
    SELECT 1 FROM plan_deportista pd WHERE pd.deportista_id=p.id AND pd.estado='Activo'
  );

-- =========================
-- Datos adicionales para que las pantallas no aparezcan vacías
-- =========================

-- Clase adicional (Yoga Matutino) (deshabilitado)
-- INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
-- SELECT 'Yoga Matutino', 'Sesión suave de estiramiento y respiración',
--        (SELECT id FROM personas WHERE email='carlos.lopez@email.com'),
--        25, NOW() + INTERVAL '5 days', 60, 'Programada'
-- WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Yoga Matutino');

-- Evaluación adicional (solo si no existe una en el día actual)
INSERT INTO evaluaciones (deportista_id, evaluador_id, fecha, peso_kg, estatura_cm, imc, grasa_corporal_pct, observaciones)
SELECT p.id, (SELECT id FROM personas WHERE email='laura.gomez@email.com'), NOW(), 77.2, 175.0, 25.2, 17.9,
       'Ligera mejora respecto a la evaluación anterior'
FROM personas p
WHERE p.email='juan.perez@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM evaluaciones ev WHERE ev.deportista_id=p.id AND DATE(ev.fecha)=CURRENT_DATE
  );

INSERT INTO entrenamientos (nombre, descripcion, creado_por)
SELECT 'Fuerza', 'dia=Lunes;hora=07:00;dur=60;notas=Enfocado en piernas',
       (SELECT id FROM personas WHERE email='carlos.lopez@email.com')
WHERE NOT EXISTS (
  SELECT 1 FROM entrenamientos WHERE nombre='Fuerza' AND descripcion LIKE 'dia=Lunes;hora=07:00%'
);

-- =========================
-- Promociones: DDL y datos semilla (PostgreSQL)
-- =========================

-- Tabla principal de promociones
CREATE TABLE IF NOT EXISTS promociones (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  descripcion TEXT,
  tipo VARCHAR(20) NOT NULL,
  valor NUMERIC(12,2) NOT NULL,
  max_usos INTEGER NOT NULL,
  usados INTEGER NOT NULL DEFAULT 0,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  estado VARCHAR(15) NOT NULL,
  creado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Membresías aplicables por promoción
CREATE TABLE IF NOT EXISTS promocion_membresias (
  id SERIAL PRIMARY KEY,
  promocion_id INTEGER NOT NULL REFERENCES promociones(id) ON DELETE CASCADE,
  membresia VARCHAR(50) NOT NULL
);

-- Índices útiles
CREATE INDEX IF NOT EXISTS idx_promociones_estado ON promociones(estado);
CREATE INDEX IF NOT EXISTS idx_promociones_fechas ON promociones(fecha_inicio, fecha_fin);

-- Datos semilla (no duplicar si ya existen por nombre)
INSERT INTO promociones (nombre, descripcion, tipo, valor, max_usos, usados, fecha_inicio, fecha_fin, estado)
SELECT 'Descuento Enero', 'Promoción especial de inicio de año para nuevos miembros', 'PERCENTAGE', 20.00, 30, 12,
       DATE '2024-12-31', DATE '2025-01-30', 'EXPIRED'
WHERE NOT EXISTS (SELECT 1 FROM promociones WHERE nombre='Descuento Enero');

INSERT INTO promociones (nombre, descripcion, tipo, valor, max_usos, usados, fecha_inicio, fecha_fin, estado)
SELECT 'Verano Fit', 'Descuento para membresías anuales durante el verano', 'AMOUNT', 500.00, 15, 4,
       DATE '2025-11-30', DATE '2026-02-27', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM promociones WHERE nombre='Verano Fit');

INSERT INTO promociones (nombre, descripcion, tipo, valor, max_usos, usados, fecha_inicio, fecha_fin, estado)
SELECT 'Promo Black Friday', 'Oferta especial de Black Friday', 'PERCENTAGE', 30.00, 50, 0,
       DATE '2024-11-25', DATE '2024-11-30', 'INACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM promociones WHERE nombre='Promo Black Friday');

-- Membresías por promoción
INSERT INTO promocion_membresias (promocion_id, membresia)
SELECT p.id, m FROM promociones p, (VALUES ('Mensual'), ('Trimestral')) AS t(m)
WHERE p.nombre='Descuento Enero'
  AND NOT EXISTS (SELECT 1 FROM promocion_membresias pm WHERE pm.promocion_id=p.id);

INSERT INTO promocion_membresias (promocion_id, membresia)
SELECT p.id, 'Anual' FROM promociones p
WHERE p.nombre='Verano Fit'
  AND NOT EXISTS (SELECT 1 FROM promocion_membresias pm WHERE pm.promocion_id=p.id);

INSERT INTO promocion_membresias (promocion_id, membresia)
SELECT p.id, m FROM promociones p, (VALUES ('Mensual'), ('Trimestral'), ('Anual')) AS t(m)
WHERE p.nombre='Promo Black Friday'
  AND NOT EXISTS (SELECT 1 FROM promocion_membresias pm WHERE pm.promocion_id=p.id);

-- =========================
-- Tabla de Inventario
-- =========================

-- Crear tabla de inventario
CREATE TABLE IF NOT EXISTS inventario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    codigo_producto VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    cantidad INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 0,
    stock_maximo INTEGER NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    proveedor VARCHAR(255) NOT NULL,
    ubicacion VARCHAR(255) NOT NULL,
    fecha_ingreso DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    ultima_actualizacion DATE NOT NULL DEFAULT CURRENT_DATE
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_inventario_categoria ON inventario(categoria);
CREATE INDEX IF NOT EXISTS idx_inventario_estado ON inventario(estado);
CREATE INDEX IF NOT EXISTS idx_inventario_proveedor ON inventario(proveedor);
CREATE INDEX IF NOT EXISTS idx_inventario_ubicacion ON inventario(ubicacion);
CREATE INDEX IF NOT EXISTS idx_inventario_codigo_producto ON inventario(codigo_producto);

-- Insertar datos de ejemplo
INSERT INTO inventario (nombre, categoria, codigo_producto, descripcion, cantidad, stock_minimo, stock_maximo, precio_unitario, proveedor, ubicacion, estado) VALUES
('Proteína Whey 5kg', 'Suplementos', 'PROT001', 'Proteína de suero de alta calidad', 25, 10, 50, 45.99, 'NutriTech', 'Almacén A', 'DISPONIBLE'),
('Creatina Monohidrato 500g', 'Suplementos', 'CREA001', 'Creatina pura monohidratada', 30, 15, 60, 25.50, 'PowerSupplements', 'Almacén B', 'DISPONIBLE'),
('Mancuerras 20kg', 'Equipamiento', 'MANC020', 'Par de mancuerras ajustables de 20kg', 8, 5, 20, 89.99, 'FitnessGear', 'Sala de pesas', 'DISPONIBLE'),
('Cinta de correr T3000', 'Equipamiento', 'CINTA001', 'Cinta de correr profesional con monitor cardíaco', 3, 2, 10, 1299.99, 'ProFit Equipment', 'Sala de cardio', 'BAJO_STOCK'),
('Barra olímpica 20kg', 'Equipamiento', 'BARRA001', 'Barra olímpica estándar de 20kg', 12, 8, 25, 199.99, 'Olympic Fitness', 'Sala de pesas', 'DISPONIBLE'),
('Discos de hierro 10kg', 'Equipamiento', 'DISC010', 'Disco de hierro de 10kg', 0, 10, 50, 29.99, 'IronWeights', 'Sala de pesas', 'AGOTADO'),
('Rodilleras de compresión', 'Accesorios', 'RODI001', 'Rodilleras de compresión para levantamiento', 15, 8, 30, 19.99, 'SportProtection', 'Tienda', 'DISPONIBLE'),
('Shaker de proteína 700ml', 'Accesorios', 'SHAK001', 'Shaker con mezclador esférico', 40, 20, 80, 8.99, 'FitGear', 'Tienda', 'DISPONIBLE'),
('Pre-entreno 300g', 'Suplementos', 'PRE001', 'Suplemento pre-entreno con cafeína', 18, 10, 40, 35.99, 'EnergyBoost', 'Almacén A', 'DISPONIBLE'),
('Guantes de gimnasio', 'Accesorios', 'GUAN001', 'Guantes de cuero con muñequera', 22, 15, 40, 15.99, 'GymWear', 'Tienda', 'DISPONIBLE');

-- Actualizar estados basados en el stock
UPDATE inventario SET estado = 'AGOTADO' WHERE cantidad <= 0;
UPDATE inventario SET estado = 'BAJO_STOCK' WHERE cantidad > 0 AND cantidad <= stock_minimo;
UPDATE inventario SET estado = 'DISPONIBLE' WHERE cantidad > stock_minimo;
