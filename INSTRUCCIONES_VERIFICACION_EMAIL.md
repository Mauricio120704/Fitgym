# 🚀 Instrucciones Rápidas: Configuración de Verificación de Email

## ⚡ Inicio Rápido (5 minutos)

### Paso 1: Ejecutar Migración de Base de Datos

```bash
# Opción A: Desde línea de comandos
psql -U postgres -d gimnasiodbM -f src/main/resources/db/migration_email_verification.sql

# Opción B: Desde PgAdmin/DBeaver
# Abrir el archivo src/main/resources/db/migration_email_verification.sql
# Ejecutar todo el script
```

**⚠️ Importante:** Decide si activar cuentas existentes o no:
- **Para desarrollo:** Descomenta la línea del UPDATE en el script SQL para activar cuentas existentes
- **Para producción nueva:** Deja como está, todas las cuentas necesitarán verificación

### Paso 2: Configurar Servicio de Email

Edita `src/main/resources/application.properties`:

#### Opción A: Gmail (Recomendado para producción)

1. **Obtén una Contraseña de Aplicación de Gmail:**
   - Ve a: https://myaccount.google.com/security
   - Activa "Verificación en dos pasos"
   - Ve a: https://myaccount.google.com/apppasswords
   - Genera una contraseña para "Correo"

2. **Configura en application.properties:**
```properties
spring.mail.username=tu-email@gmail.com
spring.mail.password=xxxx-xxxx-xxxx-xxxx  # Contraseña de aplicación de 16 dígitos
```

#### Opción B: Mailtrap (Recomendado para desarrollo/testing)

1. **Crea cuenta gratuita:** https://mailtrap.io/
2. **Obtén credenciales** de tu inbox de prueba
3. **Comenta la configuración de Gmail** y descomenta Mailtrap:

```properties
# Comentar Gmail
# spring.mail.host=smtp.gmail.com
# spring.mail.port=587
# spring.mail.username=tu-email@gmail.com
# spring.mail.password=tu-app-password

# Descomentar Mailtrap
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=tu-username-mailtrap
spring.mail.password=tu-password-mailtrap
```

### Paso 3: Configurar URL Base

En `application.properties`:

```properties
# Para desarrollo local
app.base-url=http://localhost:8080

# Para producción (cambiar cuando deploys)
# app.base-url=https://tudominio.com
```

### Paso 4: Ejecutar la Aplicación

```bash
# Con Maven
mvn spring-boot:run

# O ejecutar desde tu IDE
# Run Gimnasiov1Application.java
```

### Paso 5: Probar el Sistema

1. **Ir a:** http://localhost:8080/registro
2. **Completar formulario** con un email real (si usas Gmail) o cualquier email (si usas Mailtrap)
3. **Revisar email recibido:**
   - Gmail: Revisa tu bandeja de entrada
   - Mailtrap: Ve a tu inbox en mailtrap.io
4. **Hacer clic en el enlace** de verificación
5. **Iniciar sesión** con las credenciales creadas

## 📋 Checklist de Configuración

- [ ] Migración SQL ejecutada
- [ ] Credenciales SMTP configuradas en application.properties
- [ ] URL base configurada
- [ ] Aplicación iniciada sin errores
- [ ] Registro de prueba completado
- [ ] Email recibido
- [ ] Verificación exitosa
- [ ] Login funciona después de verificar

## 🐛 Problemas Comunes

### "AuthenticationFailedException" al enviar email
**Solución:** Usa la contraseña de aplicación de Gmail, no tu contraseña normal

### Email no llega
**Solución:** 
- Gmail: Revisa carpeta de spam
- Mailtrap: Ve a mailtrap.io y revisa el inbox virtual

### "Connection refused"
**Solución:** 
- Verifica que el puerto 587 no esté bloqueado por firewall
- Intenta primero con Mailtrap para descartar problemas de red

### No puedo hacer login después de registrarme
**Esto es normal:** Debes verificar tu email primero haciendo clic en el enlace que recibiste

### El enlace de verificación dice "Token inválido"
**Posibles causas:**
- El token expiró (válido por 24 horas)
- Ya usaste ese enlace antes
- Hay un error en la URL del enlace

**Solución:** Ve a http://localhost:8080/reenviar-verificacion y solicita un nuevo enlace

## 🔧 Configuración Avanzada

### Cambiar tiempo de expiración de tokens

Edita `VerificationToken.java` línea 40:
```java
// De 24 horas a X horas
this.fechaExpiracion = LocalDateTime.now().plusHours(X);
```

### Personalizar plantilla de email

Edita el método `construirEmailVerificacion` en `EmailService.java`

### Cambiar URL de redireccionamiento después de verificar

Edita `verificacion-resultado.html` y cambia el botón "Iniciar Sesión"

## 📊 Monitoreo

### Ver tokens activos en base de datos:
```sql
SELECT p.email, vt.token, vt.fecha_creacion, vt.fecha_expiracion, vt.usado
FROM verification_tokens vt
JOIN personas p ON vt.persona_id = p.id
WHERE vt.usado = FALSE
ORDER BY vt.fecha_creacion DESC;
```

### Ver estado de cuentas:
```sql
SELECT email, activo, email_verificado, fecha_registro
FROM personas
ORDER BY fecha_registro DESC
LIMIT 20;
```

### Limpiar tokens expirados:
```sql
DELETE FROM verification_tokens
WHERE fecha_expiracion < NOW();
```

## 📞 Soporte

Si tienes problemas:
1. Revisa los logs de la aplicación en consola
2. Consulta `VERIFICACION_EMAIL_IMPLEMENTATION.md` para detalles completos
3. Verifica que todas las dependencias estén instaladas (`mvn clean install`)

## ✅ ¡Listo!

Tu sistema ahora tiene verificación de email completa y funcional. Los nuevos deportistas deberán verificar su correo antes de poder acceder al sistema.

---

**Nota sobre Seguridad:** En producción, asegúrate de:
- Usar variables de entorno para credenciales SMTP
- Configurar SSL/TLS correctamente
- Cambiar `app.base-url` a tu dominio real
- Reducir nivel de logging de TRACE a INFO
