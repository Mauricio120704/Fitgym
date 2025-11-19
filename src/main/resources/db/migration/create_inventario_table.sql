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
