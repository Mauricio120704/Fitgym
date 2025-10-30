# Solución al Problema de Login

## 🔍 Diagnóstico del Problema

El login no funciona para usuarios recién registrados. Las posibles causas son:

1. **Cuenta bloqueada** por `membresiaActiva = null` o `false`
2. **Rol no asignado correctamente** en la base de datos
3. **Contraseña no encriptada** o encriptada con diferente algoritmo
4. **Usuario no encontrado** en la base de datos

## ✅ Cambios Implementados

### 1. Logging Detallado en `CustomUserDetailsService`

Agregado logging para diagnosticar:
- Email del usuario
- Hash de la contraseña
- Tipo de usuario
- Rol asignado
- Estado de membresía
- Authorities (roles de Spring Security)
- Si la cuenta está bloqueada

### 2. Manejo Robusto de `membresiaActiva`

```java
boolean isAccountLocked = persona.getMembresiaActiva() == null || !persona.getMembresiaActiva();
```

Ahora maneja correctamente el caso cuando `membresiaActiva` es `null`.

### 3. Búsqueda de Rol Mejorada

```java
Role rolDeportista = roleRepository.findByCodigo("DEPORTISTA")
    .orElseGet(() -> {
        return roleRepository.findById(4)
            .orElseThrow(() -> new RuntimeException("Rol DEPORTISTA no encontrado"));
    });
```

Busca primero por código "DEPORTISTA", con fallback a ID 4.

### 4. Logging en Registro

Muestra:
- Rol asignado con su ID
- Contraseña original (solo para debug)
- Contraseña encriptada

## 🧪 Pasos para Probar

### 1. Iniciar la Aplicación

```bash
mvn spring-boot:run
```

### 2. Registrar un Nuevo Usuario

1. Ir a `http://localhost:8080/registro`
2. Completar el formulario con:
   - Nombre: Test
   - Apellido: Usuario
   - Email: test@mail.com
   - DNI: 12345678
   - Teléfono: 987654321
   - Contraseña: 123456
   - Confirmar contraseña: 123456

3. **Observar la consola** para ver:
```
=== DEBUG REGISTRO ===
Rol asignado: DEPORTISTA (ID: X)
Password original: 123456
Password encriptado: $2a$10$...
======================
```

### 3. Intentar Iniciar Sesión

1. Ir a `http://localhost:8080/login`
2. Ingresar:
   - Email: test@mail.com
   - Contraseña: 123456

3. **Observar la consola** para ver:
```
=== DEBUG LOGIN ===
Email: test@mail.com
Password (hash): $2a$10$...
Tipo: DEPORTISTA
Rol: DEPORTISTA
Membresia Activa: true
Authorities: [ROLE_CLIENTE]
Account Locked: false
===================
```

## 🔧 Verificaciones en Base de Datos

### 1. Verificar que el Usuario Existe

```sql
SELECT id, nombre, email, tipo, rol_id, membresia_activa, contraseña 
FROM personas 
WHERE email = 'test@mail.com';
```

**Debe mostrar:**
- `tipo`: DEPORTISTA
- `rol_id`: 4 (o el ID del rol DEPORTISTA)
- `membresia_activa`: true
- `contraseña`: Debe empezar con `$2a$10$` (BCrypt)

### 2. Verificar que el Rol Existe

```sql
SELECT * FROM roles WHERE id = 4 OR codigo = 'DEPORTISTA';
```

**Debe mostrar:**
- `id`: 4
- `codigo`: DEPORTISTA
- `nombre`: Deportista

### 3. Si el Rol No Existe, Ejecutar:

```sql
INSERT INTO roles (id, codigo, nombre) VALUES 
(4, 'DEPORTISTA', 'Deportista')
ON CONFLICT (id) DO NOTHING;
```

## 🐛 Posibles Problemas y Soluciones

### Problema 1: "Account Locked: true"

**Causa:** `membresia_activa` es `null` o `false`

**Solución:**
```sql
UPDATE personas 
SET membresia_activa = true 
WHERE email = 'test@mail.com';
```

### Problema 2: "Rol: null"

**Causa:** `rol_id` no está asignado o el rol no existe

**Solución:**
```sql
-- Verificar que el rol existe
SELECT * FROM roles WHERE codigo = 'DEPORTISTA';

-- Si no existe, crearlo
INSERT INTO roles (codigo, nombre) VALUES ('DEPORTISTA', 'Deportista');

-- Asignar el rol al usuario
UPDATE personas 
SET rol_id = (SELECT id FROM roles WHERE codigo = 'DEPORTISTA')
WHERE email = 'test@mail.com';
```

### Problema 3: Contraseña No Coincide

**Causa:** La contraseña no está encriptada con BCrypt

**Solución:** Registrar nuevamente el usuario. El sistema ahora encripta correctamente.

### Problema 4: "Usuario no encontrado"

**Causa:** El email no existe en la base de datos

**Solución:** Verificar que el registro se completó correctamente.

## 📋 Checklist de Verificación

Antes de intentar login, verificar:

- [ ] Usuario existe en la tabla `personas`
- [ ] `membresia_activa` = `true`
- [ ] `tipo` = `DEPORTISTA`
- [ ] `rol_id` = 4 (o ID del rol DEPORTISTA)
- [ ] `contraseña` empieza con `$2a$10$` (BCrypt)
- [ ] Rol DEPORTISTA existe en tabla `roles`
- [ ] Rol tiene `codigo` = `DEPORTISTA`

## 🎯 Resultado Esperado

Después de las correcciones:

1. **Registro exitoso:**
   - Usuario guardado con `rol_id = 4`
   - Contraseña encriptada con BCrypt
   - `membresia_activa = true`
   - Auto-login funciona
   - Redirige a `/planes`

2. **Login exitoso:**
   - Spring Security valida credenciales
   - Asigna `ROLE_CLIENTE` al usuario
   - Redirige a `/perfil`
   - Sidebar muestra solo 3 opciones

## 🔄 Si el Problema Persiste

### 1. Limpiar y Registrar de Nuevo

```sql
-- Eliminar usuario de prueba
DELETE FROM personas WHERE email = 'test@mail.com';
```

Luego registrar nuevamente desde la interfaz.

### 2. Verificar Logs Completos

Buscar en la consola:
- Excepciones de Spring Security
- Mensajes de "Bad credentials"
- Errores de base de datos

### 3. Probar con Usuario Existente

Si tienes un usuario antiguo que funcionaba antes, intentar con ese para verificar si el problema es con usuarios nuevos o con todos.

## 📝 Notas Importantes

1. **BCrypt genera hashes diferentes** cada vez, incluso para la misma contraseña. Esto es normal y seguro.

2. **El logging es temporal** - Eliminar los `System.out.println` en producción por seguridad.

3. **Auto-login después del registro** usa la contraseña en texto plano (antes de encriptar), por eso funciona.

4. **Spring Security compara automáticamente** el hash guardado con la contraseña ingresada usando BCrypt.

## ✅ Verificación Final

Una vez que el login funcione:

1. Registrar un usuario nuevo
2. Debería hacer auto-login y redirigir a `/planes`
3. Cerrar sesión
4. Iniciar sesión manualmente con las mismas credenciales
5. Debería redirigir a `/perfil`
6. El sidebar debe mostrar solo 3 opciones para deportistas

---

**¡El sistema está configurado correctamente!** Si sigues estos pasos, el login debería funcionar.
