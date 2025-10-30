# Integración de Spring Security - Gimnasio FitGym

## ✅ Cambios Implementados

### 1. Servicios de Seguridad Creados

#### `CustomUserDetailsService.java`
- Implementa `UserDetailsService` de Spring Security
- Carga usuarios desde la base de datos por email
- Asigna roles automáticamente:
  - `ROLE_CLIENTE` para deportistas
  - `ROLE_ADMINISTRADOR`, `ROLE_RECEPCIONISTA`, `ROLE_ENTRENADOR` para personal

#### `AuthenticationService.java`
- Servicio para auto-login después del registro
- Permite que el usuario inicie sesión automáticamente tras registrarse

### 2. Configuración de Seguridad (`SecurityConfig.java`)

#### Rutas Públicas (sin autenticación):
- `/`, `/inicio` - Página principal
- `/login` - Formulario de login
- `/registro` - Formulario de registro
- `/planes` - Vista de planes
- `/checkout/**` - Proceso de pago
- `/Estilos/**`, `/JavaScript/**`, `/images/**` - Recursos estáticos

#### Rutas Protegidas para CLIENTES (deportistas):
- `/perfil` - Perfil del usuario
- `/cliente/**` - Rutas específicas de clientes
- `/entrenamientos` - Entrenamientos
- `/evaluaciones` - Evaluaciones

#### Rutas Protegidas para PERSONAL:
- `/miembros/**` - Gestión de miembros
- `/incidencias/**` - Gestión de incidencias
- `/pagos` - Historial de pagos
- `/clases/**` - Gestión de clases
- `/configuracion` - Configuración del sistema

### 3. Actualización del Controlador (`HomeController.java`)

#### Cambios Realizados:
- ✅ Eliminado método `procesarLogin` (Spring Security lo maneja)
- ✅ Actualizado método `/perfil` para usar `@AuthenticationPrincipal`
- ✅ Actualizado método `/registro` para auto-login después del registro
- ✅ Actualizado método `/login` para mostrar mensajes de error/logout

### 4. Actualización de `PasswordEncoderService.java`
- Ahora inyecta el `PasswordEncoder` bean de `SecurityConfig`
- Usa BCrypt para encriptar contraseñas

## 🔧 Configuración de Base de Datos

### Asegúrate de tener los roles en la tabla `roles`:

```sql
INSERT INTO roles (codigo, nombre) VALUES 
('DEPORTISTA', 'Deportista'),
('ADMINISTRADOR', 'Administrador'),
('RECEPCIONISTA', 'Recepcionista'),
('ENTRENADOR', 'Entrenador')
ON CONFLICT (codigo) DO NOTHING;
```

## 🚀 Flujo de Usuario Actualizado

### Registro de Deportista:
1. Usuario accede a `/registro`
2. Completa el formulario con sus datos
3. Al hacer clic en "Registrarse":
   - Se crea el usuario con tipo `DEPORTISTA`
   - La contraseña se encripta con BCrypt
   - Se hace auto-login automáticamente
   - Se redirige a `/planes` para seleccionar un plan

### Login:
1. Usuario accede a `/login`
2. Ingresa email y contraseña
3. Spring Security valida las credenciales
4. Si es correcto:
   - **Deportista (ROLE_CLIENTE)**: redirige a `/perfil`
   - **Personal**: redirige a `/miembros`
5. Si es incorrecto: muestra mensaje de error

### Acceso a Rutas Protegidas:
- Si un usuario no autenticado intenta acceder a una ruta protegida, se redirige a `/login`
- Si un usuario autenticado no tiene el rol necesario, recibe error 403 (Forbidden)

## 📝 Notas Importantes

### CSRF (Cross-Site Request Forgery):
Actualmente está **deshabilitado** para facilitar las pruebas:
```java
.csrf(csrf -> csrf.disable())
```

**Para producción**, debes habilitarlo y agregar el token CSRF en tus formularios:
```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

### Logout:
Para cerrar sesión, el usuario puede acceder a `/logout` o crear un botón:
```html
<form th:action="@{/logout}" method="post">
    <button type="submit">Cerrar Sesión</button>
</form>
```

### Obtener Usuario Actual en Controladores:
```java
@GetMapping("/ruta")
public String metodo(@AuthenticationPrincipal UserDetails userDetails) {
    String email = userDetails.getUsername();
    // ...
}
```

### Obtener Usuario Actual en Thymeleaf:
```html
<div th:if="${#authentication.principal != 'anonymousUser'}">
    <p>Bienvenido, <span th:text="${#authentication.principal.username}"></span></p>
</div>
```

## ✅ Verificación de la Integración

### 1. Probar Registro:
- Ir a `/registro`
- Completar formulario
- Verificar que se crea el usuario y se redirige a `/planes`
- Verificar que el usuario está autenticado

### 2. Probar Login:
- Ir a `/login`
- Ingresar credenciales correctas
- Verificar redirección según rol

### 3. Probar Protección de Rutas:
- Sin autenticación, intentar acceder a `/perfil` → debe redirigir a `/login`
- Con usuario deportista, intentar acceder a `/miembros` → debe dar error 403

### 4. Probar Logout:
- Acceder a `/logout`
- Verificar que se cierra la sesión
- Verificar redirección a `/login?logout=true`

## 🎯 Próximos Pasos Recomendados

1. **Habilitar CSRF** en producción
2. **Implementar "Recordarme"** (Remember Me)
3. **Agregar recuperación de contraseña**
4. **Implementar roles más granulares** si es necesario
5. **Agregar auditoría** de accesos
6. **Configurar sesiones** (timeout, concurrent sessions)

## 🔐 Seguridad Adicional

### Configurar Timeout de Sesión:
En `application.properties`:
```properties
server.servlet.session.timeout=30m
```

### Limitar Sesiones Concurrentes:
En `SecurityConfig`:
```java
.sessionManagement(session -> session
    .maximumSessions(1)
    .maxSessionsPreventsLogin(true)
)
```

## 📞 Soporte

Si encuentras algún problema:
1. Verifica los logs de la aplicación
2. Asegúrate de que los roles existen en la base de datos
3. Verifica que las contraseñas estén encriptadas con BCrypt
4. Revisa que los nombres de los campos del formulario coincidan con la configuración

---

**¡La integración de Spring Security está completa y lista para usar!** 🎉
