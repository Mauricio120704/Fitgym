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
INSERT INTO personas (nombre, apellido, email, telefono, dni, tipo, rol_id, membresia_activa, fecha_registro)
SELECT 'Laura','Gómez','laura.gomez@email.com','555-0201','74125896','PERSONAL',
       (SELECT id FROM roles WHERE codigo='ADMINISTRADOR'), TRUE, DATE '2024-01-14'
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE email='laura.gomez@email.com');

INSERT INTO personas (nombre, apellido, email, telefono, dni, tipo, rol_id, membresia_activa, fecha_registro)
SELECT 'María','García','maria.garcia@email.com','555-0202','70234561','PERSONAL',
       (SELECT id FROM roles WHERE codigo='RECEPCIONISTA'), TRUE, DATE '2023-06-01'
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE email='maria.garcia@email.com');

INSERT INTO personas (nombre, apellido, email, telefono, dni, tipo, rol_id, membresia_activa, fecha_registro)
SELECT 'Carlos','López','carlos.lopez@email.com','555-0203','78901234','PERSONAL',
       (SELECT id FROM roles WHERE codigo='ENTRENADOR'), TRUE, DATE '2024-03-10'
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE email='carlos.lopez@email.com');

INSERT INTO personas (nombre, apellido, email, telefono, dni, tipo, rol_id, membresia_activa, fecha_registro)
SELECT 'Ana','Martínez','ana.martinez@email.com','555-0204','77889966','PERSONAL',
       (SELECT id FROM roles WHERE codigo='RECEPCIONISTA'), TRUE, DATE '2023-08-20'
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE email='ana.martinez@email.com');

-- Deportista
INSERT INTO personas (nombre, apellido, email, telefono, dni, tipo, membresia_activa, fecha_registro)
SELECT 'Juan','Pérez','juan.perez@email.com','555-0301','55667788','DEPORTISTA', TRUE, CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE email='juan.perez@email.com');

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

-- Clase y reserva de ejemplo
INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 'Spinning', 'Clase de alta intensidad en bicicleta estática.',
       (SELECT id FROM personas WHERE email='carlos.lopez@email.com'),
       20, NOW() + INTERVAL '3 days', 60, 'Programada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Spinning');

INSERT INTO reservas_clase (clase_id, deportista_id, estado)
SELECT c.id, p.id, 'Reservado'
FROM clases c, personas p
WHERE c.nombre='Spinning' AND p.email='juan.perez@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM reservas_clase rc WHERE rc.clase_id=c.id AND rc.deportista_id=p.id
  );

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

-- Clase adicional (Yoga Matutino)
INSERT INTO clases (nombre, descripcion, entrenador_id, capacidad, fecha, duracion_minutos, estado)
SELECT 'Yoga Matutino', 'Sesión suave de estiramiento y respiración',
       (SELECT id FROM personas WHERE email='carlos.lopez@email.com'),
       25, NOW() + INTERVAL '5 days', 60, 'Programada'
WHERE NOT EXISTS (SELECT 1 FROM clases WHERE nombre='Yoga Matutino');

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
