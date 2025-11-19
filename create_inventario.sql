-- Crear tabla inventario si no existe
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

-- Crear índices para mejor rendimiento
CREATE INDEX IF NOT EXISTS idx_inventario_categoria ON inventario(categoria);
CREATE INDEX IF NOT EXISTS idx_inventario_estado ON inventario(estado);
CREATE INDEX IF NOT EXISTS idx_inventario_proveedor ON inventario(proveedor);
CREATE INDEX IF NOT EXISTS idx_inventario_ubicacion ON inventario(ubicacion);
CREATE INDEX IF NOT EXISTS idx_inventario_codigo_producto ON inventario(codigo_producto);

-- Insertar datos de ejemplo
INSERT INTO inventario (nombre, categoria, codigo_producto, descripcion, cantidad, stock_minimo, stock_maximo, precio_unitario, proveedor, ubicacion, estado) VALUES
('Mancuerna 10 kg', 'Pesas', 'MANC-001', 'Mancuerna de 10 kg con recubrimiento de goma', 15, 5, 25, 25.99, 'FitnessPro', 'Sala de pesas', 'DISPONIBLE'),
('Banda elástica resistente', 'Accesorios', 'BAND-001', 'Banda elástica de alta resistencia', 30, 10, 50, 8.50, 'SportGear', 'Área funcional', 'DISPONIBLE'),
('Colchoneta yoga', 'Accesorios', 'YOGA-001', 'Colchoneta antideslizante para yoga', 12, 8, 20, 35.00, 'ZenFitness', 'Sala de yoga', 'DISPONIBLE'),
('Barra olímpica 20 kg', 'Barras', 'BARR-001', 'Barra olímpica estándar de 20 kg', 6, 2, 10, 150.00, 'PowerLift', 'Sala de pesas', 'BAJO_STOCK'),
('Disco 25 kg', 'Discos', 'DISC-001', 'Disco de hierro de 25 kg', 8, 4, 15, 45.00, 'IronMax', 'Sala de pesas', 'DISPONIBLE'),
('Cuerda para saltar', 'Accesorios', 'CUER-001', 'Cuerda de velocidad con rodamientos', 25, 15, 40, 12.99, 'SpeedFit', 'Área de cardio', 'DISPONIBLE'),
('Balón medicinal 5 kg', 'Accesorios', 'BALM-001', 'Balón medicinal de 5 kg con textura', 10, 5, 15, 28.50, 'MedBall', 'Área funcional', 'DISPONIBLE'),
('Banco de pesas ajustable', 'Muebles', 'BANC-001', 'Banco ajustable con múltiples ángulos', 4, 2, 8, 220.00, 'GymEquip', 'Sala de pesas', 'BAJO_STOCK'),
('Set de mancuernas ajustables', 'Pesas', 'MANC-002', 'Set de mancuernas de 2.5 a 24 kg', 3, 1, 5, 180.00, 'FlexFit', 'Sala de pesas', 'BAJO_STOCK'),
('Rodillo abdominal', 'Accesorios', 'RODL-001', 'Rodillo abdominal con doble rueda', 18, 10, 30, 15.99, 'CoreFit', 'Área funcional', 'DISPONIBLE')
ON CONFLICT (codigo_producto) DO NOTHING;
