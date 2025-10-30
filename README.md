# 🏋️ Sistema de Gestión de Gimnasio

Sistema web completo para la gestión de miembros y pagos de un gimnasio, desarrollado con Spring Boot y diseño moderno con Tailwind CSS.

## 📋 Características

### ✨ Gestión de Miembros
- ✅ Lista de miembros con filtros (Activos/Inactivos)
- ✅ Búsqueda en tiempo real
- ✅ Registro de nuevos miembros
- ✅ Edición de información de miembros
- ✅ Eliminación de miembros
- ✅ Fecha de registro automática
- ✅ Estadísticas en tiempo real

### 👥 Gestión de Invitados (NUEVO)
- ✅ Registro de invitados asociados a miembros
- ✅ Generación automática de código de pase (INV-XXXXXXXX)
- ✅ Búsqueda de miembros para registrar invitados
- ✅ Historial de invitados por miembro
- ✅ Filtros por estado (ACTIVO/FINALIZADO/CANCELADO)
- ✅ Registro de entrada y salida
- ✅ Control de acceso temporal
- ✅ Auditoría completa de invitaciones

### 💰 Historial de Pagos
- ✅ Visualización de todos los pagos
- ✅ Filtros por estado (Completado/Pendiente/Fallido)
- ✅ Búsqueda por código, plan o método de pago
- ✅ Moneda en Soles Peruanos (S/)
- ✅ Estadísticas de pagos anuales

### 🎨 Interfaz de Usuario
- ✅ Diseño moderno y responsive
- ✅ Sidebar colapsable con iconos
- ✅ Estado persistente del sidebar (localStorage)
- ✅ Botón flotante para acciones rápidas
- ✅ Tailwind CSS para estilos
- ✅ Transiciones suaves

## 🛠️ Tecnologías Utilizadas

### Backend
- **Spring Boot 3.4.1** - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **H2 Database** - Base de datos en memoria
- **Thymeleaf** - Motor de plantillas
- **Maven** - Gestión de dependencias

### Frontend
- **Tailwind CSS** - Framework CSS
- **JavaScript Vanilla** - Interactividad
- **Thymeleaf** - Renderizado del lado del servidor

## 🚀 Instalación y Ejecución

### Prerrequisitos
- Java 17 o superior
- Maven 3.6 o superior

### Pasos para ejecutar

1. **Clonar el repositorio**
```bash
git clone <url-del-repositorio>
cd gimnasiov1
```

2. **Ejecutar la aplicación**
```bash
./mvnw spring-boot:run
```

O en Windows:
```bash
mvnw.cmd spring-boot:run
```

3. **Acceder a la aplicación**
- Aplicación principal: http://localhost:8080
- Consola H2: http://localhost:8080/h2-console

## 📁 Estructura del Proyecto

```
gimnasiov1/
├── src/
│   ├── main/
│   │   ├── java/com/integradorii/gimnasiov1/
│   │   │   ├── controller/       # Controladores MVC
│   │   │   ├── model/            # Entidades JPA
│   │   │   ├── repository/       # Repositorios Spring Data
│   │   │   ├── service/          # Lógica de negocio
│   │   │   └── DataInitializer.java  # Datos de prueba
│   │   └── resources/
│   │       ├── static/           # Archivos estáticos (CSS, JS)
│   │       ├── templates/        # Plantillas Thymeleaf
│   │       └── application.properties
│   └── test/                     # Tests unitarios
└── pom.xml                       # Configuración Maven
```

## 🗄️ Modelo de Datos

### Miembro
- ID (Long)
- Nombre (String)
- Apellido (String)
- Email (String)
- Teléfono (String)
- Fecha de Registro (LocalDate) - Automática
- Membresía Activa (Boolean)

### Pago
- ID (Long)
- Código de Pago (String)
- Fecha (LocalDate)
- Plan/Servicio (String)
- Método de Pago (String)
- Monto (Double)
- Estado (String)

## 🌐 Rutas Principales

| Ruta | Método | Descripción |
|------|--------|-------------|
| `/` | GET | Redirige a lista de miembros |
| `/miembros` | GET | Lista de miembros |
| `/miembros/nuevo` | GET | Formulario nuevo miembro |
| `/miembros` | POST | Guardar nuevo miembro |
| `/miembros/editar/{id}` | GET | Formulario editar miembro |
| `/miembros/{id}` | POST | Actualizar miembro |
| `/miembros/eliminar/{id}` | GET | Eliminar miembro |
| `/pagos` | GET | Historial de pagos |
| `/admin/invitados` | GET | Lista de miembros para invitados |
| `/admin/invitados/registrar/{personaId}` | GET | Formulario registrar invitado |
| `/admin/invitados/registrar/{personaId}` | POST | Guardar invitado |
| `/admin/invitados/persona/{personaId}` | GET | Historial de invitados |
| `/admin/invitados/{id}/registrar-salida` | POST | Registrar salida de invitado |

## 🎯 Funcionalidades Destacadas

### Sidebar Inteligente
- Se colapsa a solo iconos para maximizar espacio
- Estado persistente entre páginas usando localStorage
- Sin parpadeos al navegar
- Responsive para móviles

### Filtros y Búsqueda
- Filtros por estado en ambas secciones
- Búsqueda en tiempo real
- Combinación de filtros
- Estadísticas dinámicas

### Diseño Responsive
- Adaptado para desktop, tablet y móvil
- Sidebar oculto en móviles
- Tablas con scroll horizontal
- Botones flotantes para acciones rápidas

## 📝 Configuración

### Base de Datos H2
La aplicación usa H2 en memoria por defecto. Para acceder a la consola:

- URL: `jdbc:h2:mem:gimnasio`
- Usuario: `sa`
- Contraseña: (vacío)

### Datos de Prueba
La aplicación incluye datos de ejemplo que se cargan automáticamente:
- 4 miembros de ejemplo
- 5 pagos de ejemplo

## 🤝 Contribuciones

Este es un proyecto académico para el curso Integrador II.

## 👨‍💻 Autor

Desarrollado como proyecto integrador para la universidad.

## 📄 Licencia

Este proyecto es de uso académico.

## 📚 Documentación Adicional

### Módulo de Invitados
- **[RESUMEN_INVITADOS.md](RESUMEN_INVITADOS.md)** - Resumen ejecutivo completo
- **[INVITADOS_IMPLEMENTATION.md](INVITADOS_IMPLEMENTATION.md)** - Documentación técnica detallada
- **[INSTALACION_INVITADOS.md](INSTALACION_INVITADOS.md)** - Guía de instalación paso a paso
- **[CASOS_USO_INVITADOS.md](CASOS_USO_INVITADOS.md)** - Casos de uso y escenarios de prueba

### Otros Módulos
- **[FUNCIONALIDADES.md](FUNCIONALIDADES.md)** - Listado completo de funcionalidades
- **[RECLAMOS_IMPLEMENTATION.md](RECLAMOS_IMPLEMENTATION.md)** - Módulo de reclamos
- **[VERIFICACION_EMAIL_IMPLEMENTATION.md](VERIFICACION_EMAIL_IMPLEMENTATION.md)** - Sistema de verificación de email

---

⭐ Si te gusta este proyecto, dale una estrella en GitHub!
