# Modificaciones: Rol Deportista y Sidebar Dinámico

## ✅ Cambios Implementados

### 1. Asignación Automática de Rol en el Registro

#### Modificación en `HomeController.java`
**Ubicación**: Método `registrarDeportista()`

**Cambio Realizado**:
```java
// Asignar rol de DEPORTISTA (rol_id = 4)
Role rolDeportista = roleRepository.findById(4)
    .orElseThrow(() -> new RuntimeException("Rol DEPORTISTA no encontrado en la base de datos"));
p.setRol(rolDeportista);
```

**Resultado**:
- ✅ Al registrarse, cada nuevo usuario deportista recibe automáticamente `rol_id = 4`
- ✅ El sistema valida que el rol exista en la base de datos antes de asignar
- ✅ Si el rol no existe, lanza una excepción clara

---

### 2. Actualización del Script SQL

#### Modificación en `init_roles.sql`
**Cambio Realizado**:
```sql
-- Insertar roles del sistema con IDs específicos
-- IMPORTANTE: El rol DEPORTISTA debe tener id = 4
INSERT INTO roles (id, codigo, nombre) VALUES 
(1, 'ADMINISTRADOR', 'Administrador'),
(2, 'RECEPCIONISTA', 'Recepcionista'),
(3, 'ENTRENADOR', 'Entrenador'),
(4, 'DEPORTISTA', 'Deportista')
ON CONFLICT (id) DO NOTHING;
```

**Resultado**:
- ✅ Garantiza que el rol DEPORTISTA tenga siempre el ID = 4
- ✅ Los roles se insertan con IDs fijos para evitar inconsistencias

---

### 3. Actualización de `CustomUserDetailsService`

#### Modificación en `CustomUserDetailsService.java`
**Cambio Realizado**:
```java
// Asignar rol basado en el rol de la base de datos
if (persona.getRol() != null) {
    String rolCodigo = persona.getRol().getCodigo();
    // Para deportistas, usar ROLE_CLIENTE (compatibilidad con la configuración de seguridad)
    if ("DEPORTISTA".equals(rolCodigo)) {
        authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    } else {
        // Para personal, usar el rol específico
        authorities.add(new SimpleGrantedAuthority("ROLE_" + rolCodigo));
    }
}
```

**Resultado**:
- ✅ Los deportistas ahora obtienen su rol desde la base de datos
- ✅ Se mantiene la compatibilidad con `ROLE_CLIENTE` para Spring Security
- ✅ Fallback implementado si no hay rol en BD

---

### 4. Sidebar Dinámico con Fragmentos Thymeleaf

#### Nuevo Archivo: `fragments/sidebar.html`

**Características**:
- ✅ **Sidebar para DEPORTISTAS** (`ROLE_CLIENTE`):
  - Mi Perfil
  - Evaluaciones
  - Historial de Pagos
  - Cerrar Sesión

- ✅ **Sidebar para PERSONAL** (Administradores, Recepcionistas, Entrenadores):
  - Inicio
  - Registrar Usuario
  - Personal
  - Incidencias
  - Historial de Pagos
  - Cupos de Clases
  - Evaluaciones
  - Entrenamientos
  - Mi Perfil
  - Cerrar Sesión

**Tecnología Utilizada**:
```html
<div sec:authorize="hasRole('CLIENTE')">
    <!-- Menú para deportistas -->
</div>

<div sec:authorize="hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'ENTRENADOR')">
    <!-- Menú para personal -->
</div>
```

**Ventajas**:
- ✅ Un solo archivo para mantener
- ✅ Lógica centralizada
- ✅ Fácil de actualizar
- ✅ Reutilizable en todas las vistas

---

### 5. Actualización de `perfil-usuario.html`

#### Cambio Realizado:
```html
<!-- Antes: Sidebar estático con todas las opciones -->
<aside id="sidebar">...</aside>

<!-- Después: Sidebar dinámico usando fragmento -->
<div th:replace="~{fragments/sidebar :: sidebar('perfil')}"></div>
```

**Resultado**:
- ✅ El sidebar ahora se adapta automáticamente según el rol del usuario
- ✅ Los deportistas solo ven las 3 opciones relevantes
- ✅ El personal ve todas las opciones de gestión

---

### 6. Nuevo Controlador para Clientes

#### Nuevo Archivo: `ClienteController.java`

**Funcionalidad**:
```java
@Controller
@RequestMapping("/cliente")
public class ClienteController {
    
    @GetMapping("/pagos")
    public String historialPagos(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Obtiene el usuario autenticado
        // Muestra su historial de pagos
        return "cliente-pagos";
    }
}
```

**Resultado**:
- ✅ Ruta específica para clientes: `/cliente/pagos`
- ✅ Protegida por Spring Security (solo `ROLE_CLIENTE`)
- ✅ Obtiene automáticamente el usuario autenticado

---

### 7. Nueva Vista: `cliente-pagos.html`

**Características**:
- ✅ Usa el sidebar dinámico
- ✅ Muestra el historial de pagos del cliente
- ✅ Diseño consistente con el resto del sistema

---

## 🔧 Configuración Requerida

### 1. Ejecutar Script SQL
```bash
# Ejecutar en PostgreSQL
psql -U tu_usuario -d tu_base_de_datos -f src/main/resources/db/init_roles.sql
```

### 2. Verificar Dependencias en `pom.xml`
```xml
<!-- Ya incluida -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

---

## 🎯 Flujo Completo del Usuario Deportista

### Registro:
1. Usuario accede a `/registro`
2. Completa el formulario
3. **Sistema asigna automáticamente `rol_id = 4` (DEPORTISTA)**
4. Auto-login
5. Redirige a `/planes`

### Login:
1. Usuario accede a `/login`
2. Ingresa credenciales
3. Spring Security valida
4. **Sistema asigna `ROLE_CLIENTE` basado en `rol_id = 4`**
5. Redirige a `/perfil`

### Navegación:
1. Usuario ve **solo 3 opciones** en el sidebar:
   - Mi Perfil
   - Evaluaciones
   - Historial de Pagos
2. No puede acceder a rutas de personal (protegidas por Spring Security)
3. Experiencia limpia y enfocada

---

## 📝 Aplicar el Sidebar a Otras Vistas

Para aplicar el sidebar dinámico a otras vistas HTML:

### 1. Agregar namespace de Spring Security:
```html
<html lang="es" xmlns:th="http://www.thymeleaf.org" 
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
```

### 2. Reemplazar el sidebar estático:
```html
<!-- Eliminar todo el <aside id="sidebar">...</aside> -->

<!-- Agregar el fragmento -->
<div th:replace="~{fragments/sidebar :: sidebar('nombre-menu-activo')}"></div>
```

### 3. Especificar el menú activo:
- `'perfil'` - Para Mi Perfil
- `'evaluaciones'` - Para Evaluaciones
- `'pagos'` - Para Historial de Pagos
- `'miembros'` - Para Personal
- `'incidencias'` - Para Incidencias
- etc.

### Ejemplo para `evaluaciones.html`:
```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org" 
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <!-- ... -->
</head>
<body>
    <div class="flex h-screen bg-gray-200">
        <!-- Sidebar dinámico -->
        <div th:replace="~{fragments/sidebar :: sidebar('evaluaciones')}"></div>
        
        <!-- Resto del contenido -->
        <div class="flex-1 flex flex-col overflow-hidden">
            <!-- ... -->
        </div>
    </div>
</body>
</html>
```

---

## ✅ Verificación de Funcionamiento

### 1. Probar Registro de Deportista:
```sql
-- Verificar que el usuario tiene rol_id = 4
SELECT id, nombre, email, tipo, rol_id FROM personas WHERE tipo = 'DEPORTISTA';
```

### 2. Probar Sidebar Dinámico:
- **Como Deportista**: Login → Verificar que solo se ven 3 opciones
- **Como Personal**: Login → Verificar que se ven todas las opciones

### 3. Probar Protección de Rutas:
- Como deportista, intentar acceder a `/miembros` → Error 403
- Como deportista, acceder a `/perfil` → ✅ Permitido
- Como deportista, acceder a `/cliente/pagos` → ✅ Permitido

---

## 🎨 Personalización del Sidebar

### Agregar Nueva Opción para Deportistas:
Editar `fragments/sidebar.html`:
```html
<div sec:authorize="hasRole('CLIENTE')">
    <ul class="mt-2">
        <!-- Opciones existentes -->
        
        <!-- Nueva opción -->
        <li>
            <a th:href="@{/nueva-ruta}" 
               th:classappend="${activeMenu == 'nueva'} ? 'text-blue-600 bg-blue-100 font-semibold' : 'text-gray-700 hover:bg-gray-200'"
               class="flex items-center justify-center py-3" 
               title="Nueva Opción">
                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <!-- Icono SVG -->
                </svg>
                <span class="sidebar-text ml-3 hidden">Nueva Opción</span>
            </a>
        </li>
    </ul>
</div>
```

---

## 🔐 Seguridad Implementada

### Nivel de Vista (Thymeleaf):
```html
sec:authorize="hasRole('CLIENTE')"
```
- ✅ Oculta opciones según el rol
- ✅ Mejora la experiencia de usuario

### Nivel de Controlador (Spring Security):
```java
@PreAuthorize("hasRole('CLIENTE')")
```
- ✅ Protege las rutas en el backend
- ✅ Previene acceso no autorizado

### Nivel de Configuración (SecurityConfig):
```java
.requestMatchers("/cliente/**").hasRole("CLIENTE")
```
- ✅ Protección a nivel de aplicación
- ✅ Configuración centralizada

---

## 📊 Resumen de Archivos Modificados

### Archivos Creados:
1. ✅ `fragments/sidebar.html` - Sidebar reutilizable
2. ✅ `ClienteController.java` - Controlador para clientes
3. ✅ `cliente-pagos.html` - Vista de historial de pagos

### Archivos Modificados:
1. ✅ `HomeController.java` - Asignación de rol en registro
2. ✅ `CustomUserDetailsService.java` - Carga de rol desde BD
3. ✅ `init_roles.sql` - IDs fijos para roles
4. ✅ `perfil-usuario.html` - Uso de sidebar dinámico

---

## 🎉 Resultado Final

### Para Deportistas:
- ✅ Registro automático con `rol_id = 4`
- ✅ Sidebar limpio con solo 3 opciones relevantes
- ✅ Acceso solo a sus funcionalidades
- ✅ Experiencia de usuario optimizada

### Para el Sistema:
- ✅ Código reutilizable y mantenible
- ✅ Lógica centralizada en un solo archivo
- ✅ Fácil de extender y modificar
- ✅ Seguridad implementada en múltiples niveles

---

**¡Las modificaciones están completas y listas para usar!** 🚀
