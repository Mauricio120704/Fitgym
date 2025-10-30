# Implementación de Verificación de Email para Deportistas

## 📋 Descripción General

Este documento describe la implementación completa del sistema de verificación de email para usuarios deportistas en el sistema de gestión de gimnasios FitGym.

## 🎯 Objetivo

Asegurar que solo los usuarios con correos electrónicos válidos puedan activar su cuenta y acceder al sistema, previniendo registros fraudulentos y mejorando la seguridad.

## ✨ Características Implementadas

- ✅ Envío automático de correo de verificación al registrarse
- ✅ Tokens UUID únicos con expiración de 24 horas
- ✅ Cuentas inactivas hasta verificar el email (`activo = false`)
- ✅ Validación de tokens con mensajes descriptivos
- ✅ Posibilidad de reenviar email de verificación
- ✅ Integración con Spring Security para bloquear login de cuentas no verificadas
- ✅ Interfaz de usuario amigable con Tailwind CSS

## 📦 Componentes Agregados

### 1. **Modelo de Datos**

#### Modificaciones en `Persona.java`
```java
@Column(name = "activo")
private Boolean activo = Boolean.FALSE; // Inactivo hasta verificar email

@Column(name = "email_verificado")
private Boolean emailVerificado = Boolean.FALSE;
```

#### Nueva Entidad: `VerificationToken.java`
- Almacena tokens UUID con fecha de creación y expiración
- Relación `@OneToOne` con `Persona`
- Validación automática de expiración (24 horas)

### 2. **Repositorio**
- `VerificationTokenRepository.java`: Gestiona tokens de verificación

### 3. **Servicios**

#### `EmailService.java`
- Envío de correos HTML con plantilla profesional
- Configuración SMTP con JavaMail
- Soporte para Gmail y Mailtrap

#### `VerificationTokenService.java`
- Creación de tokens y envío de emails
- Verificación de tokens con validaciones
- Reenvío de emails de verificación

### 4. **Controladores**

#### Modificaciones en `HomeController.java`
- Integración del servicio de verificación en el registro
- Redireccionamiento a página de confirmación en lugar de auto-login
- Cuentas creadas como inactivas por defecto

#### Nuevo: `EmailVerificationController.java`
- Endpoint `/verificar-email?token=...` para verificación
- Endpoint `/reenviar-verificacion` para reenvío de emails

### 5. **Seguridad**

#### Modificaciones en `CustomUserDetailsService.java`
- Validación de campo `activo` para deportistas
- Bloqueo de login (`accountLocked=true`) si cuenta no verificada

#### Modificaciones en `SecurityConfig.java`
- Rutas públicas: `/verificar-email`, `/reenviar-verificacion`

### 6. **Vistas HTML**
- `registro-exitoso.html`: Confirmación post-registro con instrucciones
- `verificacion-resultado.html`: Resultado de la verificación (éxito/error)
- `reenviar-verificacion.html`: Formulario para reenviar verificación

## 🗄️ Migración de Base de Datos

### Script SQL para PostgreSQL

```sql
-- 1. Agregar columnas a la tabla personas
ALTER TABLE personas 
ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS email_verificado BOOLEAN DEFAULT FALSE;

-- 2. Crear tabla verification_tokens
CREATE TABLE IF NOT EXISTS verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    persona_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_verification_token_persona 
        FOREIGN KEY (persona_id) 
        REFERENCES personas(id) 
        ON DELETE CASCADE
);

-- 3. Crear índice para búsqueda rápida por token
CREATE INDEX IF NOT EXISTS idx_verification_token 
ON verification_tokens(token);

-- 4. Crear índice para búsqueda por persona
CREATE INDEX IF NOT EXISTS idx_verification_persona 
ON verification_tokens(persona_id);

-- 5. (OPCIONAL) Activar cuentas existentes si ya están en uso
-- Solo ejecutar si quieres que las cuentas actuales sigan activas
UPDATE personas 
SET activo = TRUE, 
    email_verificado = TRUE 
WHERE activo IS NULL OR activo = FALSE;

-- 6. Comentar columnas para documentación
COMMENT ON COLUMN personas.activo IS 'Indica si la cuenta está activa (email verificado)';
COMMENT ON COLUMN personas.email_verificado IS 'Indica si el email fue verificado';
COMMENT ON TABLE verification_tokens IS 'Tokens de verificación de email con expiración de 24h';
```

### Aplicar la Migración

```bash
# Opción 1: Desde línea de comandos de PostgreSQL
psql -U postgres -d gimnasiodbM -f migration_email_verification.sql

# Opción 2: Desde PgAdmin o DBeaver
# Ejecutar el script SQL anterior en una nueva consulta
```

## ⚙️ Configuración Requerida

### 1. Configurar SMTP en `application.properties`

El archivo ya contiene la configuración base. Debes actualizar con tus credenciales:

#### Para Gmail (Producción):
```properties
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password
```

**Pasos para obtener "Contraseña de Aplicación" de Gmail:**
1. Ve a https://myaccount.google.com/security
2. Activa "Verificación en dos pasos"
3. Ve a https://myaccount.google.com/apppasswords
4. Genera una contraseña para "Correo"
5. Usa esa contraseña en `spring.mail.password`

#### Para Mailtrap (Desarrollo/Testing):
```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=tu-username-mailtrap
spring.mail.password=tu-password-mailtrap
```

### 2. Configurar URL Base

En `application.properties`:
```properties
app.base-url=http://localhost:8080
```

**En producción**, cambiar a la URL real:
```properties
app.base-url=https://tudominio.com
```

## 🚀 Flujo de Uso

### Flujo de Registro Nuevo
1. Usuario completa formulario de registro en `/registro`
2. Sistema crea cuenta con `activo=false` y `email_verificado=false`
3. Sistema genera token UUID y lo almacena en `verification_tokens`
4. Sistema envía correo con enlace: `http://localhost:8080/verificar-email?token={uuid}`
5. Usuario recibe correo y hace clic en el enlace
6. Sistema valida token y activa cuenta (`activo=true`, `email_verificado=true`)
7. Usuario puede iniciar sesión

### Flujo de Reenvío de Verificación
1. Usuario accede a `/reenviar-verificacion`
2. Ingresa su email
3. Sistema elimina token anterior (si existe)
4. Sistema genera nuevo token y envía email
5. Usuario recibe nuevo correo con enlace válido

### Bloqueo de Login
- Si el usuario intenta hacer login sin verificar su email:
  - Spring Security detecta `accountLocked=true`
  - No permite el acceso aunque la contraseña sea correcta
  - Usuario debe verificar su email primero

## 🧪 Pruebas

### 1. Probar Registro
```bash
# Iniciar aplicación
mvn spring-boot:run

# Ir a http://localhost:8080/registro
# Completar formulario con email real
# Verificar que llegue el correo
```

### 2. Probar Verificación
```bash
# Hacer clic en el enlace del correo
# Verificar redirección a página de éxito
# Intentar hacer login - debe funcionar
```

### 3. Probar Token Expirado
```bash
# En base de datos, actualizar fecha_expiracion a una fecha pasada:
UPDATE verification_tokens 
SET fecha_expiracion = NOW() - INTERVAL '1 day' 
WHERE token = 'tu-token-uuid';

# Intentar usar el enlace - debe mostrar error de expiración
```

### 4. Probar Reenvío
```bash
# Ir a http://localhost:8080/reenviar-verificacion
# Ingresar email registrado
# Verificar que llegue nuevo correo
```

## 📧 Ejemplo de Email Enviado

El correo incluye:
- Logo y branding de FitGym
- Saludo personalizado con nombre del usuario
- Botón grande de "Verificar mi cuenta"
- Enlace de texto como alternativa
- Advertencia de expiración en 24 horas
- Diseño responsive y profesional

## 🔒 Seguridad

### Medidas Implementadas
- Tokens UUID únicos y aleatorios
- Expiración automática después de 24 horas
- Tokens de un solo uso (marcados como `usado=true`)
- Validación en servidor antes de activar cuenta
- Relación cascada para eliminar tokens al eliminar usuario
- Encriptación de contraseñas con BCrypt

### Prevención de Ataques
- **Token Reuse**: Los tokens se marcan como usados
- **Token Guessing**: UUID aleatorios (128 bits de entropía)
- **Brute Force**: Expiración temporal limita intentos
- **SQL Injection**: Uso de JPA y consultas parametrizadas

## 🐛 Solución de Problemas

### Error: "Connection refused" al enviar email
**Causa**: Configuración SMTP incorrecta o firewall bloqueando puerto 587
**Solución**: 
- Verificar credenciales en `application.properties`
- Probar con Mailtrap primero
- Verificar que firewall permita conexiones salientes al puerto 587

### Error: "AuthenticationFailedException"
**Causa**: Contraseña incorrecta para Gmail
**Solución**: 
- Usar "Contraseña de Aplicación" de Gmail, no la contraseña normal
- Verificar que verificación en dos pasos esté activa

### Email no llega a bandeja de entrada
**Causa**: Puede estar en carpeta de spam
**Solución**:
- Revisar carpeta de spam/correo no deseado
- Configurar remitente como contacto de confianza
- Verificar que email `from` sea válido

### Token inválido aunque acabo de registrarme
**Causa**: Diferencia de zona horaria o clock skew
**Solución**: 
- Verificar que hora del servidor esté sincronizada
- Revisar configuración de zona horaria en PostgreSQL

### Usuario no puede hacer login después de verificar
**Causa**: Campo `activo` no se actualizó correctamente
**Solución**:
```sql
-- Verificar estado en base de datos
SELECT id, email, activo, email_verificado FROM personas WHERE email = 'usuario@ejemplo.com';

-- Activar manualmente si es necesario
UPDATE personas SET activo = TRUE, email_verificado = TRUE WHERE email = 'usuario@ejemplo.com';
```

## 📝 Notas Adicionales

### Sobre `authenticationService`
- El campo `authenticationService` en `HomeController` actualmente no se usa
- Se mantiene para compatibilidad con código existente
- Si el flujo de auto-login se necesita en el futuro, ya está disponible

### Logging
- Los logs de Spring Security están en modo TRACE
- Puedes ver detalles de autenticación en consola
- Para producción, reducir nivel a INFO o WARN

### Hibernate DDL
- `spring.jpa.hibernate.ddl-auto=none` está configurado
- Las tablas deben crearse manualmente con el script SQL
- Esto previene cambios accidentales en base de datos

## 🎉 Resultado Final

Después de esta implementación, el sistema cuenta con:
- ✅ Registro seguro con verificación de email obligatoria
- ✅ Protección contra registros fraudulentos
- ✅ Experiencia de usuario profesional y clara
- ✅ Sistema de tokens robusto con expiración
- ✅ Posibilidad de reenvío de verificación
- ✅ Integración completa con Spring Security

## 📚 Referencias

- [Spring Boot Mail Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- [Spring Security Account Locking](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html)
- [JavaMail API](https://javaee.github.io/javamail/)
- [Thymeleaf Templates](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html)

---

**Desarrollado para FitGym - Sistema de Gestión de Gimnasios**
