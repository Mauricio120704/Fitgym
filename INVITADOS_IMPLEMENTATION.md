# Implementación de Gestión de Invitados

## Historia de Usuario
Como recepcionista, quiero registrar los datos de un visitante con un pase temporal asociado a un miembro del gimnasio, para controlar el acceso de invitados y llevar un registro de quién los invita.

## Descripción
Esta funcionalidad permite a los recepcionistas registrar invitados asociados a miembros del gimnasio (deportistas). Cada invitado queda vinculado al miembro que lo invita, facilitando el control y seguimiento de visitas.

## Componentes Implementados

### 1. Modelo de Datos
**Archivo**: `src/main/java/com/integradorii/gimnasiov1/model/Visitante.java`

- Se agregó el campo `invitadoPorPersona` (relación ManyToOne con `Persona`)
- Permite asociar cada visitante/invitado con un miembro del gimnasio
- La relación es opcional (nullable) para mantener compatibilidad con visitantes no invitados

### 2. Repositorio
**Archivo**: `src/main/java/com/integradorii/gimnasiov1/repository/VisitanteRepository.java`

Métodos agregados:
- `findByInvitadoPorPersona(Persona persona)`: Busca todos los invitados de una persona
- `findByInvitadoPorPersonaAndEstado(Persona persona, String estado)`: Busca invitados por persona y estado
- `countByInvitadoPorPersona(Persona persona)`: Cuenta los invitados de una persona

### 3. Controlador
**Archivo**: `src/main/java/com/integradorii/gimnasiov1/controller/InvitadoController.java`

Endpoints implementados:
- `GET /admin/invitados`: Lista todos los miembros con opción de agregar invitados
- `GET /admin/invitados/registrar/{personaId}`: Muestra formulario para registrar invitado
- `POST /admin/invitados/registrar/{personaId}`: Procesa el registro del invitado
- `GET /admin/invitados/persona/{personaId}`: Muestra los invitados de un miembro específico
- `POST /admin/invitados/{id}/registrar-salida`: Registra la salida de un invitado

### 4. Vistas

#### Listado de Miembros
**Archivo**: `src/main/resources/templates/admin/invitados/listado-miembros.html`

Características:
- Muestra todos los miembros del gimnasio en tarjetas
- Buscador por nombre, apellido, email, teléfono o DNI
- Botón "+" para agregar invitado rápidamente
- Botón "Ver Invitados" para ver el historial
- Indicador de membresía activa/inactiva

#### Formulario de Registro
**Archivo**: `src/main/resources/templates/admin/invitados/registro.html`

Campos del formulario:
- Nombre completo del invitado (requerido)
- Documento de identidad (requerido)
- Teléfono (opcional)
- Email (opcional)
- Motivo de visita (opcional)
- Muestra información del miembro que invita

#### Listado de Invitados por Miembro
**Archivo**: `src/main/resources/templates/admin/invitados/listado-invitados.html`

Características:
- Muestra todos los invitados de un miembro específico
- Filtro por estado (ACTIVO, FINALIZADO, CANCELADO)
- Información del miembro invitante
- Contador total de invitados
- Botón para registrar salida de invitados activos

### 5. Seguridad
**Archivo**: `src/main/java/com/integradorii/gimnasiov1/config/SecurityConfig.java`

- Se agregaron las rutas `/admin/invitados/**` al SecurityConfig
- Acceso permitido para roles: ADMINISTRADOR, RECEPCIONISTA, ENTRENADOR

### 6. Navegación
**Archivo**: `src/main/resources/templates/fragments/sidebar.html`

- Se agregó el enlace "Invitados" en el menú lateral
- Visible solo para personal administrativo
- Icono: Usuario con símbolo de agregar

### 7. Migración de Base de Datos
**Archivo**: `src/main/resources/db/migration/agregar_invitado_por_persona_visitantes.sql`

Cambios en la base de datos:
```sql
ALTER TABLE visitantes 
ADD COLUMN invitado_por_persona_id BIGINT NULL;

ALTER TABLE visitantes 
ADD CONSTRAINT fk_visitante_invitado_por_persona 
FOREIGN KEY (invitado_por_persona_id) 
REFERENCES personas(id) 
ON DELETE SET NULL;
```

## Flujo de Uso

1. **Acceder a la sección de Invitados**
   - El recepcionista accede desde el menú lateral: "Invitados"

2. **Buscar al miembro**
   - Se muestra el listado de todos los miembros
   - Puede usar el buscador para encontrar al miembro específico

3. **Registrar invitado**
   - Click en el botón "+" o "Agregar" del miembro
   - Completar el formulario con los datos del invitado
   - El sistema genera automáticamente un código de pase (formato: INV-XXXXXXXX)

4. **Ver invitados de un miembro**
   - Click en "Ver Invitados" en la tarjeta del miembro
   - Se muestra el historial completo de invitados
   - Puede filtrar por estado

5. **Registrar salida**
   - En el listado de invitados, click en el botón de salida
   - El sistema marca el invitado como FINALIZADO

## Características Técnicas

### Código de Pase
- Formato: `INV-XXXXXXXX` (8 caracteres alfanuméricos)
- Generado automáticamente con UUID
- Único para cada invitado

### Estados de Invitado
- **ACTIVO**: Invitado actualmente en las instalaciones
- **FINALIZADO**: Invitado que ya registró su salida
- **CANCELADO**: Invitado cancelado (no utilizado actualmente)

### Validaciones
- Nombre completo: Requerido
- Documento de identidad: Requerido
- Teléfono y email: Opcionales
- Motivo de visita: Opcional

## Diferencias con Visitantes Regulares

| Característica | Visitante Regular | Invitado |
|----------------|-------------------|----------|
| Asociación | No tiene | Asociado a un miembro |
| Código de pase | VP-XXXXXXXX | INV-XXXXXXXX |
| Registro | Por recepcionista | Por recepcionista para un miembro |
| Seguimiento | Individual | Por miembro invitante |

## Permisos y Roles

- **ADMINISTRADOR**: Acceso completo
- **RECEPCIONISTA**: Acceso completo
- **ENTRENADOR**: Acceso completo
- **CLIENTE/DEPORTISTA**: Sin acceso (solo personal administrativo)

## Próximas Mejoras Sugeridas

1. Límite de invitados por miembro
2. Validación de vigencia de membresía del invitante
3. Notificaciones al miembro cuando su invitado ingresa/sale
4. Reportes de invitados por período
5. Estadísticas de invitados por miembro
6. Restricciones de horario para invitados
7. Posibilidad de pre-registrar invitados con anticipación

## Notas Técnicas

- La relación con `Persona` es opcional (nullable) para mantener compatibilidad con visitantes existentes
- Se utiliza `ON DELETE SET NULL` para no perder el registro del invitado si se elimina el miembro
- Los índices mejoran el rendimiento de las consultas por persona
- El sistema mantiene compatibilidad con el módulo de visitantes existente
