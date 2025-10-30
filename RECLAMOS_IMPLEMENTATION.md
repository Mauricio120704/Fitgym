# Implementación del Sistema de Reclamos y Quejas

## Resumen de la Implementación

Se ha implementado completamente la funcionalidad de "Reclamos y Quejas" para usuarios deportistas, permitiendo que registren sus inquietudes desde la interfaz web y se almacenen en la base de datos.

---

## Componentes Creados

### Backend

#### 1. **Modelo de Datos** (`Reclamo.java`)
- **Ubicación:** `src/main/java/com/integradorii/gimnasiov1/model/Reclamo.java`
- **Características:**
  - Entidad JPA mapeada a la tabla `reclamos`
  - Relación ManyToOne con `Persona` (deportista y atendidoPor)
  - Campos: id, deportista_id, categoria, asunto, descripcion, estado, fecha_creacion, fecha_actualizacion, prioridad, respuesta_admin, atendido_por, activo
  - Anotaciones `@PrePersist` y `@PreUpdate` para manejo automático de fechas
  - Estado por defecto: "En proceso"
  - Prioridad por defecto: "Normal"

#### 2. **Repositorio** (`ReclamoRepository.java`)
- **Ubicación:** `src/main/java/com/integradorii/gimnasiov1/repository/ReclamoRepository.java`
- **Métodos:**
  - `findByDeportistaAndActivoTrueOrderByFechaCreacionDesc()` - Obtener reclamos de un deportista
  - `findByEstadoAndActivoTrueOrderByFechaCreacionDesc()` - Filtrar por estado
  - `findByActivoTrueOrderByFechaCreacionDesc()` - Todos los reclamos activos
  - `countByDeportistaAndActivoTrue()` - Contar reclamos por deportista

#### 3. **Servicio** (`ReclamoService.java`)
- **Ubicación:** `src/main/java/com/integradorii/gimnasiov1/service/ReclamoService.java`
- **Funcionalidades:**
  - `crearReclamo()` - Crear y validar nuevo reclamo
  - `obtenerReclamosPorDeportista()` - Listar reclamos del usuario
  - `obtenerTodosLosReclamos()` - Listar todos los reclamos
  - `obtenerReclamosPorEstado()` - Filtrar por estado
  - `contarReclamosPorDeportista()` - Estadísticas
  - Validaciones de campos obligatorios

#### 4. **Controlador** (`ReclamoController.java`)
- **Ubicación:** `src/main/java/com/integradorii/gimnasiov1/controller/ReclamoController.java`
- **Endpoints:**
  - `GET /reclamos` - Vista principal con formulario y lista de reclamos
  - `POST /reclamos/crear` - Crear nuevo reclamo (retorna JSON)
  - `GET /reclamos/mis-reclamos` - API para obtener reclamos del usuario
- **Seguridad:** Obtiene automáticamente el deportista desde la sesión de Spring Security

#### 5. **DTO** (`ReclamoDTO.java`)
- **Ubicación:** `src/main/java/com/integradorii/gimnasiov1/dto/ReclamoDTO.java`
- Objeto de transferencia de datos para el formulario

---

### Frontend

#### 1. **Vista HTML** (`reclamos.html`)
- **Ubicación:** `src/main/resources/templates/reclamos.html`
- **Características:**
  - Integrada con el layout del sistema (sidebar, header)
  - Formulario con campos: Categoría, Asunto, Descripción, Prioridad
  - Sección dinámica de "Mis Reclamos Anteriores" usando Thymeleaf
  - Mensaje de respuesta para feedback al usuario
  - Responsive con Bootstrap y Tailwind CSS

#### 2. **Estilos CSS** (`reclamos.css`)
- **Ubicación:** `src/main/resources/static/Estilos/reclamos.css`
- Estilos personalizados para el formulario y lista de reclamos
- Estados visuales (En proceso, Resuelto)
- Diseño responsive

#### 3. **JavaScript** (`reclamos.js`)
- **Ubicación:** `src/main/resources/static/JavaScript/reclamos.js`
- **Funcionalidades:**
  - Captura del evento submit del formulario
  - Validación de campos en el cliente
  - Envío asíncrono (fetch API) al endpoint `/reclamos/crear`
  - Manejo de respuestas (éxito/error)
  - Feedback visual con mensajes
  - Recarga automática tras éxito para mostrar el nuevo reclamo

---

### Base de Datos

#### Script SQL (`create_reclamos_table.sql`)
- **Ubicación:** `src/main/resources/db/migration/create_reclamos_table.sql`
- Crea la tabla `reclamos` con todas las columnas especificadas
- Índices para optimizar consultas
- Constraints y valores por defecto
- Comentarios de documentación

**Nota:** Si la tabla ya existe en tu base de datos, no es necesario ejecutar este script. JPA creará/actualizará la tabla automáticamente si está configurado.

---

## Flujo de Funcionamiento

### 1. **Acceso a la Vista**
- Usuario deportista autenticado hace clic en "Quejas y Reclamos" en el sidebar
- Se redirige a `/reclamos`
- `ReclamoController.mostrarReclamos()` carga la vista con los reclamos existentes del usuario

### 2. **Envío del Formulario**
1. Usuario completa el formulario (categoría, asunto, descripción, prioridad)
2. Hace clic en "Enviar Reclamo"
3. JavaScript captura el evento y valida los campos
4. Se envía petición POST a `/reclamos/crear` con los datos
5. `ReclamoController.crearReclamo()` recibe la petición
6. Obtiene el deportista desde `UserDetails` (Spring Security)
7. `ReclamoService.crearReclamo()` valida y crea el reclamo
8. Se guarda en la base de datos con:
   - `deportista_id`: ID del usuario autenticado
   - `estado`: "En proceso" (por defecto)
   - `fecha_creacion`: Timestamp actual (automático)
   - `activo`: true
9. Retorna respuesta JSON con éxito/error
10. JavaScript muestra mensaje de confirmación
11. Página se recarga para mostrar el nuevo reclamo en la lista

### 3. **Visualización de Reclamos**
- La sección "Mis Reclamos Anteriores" muestra todos los reclamos del deportista
- Ordenados por fecha de creación (más recientes primero)
- Muestra: ID, asunto, fecha, estado
- Estados con colores distintivos (En proceso: naranja, Resuelto: verde)

---

## Configuración de Seguridad

El endpoint `/reclamos` está protegido y solo accesible para usuarios con rol `CLIENTE` (deportistas).

**Archivo:** `src/main/java/com/integradorii/gimnasiov1/config/SecurityConfig.java`

```java
.requestMatchers("/perfil", "/cliente/**", "/entrenamientos", "/evaluaciones", "/reclamos")
    .hasRole("CLIENTE")
```

---

## Validaciones Implementadas

### Backend (ReclamoService)
- ✅ Deportista no puede ser nulo
- ✅ Categoría es obligatoria
- ✅ Asunto es obligatorio
- ✅ Descripción es obligatoria
- ✅ Prioridad tiene valor por defecto "Normal"

### Frontend (reclamos.js)
- ✅ Campos obligatorios no pueden estar vacíos
- ✅ Feedback visual durante el envío (botón deshabilitado)
- ✅ Mensajes de error/éxito claros

---

## Estructura de la Tabla `reclamos`

| Campo | Tipo | Descripción | Obligatorio | Valor por Defecto |
|-------|------|-------------|-------------|-------------------|
| id | SERIAL | ID único del reclamo | Sí | Auto-incremento |
| deportista_id | INTEGER | ID del deportista (FK a personas) | Sí | - |
| categoria | VARCHAR(100) | Categoría del reclamo | Sí | - |
| asunto | VARCHAR(150) | Asunto breve | Sí | - |
| descripcion | TEXT | Descripción detallada | Sí | - |
| estado | VARCHAR(30) | Estado actual | No | 'En proceso' |
| fecha_creacion | TIMESTAMP | Fecha de creación | No | CURRENT_TIMESTAMP |
| fecha_actualizacion | TIMESTAMP | Última actualización | No | null |
| prioridad | VARCHAR(20) | Nivel de prioridad | No | 'Normal' |
| respuesta_admin | TEXT | Respuesta del administrador | No | null |
| atendido_por | INTEGER | ID del personal que atiende (FK) | No | null |
| activo | BOOLEAN | Eliminación lógica | No | true |

---

## Pruebas Recomendadas

### 1. Crear un Reclamo
1. Iniciar sesión como deportista
2. Ir a "Quejas y Reclamos" desde el sidebar
3. Completar el formulario:
   - Categoría: "Instalaciones y Equipamiento"
   - Asunto: "Aire acondicionado no funciona"
   - Descripción: "El aire acondicionado de la sala de pesas no está funcionando desde hace 3 días"
   - Prioridad: "Alta"
4. Hacer clic en "Enviar Reclamo"
5. Verificar mensaje de éxito
6. Verificar que aparece en "Mis Reclamos Anteriores"

### 2. Validar en Base de Datos
```sql
-- Ver todos los reclamos
SELECT * FROM reclamos ORDER BY fecha_creacion DESC;

-- Ver reclamos de un deportista específico
SELECT r.*, p.nombre, p.apellido 
FROM reclamos r 
JOIN personas p ON r.deportista_id = p.id 
WHERE p.email = 'deportista@ejemplo.com';
```

### 3. Verificar Seguridad
- Intentar acceder a `/reclamos` sin estar autenticado → Redirige a login
- Intentar acceder con usuario no deportista → Acceso denegado

---

## Próximas Mejoras (Opcionales)

- [ ] Panel de administración para gestionar reclamos
- [ ] Notificaciones por email al crear reclamo
- [ ] Sistema de respuestas del administrador
- [ ] Filtros por estado/categoría en la vista del deportista
- [ ] Adjuntar archivos/imágenes a los reclamos
- [ ] Estadísticas y reportes de reclamos
- [ ] Sistema de priorización automática

---

## Archivos Modificados/Creados

### Creados
- ✅ `model/Reclamo.java`
- ✅ `repository/ReclamoRepository.java`
- ✅ `service/ReclamoService.java`
- ✅ `controller/ReclamoController.java`
- ✅ `dto/ReclamoDTO.java`
- ✅ `resources/db/migration/create_reclamos_table.sql`

### Modificados
- ✅ `templates/reclamos.html` - Agregado campo prioridad, integración con backend
- ✅ `static/JavaScript/reclamos.js` - Implementado envío asíncrono
- ✅ `static/Estilos/reclamos.css` - Estilos adicionales
- ✅ `templates/fragments/sidebar.html` - Link a /reclamos
- ✅ `config/SecurityConfig.java` - Agregado /reclamos a rutas protegidas
- ✅ `controller/HomeController.java` - Eliminado método duplicado

---

## Contacto y Soporte

Si encuentras algún problema o necesitas agregar funcionalidades adicionales, no dudes en solicitar asistencia.

**Estado:** ✅ Implementación Completa y Funcional
