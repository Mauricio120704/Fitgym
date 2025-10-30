# 🚨 SOLUCIÓN URGENTE: Tabla password_reset_tokens No Existe

## ❌ Error Identificado

```
JDBCException executing update/delete query 
[delete from password_reset_tokens where email = ?]
```

**Causa:** La tabla `password_reset_tokens` **NO EXISTE** en tu base de datos `gimnasioBD`.

---

## ✅ SOLUCIÓN EN 3 PASOS

### **Paso 1: Abrir pgAdmin o DBeaver**

1. Abre **pgAdmin** o **DBeaver**
2. Conéctate a tu servidor PostgreSQL (localhost:5432)
3. Selecciona la base de datos **`gimnasioBD`**

### **Paso 2: Ejecutar el Script SQL**

#### **Opción A: Desde pgAdmin**

1. Click derecho en la base de datos `gimnasioBD`
2. Click en "Query Tool"
3. Copia y pega el siguiente SQL:

```sql
-- Crear tabla password_reset_tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    token VARCHAR(6) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE
);

-- Crear índices
CREATE INDEX IF NOT EXISTS idx_password_reset_email ON password_reset_tokens(email);
CREATE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_expiracion ON password_reset_tokens(fecha_expiracion);
CREATE INDEX IF NOT EXISTS idx_password_reset_usado ON password_reset_tokens(usado);

-- Verificar
SELECT 'Tabla creada correctamente!' as resultado;
```

4. Click en el botón **"Execute"** (F5)
5. Deberías ver: **"Tabla creada correctamente!"**

#### **Opción B: Desde la Terminal (psql)**

```bash
psql -U postgres -d gimnasioBD -f crear_tabla_password_tokens.sql
```

### **Paso 3: Verificar que la Tabla Existe**

Ejecuta esta consulta en pgAdmin/DBeaver:

```sql
-- Verificar que la tabla existe
SELECT 
    table_name, 
    column_name, 
    data_type 
FROM information_schema.columns 
WHERE table_name = 'password_reset_tokens'
ORDER BY ordinal_position;
```

**Resultado esperado:**

| table_name | column_name | data_type |
|------------|-------------|-----------|
| password_reset_tokens | id | bigint |
| password_reset_tokens | email | character varying |
| password_reset_tokens | token | character varying |
| password_reset_tokens | fecha_creacion | timestamp |
| password_reset_tokens | fecha_expiracion | timestamp |
| password_reset_tokens | usado | boolean |

✅ Si ves estas 6 columnas, **¡la tabla está creada correctamente!**

---

## 🔄 Reiniciar y Probar

### **Paso 4: Reiniciar la Aplicación**

```bash
# Detener la aplicación (Ctrl + C)
# Reiniciar
mvn spring-boot:run
```

### **Paso 5: Probar Recuperación de Contraseña**

1. Ve a: `http://localhost:8080/password-recovery`
2. Ingresa: `Leo_iz01@outlook.com` (o tu email de deportista)
3. Click en "Enviar Instrucciones"
4. ✅ **DEBERÍA FUNCIONAR AHORA**

---

## 🔍 Verificar en Logs

Después de reiniciar, los logs deberían mostrar:

```
INFO  PasswordRecoveryService - Iniciando generación de código para email: Leo_iz01@outlook.com
INFO  PasswordRecoveryService - Usuario encontrado en tabla 'personas': Tu Nombre
DEBUG PasswordRecoveryService - Invalidando tokens previos
DEBUG PasswordRecoveryService - Tokens previos invalidados exitosamente
INFO  PasswordRecoveryService - Token guardado en BD
INFO  PasswordRecoveryService - Email enviado exitosamente ✅
```

---

## 📊 SQL Completo (Copia y Pega)

Si prefieres copiar todo de una vez:

```sql
-- ===================================
-- CREAR TABLA PARA TOKENS DE RECUPERACIÓN
-- ===================================

-- Eliminar tabla si existe (solo si quieres empezar de cero)
-- DROP TABLE IF EXISTS password_reset_tokens CASCADE;

-- Crear tabla
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    token VARCHAR(6) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE
);

-- Crear índices para optimizar búsquedas
CREATE INDEX IF NOT EXISTS idx_password_reset_email 
    ON password_reset_tokens(email);

CREATE INDEX IF NOT EXISTS idx_password_reset_token 
    ON password_reset_tokens(token);

CREATE INDEX IF NOT EXISTS idx_password_reset_expiracion 
    ON password_reset_tokens(fecha_expiracion);

CREATE INDEX IF NOT EXISTS idx_password_reset_usado 
    ON password_reset_tokens(usado);

-- Agregar comentarios a la tabla
COMMENT ON TABLE password_reset_tokens IS 
    'Almacena tokens temporales para recuperación de contraseña';

COMMENT ON COLUMN password_reset_tokens.email IS 
    'Email del usuario que solicita recuperación';

COMMENT ON COLUMN password_reset_tokens.token IS 
    'Código de 6 dígitos enviado por email';

COMMENT ON COLUMN password_reset_tokens.fecha_creacion IS 
    'Fecha y hora de creación del token';

COMMENT ON COLUMN password_reset_tokens.fecha_expiracion IS 
    'Fecha y hora de expiración (15 minutos después de creación)';

COMMENT ON COLUMN password_reset_tokens.usado IS 
    'Indica si el token ya fue utilizado';

-- Verificar que todo está bien
SELECT 
    'OK - Tabla password_reset_tokens creada con ' || 
    (SELECT COUNT(*) FROM password_reset_tokens) || 
    ' registros' as estado;

SELECT 
    column_name, 
    data_type, 
    is_nullable 
FROM information_schema.columns 
WHERE table_name = 'password_reset_tokens'
ORDER BY ordinal_position;
```

---

## 🐛 Si el Error Persiste

### **Verificar Conexión a la BD Correcta**

```sql
-- Verificar que estás en la base de datos correcta
SELECT current_database();
```

Debe devolver: **`gimnasioBD`**

### **Verificar Permisos**

```sql
-- Verificar que tienes permisos para crear tablas
SELECT has_table_privilege('postgres', 'password_reset_tokens', 'SELECT');
```

### **Ver Todas las Tablas**

```sql
-- Ver todas las tablas en gimnasioBD
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;
```

---

## 🎯 Resumen de Cambios

### **Archivos Modificados:**

✅ `PasswordResetTokenRepository.java`
- Eliminado `@Transactional` de métodos `@Modifying`
- El servicio ahora maneja todas las transacciones

✅ `PasswordRecoveryService.java`
- Agregado manejo de errores para `invalidateAllTokensByEmail()`
- Logging mejorado

✅ `application.properties`
- Contraseña de Gmail sin espacios

### **Archivo Creado:**

✅ `crear_tabla_password_tokens.sql`
- Script SQL listo para ejecutar
- Incluye verificación de existencia
- Crea todos los índices necesarios

---

## ⚠️ IMPORTANTE

**NO PODRÁS USAR LA RECUPERACIÓN DE CONTRASEÑA HASTA QUE EJECUTES EL SCRIPT SQL**

La tabla `password_reset_tokens` es **OBLIGATORIA** para que el sistema funcione.

---

## ✅ Checklist de Verificación

Después de ejecutar el SQL, verifica:

- [ ] La tabla `password_reset_tokens` existe en gimnasioBD
- [ ] La tabla tiene 6 columnas (id, email, token, fecha_creacion, fecha_expiracion, usado)
- [ ] Los 4 índices fueron creados
- [ ] La aplicación se reinició sin errores
- [ ] Puedes acceder a `/password-recovery`
- [ ] Al ingresar un email, NO sale error JDBC
- [ ] Recibes el email con el código
- [ ] Puedes completar el proceso de recuperación

---

## 🎉 Una Vez Que Funcione

Deberías poder:

1. ✅ Solicitar código de recuperación
2. ✅ Recibir email con código de 6 dígitos
3. ✅ Verificar el código
4. ✅ Establecer nueva contraseña
5. ✅ Iniciar sesión con la nueva contraseña

---

## 📞 Siguiente Paso

**EJECUTA EL SCRIPT SQL AHORA** y luego reinicia la aplicación.

El error desaparecerá inmediatamente después de crear la tabla. 🚀

---

**Archivo SQL listo para ejecutar:** `crear_tabla_password_tokens.sql`

**¡Ejecútalo y todo funcionará!** ✅
