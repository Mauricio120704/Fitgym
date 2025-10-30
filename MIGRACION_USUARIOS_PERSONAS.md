# Migración: Separación de Deportistas y Personal Administrativo

## 📋 Resumen

Este documento describe la migración del sistema para separar correctamente:
- **Tabla `personas`**: Solo para DEPORTISTAS (clientes del gimnasio)
- **Tabla `usuarios`**: Para personal ADMINISTRATIVO (Administrador, Recepcionista, Entrenador)

---

## 🎯 Objetivo

Diferenciar claramente entre los roles administrativos y los deportistas, facilitando:
- Gestión específica de perfiles de deportistas vs administrativos
- Campos personalizados para cada tipo de usuario
- Mejor organización y seguridad del sistema
- Simplificación de consultas y lógica de negocio

---

## 📁 Archivos Creados

### 1. **Modelo Usuario**
- `src/main/java/com/integradorii/gimnasiov1/model/Usuario.java`
- Entidad para personal administrativo
- Campos específicos: departamento, puesto, horario, fecha_inicio_laboral

### 2. **Modelo Persona Actualizado**
- `src/main/java/com/integradorii/gimnasiov1/model/Persona.java`
- Ahora es exclusivo para deportistas
- Nuevos campos: fecha_nacimiento, genero, direccion, contacto_emergencia

### 3. **Repository Usuario**
- `src/main/java/com/integradorii/gimnasiov1/repository/UsuarioRepository.java`
- Métodos específicos para gestionar usuarios administrativos

### 4. **Script SQL de Migración**
- `src/main/resources/db/migration/separar_usuarios_personas.sql`
- Migración completa de datos
- Actualización de referencias en todas las tablas

### 5. **Pantalla de Perfil Detallado**
- `src/main/resources/templates/perfil-detalle.html`
- Vista adaptada del proyecto GymInsights
- Muestra información del usuario autenticado
- Diferencia entre deportistas y administrativos

---

## 🔧 Pasos para Ejecutar la Migración

### Paso 1: Ejecutar el Script SQL

```bash
# Conectarse a PostgreSQL
psql -U tu_usuario -d gimnasiodb

# Ejecutar el script de migración
\i src/main/resources/db/migration/separar_usuarios_personas.sql
```

O desde un cliente GUI (pgAdmin, DBeaver):
- Abrir el archivo `separar_usuarios_personas.sql`
- Ejecutar todo el contenido

### Paso 2: Actualizar el Código Java

Los siguientes archivos necesitan ser actualizados (tienen errores de compilación):

#### ❌ Archivos con Errores Actuales:

1. **HomeController.java**
   - Líneas que usan `persona.getTipo()`, `persona.getRol()`, `persona.setTipo()`, `persona.setRol()`
   - Necesita migrar la lógica de gestión de "miembros" (antes personal) a `UsuarioController`

2. **CustomUserDetailsService.java**
   - Líneas que usan `persona.getTipo()` y `persona.getRol()`
   - Necesita consultar ambas tablas (personas y usuarios) para autenticación

3. **PersonaRepository.java**
   - Métodos `findByTipo()`, `countPersonalActivos()`, etc.
   - Eliminar métodos relacionados con "PERSONAL"

---

## 📝 Cambios Requeridos en el Código

### 1. Actualizar HomeController

El método `/miembros` actualmente gestiona "personal administrativo". Este debe:

**OPCIÓN A**: Renombrar la ruta a `/personal` o `/usuarios-admin` y usar `UsuarioRepository`

```java
@GetMapping("/personal")
public String listarPersonal(Model model) {
    List<Usuario> usuarios = usuarioRepository.findAll();
    model.addAttribute("usuarios", usuarios);
    return "personal"; // Crear vista personal.html
}
```

**OPCIÓN B**: Eliminar esta funcionalidad de HomeController y crear un nuevo `UsuarioController`

### 2. Actualizar Custom UserDetailsService

Modificar para buscar en ambas tablas:

```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    // Intentar buscar en usuarios (administrativos)
    Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
    if (usuario.isPresent()) {
        Usuario u = usuario.get();
        return User.builder()
            .username(u.getEmail())
            .password(u.getContraseña())
            .authorities("ROLE_" + u.getRol().getCodigo())
            .accountLocked(!u.getActivo())
            .build();
    }
    
    // Si no se encuentra, buscar en personas (deportistas)
    Persona persona = personaRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    
    return User.builder()
        .username(persona.getEmail())
        .password(persona.getContraseña())
        .authorities("ROLE_DEPORTISTA")
        .accountLocked(!persona.getMembresiaActiva())
        .build();
}
```

### 3. Actualizar PersonaRepository

Eliminar métodos que ya no aplican:

```java
// ❌ ELIMINAR:
List<Persona> findByTipo(String tipo);
long countPersonalActivos();
long countPersonalInactivos();
List<Persona> searchPersonal(String query);

// ✅ MANTENER:
Optional<Persona> findByEmail(String email);
boolean existsByEmail(String email);
// ... otros métodos para deportistas
```

### 4. Actualizar SecurityConfig (si aplica)

Verificar que las rutas estén correctamente configuradas para los nuevos roles:

```java
.requestMatchers("/inicio", "/dashboard").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "ENTRENADOR")
.requestMatchers("/perfil", "/evaluaciones").hasAnyRole("DEPORTISTA", "ADMINISTRADOR", "ENTRENADOR")
// ... etc
```

---

## 🔍 Verificación Post-Migración

### 1. Verificar Datos en la Base de Datos

```sql
-- Verificar usuarios administrativos
SELECT id, nombre, apellido, email, departamento, puesto 
FROM usuarios 
ORDER BY id;

-- Verificar deportistas
SELECT id, nombre, apellido, email, membresia_activa 
FROM personas 
ORDER BY id;

-- Verificar que no haya campos tipo o rol_id en personas
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'personas';
```

### 2. Probar Autenticación

- Login con usuario administrativo (debe tener acceso al dashboard)
- Login con deportista (debe tener acceso limitado)

### 3. Probar Pantallas

- `/inicio` - Dashboard (solo administrativos)
- `/perfil-detalle` - Perfil del usuario autenticado
- `/miembros` - Lista de personal (actualizar a usuarios)

---

## 📊 Estructura de las Tablas

### Tabla `usuarios` (Personal Administrativo)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | SERIAL | PK |
| nombre | VARCHAR(100) | Nombre |
| apellido | VARCHAR(100) | Apellido |
| email | VARCHAR(150) | Email único |
| telefono | VARCHAR(30) | Teléfono |
| dni | VARCHAR(20) | DNI único |
| fecha_registro | DATE | Fecha de registro |
| rol_id | INTEGER | FK a roles (ADMINISTRADOR, RECEPCIONISTA, ENTRENADOR) |
| activo | BOOLEAN | Estado activo/inactivo |
| contraseña | VARCHAR(255) | Contraseña encriptada |
| departamento | VARCHAR(200) | Departamento |
| puesto | VARCHAR(100) | Cargo/Puesto |
| fecha_inicio_laboral | DATE | Fecha de inicio |
| horario | VARCHAR(150) | Horario de trabajo |

### Tabla `personas` (Deportistas)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | SERIAL | PK |
| nombre | VARCHAR(100) | Nombre |
| apellido | VARCHAR(100) | Apellido |
| email | VARCHAR(150) | Email único |
| telefono | VARCHAR(30) | Teléfono |
| dni | VARCHAR(20) | DNI único |
| fecha_registro | DATE | Fecha de registro |
| fecha_nacimiento | DATE | Fecha de nacimiento |
| membresia_activa | BOOLEAN | Estado de membresía |
| contraseña | VARCHAR(255) | Contraseña encriptada |
| genero | VARCHAR(10) | Género (M, F, Otro) |
| direccion | VARCHAR(200) | Dirección |
| emergencia_contacto | VARCHAR(200) | Contacto de emergencia |
| emergencia_telefono | VARCHAR(30) | Teléfono de emergencia |

---

## ⚠️ Notas Importantes

1. **Backup**: El script crea automáticamente `personas_backup` con el personal antes de migrar

2. **Contraseñas de Prueba**:
   - Admin: `admin@fitgym.com` / `admin123`
   - Cliente: `cliente@email.com` / `cliente123`

3. **Referencias Actualizadas**: El script actualiza automáticamente las FK en:
   - clases (entrenador_id)
   - evaluaciones (evaluador_id)
   - entrenamientos (creado_por)
   - incidencias (reportado_por)
   - reclamos (administrador_respuesta_id)

4. **Triggers**: Se crean triggers para actualizar `updated_at` automáticamente

---

## 🚀 Próximos Pasos Recomendados

1. ✅ Ejecutar script SQL de migración
2. ✅ Corregir errores de compilación en HomeController
3. ✅ Actualizar CustomUserDetailsService
4. ✅ Crear UsuarioController para gestión de personal
5. ✅ Actualizar SecurityConfig si es necesario
6. ✅ Probar autenticación con ambos tipos de usuarios
7. ✅ Actualizar vistas HTML que muestren información de roles

---

## 📞 Soporte

Si encuentras problemas durante la migración:
1. Verifica que el script SQL se ejecutó completamente
2. Revisa los logs de la aplicación
3. Verifica las constraints de FK en la base de datos
4. Asegúrate de que todos los repositories están actualizados

---

**Fecha de Creación**: 26 de octubre de 2024  
**Versión**: 1.0
