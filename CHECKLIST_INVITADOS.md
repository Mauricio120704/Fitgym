# ✅ Checklist de Verificación - Módulo de Invitados

Use este checklist para verificar que el módulo de invitados esté funcionando correctamente.

## 📋 Pre-Instalación

- [ ] PostgreSQL está instalado y corriendo
- [ ] Base de datos `gimnasioDB2` existe
- [ ] Usuario `postgres` con contraseña `123456` configurado
- [ ] Java 17 o superior instalado
- [ ] Maven instalado y configurado
- [ ] Proyecto compila sin errores (`mvn clean compile`)

## 🗄️ Base de Datos

### Migración SQL
- [ ] Script `agregar_invitado_por_persona_visitantes.sql` ejecutado
- [ ] Columna `invitado_por_persona_id` existe en tabla `visitantes`
- [ ] Clave foránea `fk_visitante_invitado_por_persona` creada
- [ ] Índice `idx_visitantes_invitado_por_persona` creado

### Verificación
```sql
-- Ejecutar para verificar
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'visitantes' 
  AND column_name = 'invitado_por_persona_id';
```
- [ ] Query retorna 1 fila con `data_type = bigint` y `is_nullable = YES`

## 💻 Código Backend

### Archivos Creados/Modificados
- [ ] `Visitante.java` - Campo `invitadoPorPersona` agregado
- [ ] `Visitante.java` - Getters y setters para `invitadoPorPersona`
- [ ] `VisitanteRepository.java` - Import de `Persona` agregado
- [ ] `VisitanteRepository.java` - Métodos `findByInvitadoPorPersona` agregados
- [ ] `InvitadoController.java` - Archivo creado con 5 endpoints
- [ ] `SecurityConfig.java` - Rutas `/admin/invitados/**` agregadas

### Compilación
- [ ] Proyecto compila sin errores
- [ ] No hay warnings críticos
- [ ] Todas las dependencias resueltas

```bash
mvn clean compile
```

## 🎨 Frontend

### Vistas Creadas
- [ ] `templates/admin/invitados/listado-miembros.html` - Creado
- [ ] `templates/admin/invitados/registro.html` - Creado
- [ ] `templates/admin/invitados/listado-invitados.html` - Creado
- [ ] `templates/fragments/header.html` - Creado
- [ ] `templates/fragments/footer.html` - Creado
- [ ] `templates/fragments/sidebar.html` - Enlace "Invitados" agregado

### Elementos de UI
- [ ] Tarjetas de miembros se visualizan correctamente
- [ ] Botón "+" visible en cada tarjeta
- [ ] Formulario de registro tiene todos los campos
- [ ] Tabla de invitados muestra columnas correctas
- [ ] Filtros funcionan correctamente
- [ ] Mensajes de éxito/error se muestran

## 🚀 Aplicación en Ejecución

### Inicio de Aplicación
- [ ] Aplicación inicia sin errores
- [ ] Puerto 8080 disponible
- [ ] Logs no muestran errores críticos
- [ ] Conexión a base de datos exitosa

```bash
mvn spring-boot:run
```

### Acceso y Navegación
- [ ] Aplicación accesible en `http://localhost:8080`
- [ ] Login funciona correctamente
- [ ] Usuario con rol RECEPCIONISTA puede acceder
- [ ] Menú lateral muestra opción "Invitados"
- [ ] Click en "Invitados" redirige correctamente

## 🧪 Pruebas Funcionales

### Test 1: Acceso al Módulo
- [ ] Navegar a `/admin/invitados`
- [ ] Página carga sin errores
- [ ] Se muestra listado de miembros
- [ ] Buscador está visible

### Test 2: Búsqueda de Miembros
- [ ] Ingresar texto en buscador
- [ ] Click en "Buscar"
- [ ] Resultados filtrados correctamente
- [ ] Sin errores en consola

### Test 3: Registro de Invitado
- [ ] Seleccionar un miembro
- [ ] Click en botón "+" o "Agregar Invitado"
- [ ] Formulario se carga correctamente
- [ ] Información del miembro se muestra
- [ ] Completar datos del invitado:
  - [ ] Nombre completo: "Test Invitado"
  - [ ] Documento: "99999999"
  - [ ] Teléfono: "999999999"
  - [ ] Email: "test@test.com"
  - [ ] Motivo: "Prueba del sistema"
- [ ] Click en "Registrar Invitado"
- [ ] Mensaje de éxito aparece
- [ ] Código de pase generado (formato INV-XXXXXXXX)
- [ ] Redirige a listado de miembros

### Test 4: Ver Historial de Invitados
- [ ] Click en "Ver Invitados" de un miembro
- [ ] Página carga correctamente
- [ ] Información del miembro se muestra
- [ ] Total de invitados es correcto
- [ ] Lista de invitados se muestra
- [ ] Invitado recién creado aparece
- [ ] Estado es "ACTIVO"
- [ ] Código de pase se muestra

### Test 5: Filtrar Invitados
- [ ] En historial de invitados
- [ ] Seleccionar filtro "ACTIVO"
- [ ] Click en "Filtrar"
- [ ] Solo invitados activos se muestran
- [ ] Cambiar a filtro "FINALIZADO"
- [ ] Solo invitados finalizados se muestran

### Test 6: Registrar Salida
- [ ] Localizar invitado activo
- [ ] Click en botón de salida (icono de flecha)
- [ ] Confirmación aparece
- [ ] Confirmar acción
- [ ] Mensaje de éxito aparece
- [ ] Estado cambia a "FINALIZADO"
- [ ] Ya no aparece en filtro "ACTIVO"

### Test 7: Validaciones
- [ ] Intentar registrar invitado sin nombre
- [ ] Error de validación aparece
- [ ] Intentar registrar invitado sin documento
- [ ] Error de validación aparece
- [ ] Formulario no se envía con datos incompletos

### Test 8: Códigos de Pase
- [ ] Registrar múltiples invitados
- [ ] Cada uno tiene código único
- [ ] Todos tienen formato INV-XXXXXXXX
- [ ] No hay códigos duplicados

## 🔒 Seguridad

### Permisos
- [ ] Usuario ADMINISTRADOR puede acceder
- [ ] Usuario RECEPCIONISTA puede acceder
- [ ] Usuario ENTRENADOR puede acceder
- [ ] Usuario CLIENTE/DEPORTISTA NO puede acceder
- [ ] Usuario sin autenticar redirige a login

### Auditoría
- [ ] Campo `registrado_por` se llena automáticamente
- [ ] Campo `invitado_por_persona_id` se llena correctamente
- [ ] Timestamps de ingreso se registran
- [ ] Timestamps de salida se registran

## 📊 Base de Datos (Post-Pruebas)

### Verificar Datos
```sql
-- Verificar invitados creados
SELECT * FROM visitantes 
WHERE invitado_por_persona_id IS NOT NULL 
ORDER BY fecha_hora_ingreso DESC;
```
- [ ] Invitados de prueba aparecen
- [ ] Datos son correctos
- [ ] Relaciones funcionan

### Integridad Referencial
```sql
-- Verificar relaciones
SELECT 
    v.nombre_completo AS invitado,
    p.nombre || ' ' || p.apellido AS invitado_por
FROM visitantes v
JOIN personas p ON v.invitado_por_persona_id = p.id
WHERE v.invitado_por_persona_id IS NOT NULL;
```
- [ ] Query ejecuta sin errores
- [ ] Relaciones son correctas

## 📱 Responsive Design

### Desktop (1920x1080)
- [ ] Layout se ve correctamente
- [ ] Tarjetas tienen buen tamaño
- [ ] Tablas son legibles
- [ ] Botones bien posicionados

### Tablet (768x1024)
- [ ] Layout se adapta
- [ ] Sidebar funciona
- [ ] Formularios son usables
- [ ] Tablas tienen scroll

### Mobile (375x667)
- [ ] Sidebar se oculta
- [ ] Tarjetas en columna única
- [ ] Formularios son usables
- [ ] Botones accesibles

## 🐛 Manejo de Errores

### Errores Comunes
- [ ] Error 404 no aparece en rutas válidas
- [ ] Error 500 no aparece en operaciones normales
- [ ] Errores de validación se muestran claramente
- [ ] Errores de base de datos se manejan

### Logs
- [ ] No hay errores en logs de aplicación
- [ ] No hay warnings críticos
- [ ] Queries SQL se ejecutan correctamente

## 📈 Performance

### Tiempos de Carga
- [ ] Listado de miembros carga en < 2 segundos
- [ ] Formulario de registro carga en < 1 segundo
- [ ] Historial de invitados carga en < 2 segundos
- [ ] Búsqueda responde en < 1 segundo

### Consultas SQL
- [ ] Índices están siendo utilizados
- [ ] No hay N+1 queries
- [ ] Joins son eficientes

## 📚 Documentación

### Archivos de Documentación
- [ ] `RESUMEN_INVITADOS.md` - Existe y está completo
- [ ] `INVITADOS_IMPLEMENTATION.md` - Existe y está completo
- [ ] `INSTALACION_INVITADOS.md` - Existe y está completo
- [ ] `CASOS_USO_INVITADOS.md` - Existe y está completo
- [ ] `README.md` - Actualizado con nueva funcionalidad

### Scripts SQL
- [ ] `agregar_invitado_por_persona_visitantes.sql` - Existe
- [ ] `verificar_invitados.sql` - Existe

## ✅ Checklist Final

### Funcionalidad Completa
- [ ] Todos los endpoints funcionan
- [ ] Todas las vistas se renderizan
- [ ] Todas las validaciones funcionan
- [ ] Todos los filtros funcionan
- [ ] Todas las búsquedas funcionan

### Calidad de Código
- [ ] Código compila sin errores
- [ ] Código sigue convenciones
- [ ] No hay código duplicado
- [ ] Comentarios son claros

### Experiencia de Usuario
- [ ] Interfaz es intuitiva
- [ ] Mensajes son claros
- [ ] Flujo es lógico
- [ ] No hay pasos innecesarios

### Documentación
- [ ] Documentación técnica completa
- [ ] Guía de instalación clara
- [ ] Casos de uso documentados
- [ ] README actualizado

## 🎉 Resultado Final

Si todos los items están marcados:
- ✅ **MÓDULO COMPLETAMENTE FUNCIONAL**
- ✅ **LISTO PARA PRODUCCIÓN**
- ✅ **DOCUMENTACIÓN COMPLETA**

Si hay items sin marcar:
- ⚠️ Revisar items pendientes
- ⚠️ Consultar documentación
- ⚠️ Ejecutar pruebas adicionales

---

**Fecha de Verificación**: _______________  
**Verificado por**: _______________  
**Resultado**: [ ] APROBADO  [ ] PENDIENTE  [ ] RECHAZADO  
**Notas**: _____________________________________
