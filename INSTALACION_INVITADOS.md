# Guía de Instalación - Módulo de Invitados

## 📋 Requisitos Previos
- Base de datos PostgreSQL corriendo en `localhost:5432`
- Base de datos `gimnasioDB2` creada
- Usuario `postgres` con contraseña `123456`
- Aplicación Spring Boot configurada

## 🔧 Pasos de Instalación

### Paso 1: Ejecutar el Script de Migración SQL

Conectarse a la base de datos y ejecutar el siguiente script:

```sql
-- Script de migración para agregar la columna invitado_por_persona_id a la tabla visitantes
-- Esta columna permite asociar visitantes/invitados con miembros del gimnasio (personas/deportistas)

-- Agregar la columna invitado_por_persona_id a la tabla visitantes
ALTER TABLE visitantes 
ADD COLUMN invitado_por_persona_id BIGINT NULL;

-- Agregar la clave foránea que referencia a la tabla personas
ALTER TABLE visitantes 
ADD CONSTRAINT fk_visitante_invitado_por_persona 
FOREIGN KEY (invitado_por_persona_id) 
REFERENCES personas(id) 
ON DELETE SET NULL;

-- Crear índice para mejorar el rendimiento de las consultas
CREATE INDEX idx_visitantes_invitado_por_persona 
ON visitantes(invitado_por_persona_id);

-- Comentarios para documentación
COMMENT ON COLUMN visitantes.invitado_por_persona_id IS 'ID de la persona (miembro/deportista) que invita al visitante';
```

#### Opciones para ejecutar el script:

**Opción A: Usando pgAdmin**
1. Abrir pgAdmin
2. Conectarse a la base de datos `gimnasioDB2`
3. Abrir Query Tool (Tools > Query Tool)
4. Copiar y pegar el script
5. Ejecutar (F5 o botón Execute)

**Opción B: Usando psql desde línea de comandos**
```bash
psql -U postgres -d gimnasioDB2 -f "src/main/resources/db/migration/agregar_invitado_por_persona_visitantes.sql"
```

**Opción C: Usando psql interactivo**
```bash
psql -U postgres -d gimnasioDB2
```
Luego copiar y pegar el script SQL.

### Paso 2: Verificar la Migración

Ejecutar el siguiente query para verificar que la columna se creó correctamente:

```sql
-- Verificar la estructura de la tabla
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'visitantes' 
  AND column_name = 'invitado_por_persona_id';

-- Verificar la clave foránea
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name = 'visitantes'
  AND kcu.column_name = 'invitado_por_persona_id';
```

**Resultado esperado:**
```
column_name                  | data_type | is_nullable
-----------------------------+-----------+-------------
invitado_por_persona_id      | bigint    | YES

constraint_name                      | table_name | column_name              | foreign_table_name | foreign_column_name
-------------------------------------+------------+--------------------------+--------------------+--------------------
fk_visitante_invitado_por_persona    | visitantes | invitado_por_persona_id  | personas           | id
```

### Paso 3: Compilar el Proyecto

```bash
mvn clean compile
```

**Resultado esperado:**
```
[INFO] BUILD SUCCESS
```

### Paso 4: Iniciar la Aplicación

```bash
mvn spring-boot:run
```

O desde tu IDE (Run/Debug).

### Paso 5: Verificar el Funcionamiento

1. **Acceder a la aplicación:**
   - URL: `http://localhost:8080`

2. **Iniciar sesión** con un usuario que tenga rol de ADMINISTRADOR, RECEPCIONISTA o ENTRENADOR

3. **Acceder al módulo de Invitados:**
   - Desde el menú lateral, hacer clic en "Invitados"
   - O directamente: `http://localhost:8080/admin/invitados`

4. **Verificar las funcionalidades:**
   - ✅ Se muestra el listado de miembros
   - ✅ Puedes buscar miembros
   - ✅ Aparece el botón "+" para agregar invitados
   - ✅ Puedes registrar un invitado
   - ✅ Se genera el código de pase (INV-XXXXXXXX)
   - ✅ Puedes ver el historial de invitados por miembro
   - ✅ Puedes registrar la salida de un invitado

## 🐛 Solución de Problemas

### Error: "relation 'visitantes' does not exist"
**Causa:** La tabla visitantes no existe en la base de datos.
**Solución:** Verificar que la tabla visitantes esté creada. Si no existe, revisar los scripts de creación de tablas del proyecto.

### Error: "column 'invitado_por_persona_id' does not exist"
**Causa:** El script de migración no se ejecutó correctamente.
**Solución:** Ejecutar nuevamente el script SQL del Paso 1.

### Error: "Access Denied" al intentar acceder a /admin/invitados
**Causa:** El usuario no tiene los permisos necesarios.
**Solución:** 
- Verificar que el usuario tenga rol ADMINISTRADOR, RECEPCIONISTA o ENTRENADOR
- Verificar que esté autenticado correctamente

### Error: "Cannot find bean of type PersonaRepository"
**Causa:** El proyecto no compiló correctamente.
**Solución:** 
```bash
mvn clean install
```

### La página no carga o muestra error 404
**Causa:** El controlador no está registrado o la ruta es incorrecta.
**Solución:**
- Verificar que `InvitadoController.java` esté en el paquete correcto
- Verificar los logs de la aplicación
- Reiniciar la aplicación

## 📊 Datos de Prueba (Opcional)

Si deseas probar con datos de ejemplo, puedes ejecutar:

```sql
-- Verificar que existan personas (miembros) en la base de datos
SELECT id, nombre, apellido, email, membresia_activa 
FROM personas 
LIMIT 5;

-- Si no hay personas, crear una de prueba
INSERT INTO personas (nombre, apellido, email, dni, telefono, fecha_registro, membresia_activa, activo, email_verificado, contraseña)
VALUES 
('Juan', 'Pérez', 'juan.perez@example.com', '12345678', '999888777', CURRENT_DATE, true, true, true, '$2a$10$dummyHashedPassword'),
('María', 'García', 'maria.garcia@example.com', '87654321', '999777666', CURRENT_DATE, true, true, true, '$2a$10$dummyHashedPassword');

-- Crear un invitado de prueba asociado a Juan Pérez (ajustar el ID según tu base de datos)
INSERT INTO visitantes (nombre_completo, documento_identidad, telefono, email, fecha_hora_ingreso, codigo_pase, motivo_visita, estado, invitado_por_persona_id, registrado_por)
VALUES 
('Pedro Invitado', '11223344', '999666555', 'pedro@example.com', NOW(), 'INV-TEST001', 'Visita de prueba', 'ACTIVO', 1, 1);
```

## ✅ Checklist de Instalación

- [ ] Script SQL ejecutado correctamente
- [ ] Columna `invitado_por_persona_id` creada en tabla `visitantes`
- [ ] Clave foránea `fk_visitante_invitado_por_persona` creada
- [ ] Índice `idx_visitantes_invitado_por_persona` creado
- [ ] Proyecto compilado sin errores
- [ ] Aplicación iniciada correctamente
- [ ] Acceso a `/admin/invitados` funciona
- [ ] Puede registrar un invitado
- [ ] Código de pase se genera correctamente
- [ ] Puede ver historial de invitados
- [ ] Puede registrar salida de invitado

## 📞 Soporte

Si encuentras algún problema durante la instalación:
1. Revisar los logs de la aplicación
2. Verificar la conexión a la base de datos
3. Consultar la documentación completa en `INVITADOS_IMPLEMENTATION.md`

## 🎉 ¡Listo!

Una vez completados todos los pasos, el módulo de invitados estará completamente funcional y listo para usar.
