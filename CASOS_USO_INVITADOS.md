# Casos de Uso - Módulo de Invitados

## 📖 Descripción General
Este documento describe los casos de uso del módulo de invitados, incluyendo flujos de trabajo, ejemplos prácticos y escenarios de prueba.

## 👥 Actores

### Actor Principal
- **Recepcionista**: Personal encargado de registrar y gestionar invitados

### Actores Secundarios
- **Administrador**: Puede realizar todas las operaciones del recepcionista
- **Entrenador**: Puede consultar y registrar invitados
- **Miembro/Deportista**: Cliente del gimnasio que puede invitar personas

## 📋 Casos de Uso Principales

### CU-01: Registrar Invitado para un Miembro

**Actor**: Recepcionista  
**Precondiciones**: 
- El recepcionista está autenticado
- Existe al menos un miembro registrado en el sistema

**Flujo Principal**:
1. El recepcionista accede al módulo "Invitados" desde el menú lateral
2. El sistema muestra el listado de miembros
3. El recepcionista busca al miembro que trae un invitado
4. El recepcionista hace clic en el botón "+" o "Agregar Invitado"
5. El sistema muestra el formulario de registro
6. El recepcionista ingresa los datos del invitado:
   - Nombre completo (requerido)
   - Documento de identidad (requerido)
   - Teléfono (opcional)
   - Email (opcional)
   - Motivo de visita (opcional)
7. El recepcionista hace clic en "Registrar Invitado"
8. El sistema valida los datos
9. El sistema genera un código de pase único (INV-XXXXXXXX)
10. El sistema guarda el registro asociándolo al miembro
11. El sistema muestra mensaje de éxito con el código de pase
12. El recepcionista entrega el código al invitado

**Postcondiciones**:
- El invitado queda registrado en el sistema
- El invitado está asociado al miembro
- El estado del invitado es "ACTIVO"
- Se ha generado un código de pase único

**Flujo Alternativo 1: Datos Incompletos**
- 8a. El sistema detecta que faltan datos requeridos
- 8b. El sistema muestra mensaje de error indicando los campos faltantes
- 8c. El flujo vuelve al paso 6

**Ejemplo Práctico**:
```
Miembro: Juan Pérez (DNI: 12345678)
Invitado: María López
Documento: 87654321
Teléfono: 999888777
Email: maria.lopez@example.com
Motivo: Conocer las instalaciones
Código generado: INV-A3B7C9D1
```

---

### CU-02: Consultar Invitados de un Miembro

**Actor**: Recepcionista  
**Precondiciones**: 
- El recepcionista está autenticado
- El miembro existe en el sistema

**Flujo Principal**:
1. El recepcionista accede al módulo "Invitados"
2. El sistema muestra el listado de miembros
3. El recepcionista localiza al miembro deseado
4. El recepcionista hace clic en "Ver Invitados"
5. El sistema muestra el historial completo de invitados del miembro
6. El sistema muestra:
   - Información del miembro
   - Total de invitados
   - Lista de invitados con sus datos
   - Estado de cada invitado

**Postcondiciones**:
- Se visualiza el historial de invitados del miembro

**Filtros Disponibles**:
- Todos los invitados
- Solo invitados activos
- Solo invitados finalizados
- Solo invitados cancelados

**Ejemplo Práctico**:
```
Miembro: Juan Pérez
Total de invitados: 5

Historial:
1. María López (INV-A3B7C9D1) - ACTIVO - 29/10/2024 14:30
2. Pedro García (INV-B2C8D4E6) - FINALIZADO - 28/10/2024 10:15
3. Ana Martínez (INV-C1D9E5F7) - FINALIZADO - 25/10/2024 16:45
4. Luis Rodríguez (INV-D0E8F6G8) - FINALIZADO - 20/10/2024 09:30
5. Carmen Silva (INV-E9F7G5H3) - FINALIZADO - 15/10/2024 18:00
```

---

### CU-03: Registrar Salida de Invitado

**Actor**: Recepcionista  
**Precondiciones**: 
- El recepcionista está autenticado
- El invitado está registrado con estado "ACTIVO"

**Flujo Principal**:
1. El recepcionista accede a "Ver Invitados" del miembro correspondiente
2. El sistema muestra la lista de invitados
3. El recepcionista localiza al invitado que está saliendo
4. El recepcionista hace clic en el botón de "Registrar Salida"
5. El sistema solicita confirmación
6. El recepcionista confirma la acción
7. El sistema actualiza el estado del invitado a "FINALIZADO"
8. El sistema registra la fecha y hora de salida
9. El sistema muestra mensaje de éxito

**Postcondiciones**:
- El estado del invitado cambia a "FINALIZADO"
- Se registra la fecha y hora de salida
- El invitado ya no aparece como activo

**Flujo Alternativo 1: Cancelar Operación**
- 6a. El recepcionista cancela la confirmación
- 6b. El sistema no realiza cambios
- 6c. El flujo termina

**Ejemplo Práctico**:
```
Invitado: María López (INV-A3B7C9D1)
Estado anterior: ACTIVO
Fecha ingreso: 29/10/2024 14:30
Fecha salida: 29/10/2024 17:45
Estado nuevo: FINALIZADO
Tiempo de permanencia: 3 horas 15 minutos
```

---

### CU-04: Buscar Miembro para Registrar Invitado

**Actor**: Recepcionista  
**Precondiciones**: 
- El recepcionista está autenticado
- Existen miembros en el sistema

**Flujo Principal**:
1. El recepcionista accede al módulo "Invitados"
2. El sistema muestra el listado de miembros
3. El recepcionista ingresa criterios de búsqueda en el campo de búsqueda
4. El recepcionista hace clic en "Buscar"
5. El sistema filtra los miembros según los criterios
6. El sistema muestra solo los miembros que coinciden

**Criterios de Búsqueda**:
- Nombre
- Apellido
- Email
- Teléfono
- DNI

**Postcondiciones**:
- Se muestran solo los miembros que coinciden con la búsqueda

**Ejemplo Práctico**:
```
Búsqueda: "juan"
Resultados:
- Juan Pérez (DNI: 12345678)
- Juan Carlos Gómez (DNI: 23456789)
- María Juana Torres (DNI: 34567890)

Búsqueda: "12345678"
Resultados:
- Juan Pérez (DNI: 12345678)
```

---

## 🎯 Escenarios de Prueba

### Escenario 1: Registro Exitoso de Invitado

**Datos de Entrada**:
- Miembro: Juan Pérez (ID: 1)
- Nombre invitado: María López
- Documento: 87654321
- Teléfono: 999888777
- Email: maria@example.com
- Motivo: Conocer instalaciones

**Resultado Esperado**:
- ✅ Invitado registrado exitosamente
- ✅ Código de pase generado (INV-XXXXXXXX)
- ✅ Estado: ACTIVO
- ✅ Asociado al miembro Juan Pérez
- ✅ Fecha y hora de ingreso registrada

---

### Escenario 2: Registro con Datos Mínimos

**Datos de Entrada**:
- Miembro: Ana García (ID: 2)
- Nombre invitado: Pedro Sánchez
- Documento: 11223344
- Teléfono: (vacío)
- Email: (vacío)
- Motivo: (vacío)

**Resultado Esperado**:
- ✅ Invitado registrado exitosamente
- ✅ Código de pase generado
- ✅ Campos opcionales quedan vacíos

---

### Escenario 3: Registro sin Nombre (Error)

**Datos de Entrada**:
- Miembro: Juan Pérez (ID: 1)
- Nombre invitado: (vacío)
- Documento: 87654321

**Resultado Esperado**:
- ❌ Error de validación
- ❌ Mensaje: "El nombre completo es requerido"
- ❌ No se crea el registro

---

### Escenario 4: Registro sin Documento (Error)

**Datos de Entrada**:
- Miembro: Juan Pérez (ID: 1)
- Nombre invitado: María López
- Documento: (vacío)

**Resultado Esperado**:
- ❌ Error de validación
- ❌ Mensaje: "El documento de identidad es requerido"
- ❌ No se crea el registro

---

### Escenario 5: Consultar Invitados de Miembro sin Invitados

**Datos de Entrada**:
- Miembro: Carlos Ruiz (ID: 5, sin invitados registrados)

**Resultado Esperado**:
- ✅ Se muestra la página de invitados
- ✅ Total de invitados: 0
- ✅ Mensaje: "No se encontraron invitados"

---

### Escenario 6: Registrar Salida de Invitado Activo

**Datos de Entrada**:
- Invitado: María López (ID: 10, Estado: ACTIVO)

**Resultado Esperado**:
- ✅ Estado cambia a FINALIZADO
- ✅ Fecha y hora de salida registrada
- ✅ Mensaje de éxito mostrado
- ✅ Ya no aparece en lista de activos

---

### Escenario 7: Múltiples Invitados del Mismo Miembro

**Datos de Entrada**:
- Miembro: Juan Pérez (ID: 1)
- Registrar 3 invitados diferentes en el mismo día

**Resultado Esperado**:
- ✅ Los 3 invitados se registran correctamente
- ✅ Cada uno tiene su propio código único
- ✅ Todos están asociados al mismo miembro
- ✅ Todos aparecen en el historial del miembro

---

### Escenario 8: Búsqueda de Miembro por Nombre Parcial

**Datos de Entrada**:
- Búsqueda: "juan"

**Resultado Esperado**:
- ✅ Se muestran todos los miembros con "juan" en nombre o apellido
- ✅ Búsqueda no sensible a mayúsculas/minúsculas
- ✅ Incluye coincidencias parciales

---

### Escenario 9: Filtrar Invitados por Estado

**Datos de Entrada**:
- Miembro: Juan Pérez (con 5 invitados: 2 activos, 3 finalizados)
- Filtro: ACTIVO

**Resultado Esperado**:
- ✅ Se muestran solo los 2 invitados activos
- ✅ Los 3 finalizados no aparecen
- ✅ Total mostrado: 2

---

### Escenario 10: Verificar Códigos de Pase Únicos

**Datos de Entrada**:
- Registrar 100 invitados diferentes

**Resultado Esperado**:
- ✅ Cada invitado tiene un código único
- ✅ Todos los códigos tienen formato INV-XXXXXXXX
- ✅ No hay códigos duplicados

---

## 📊 Métricas y Reportes Sugeridos

### Métricas por Miembro
- Total de invitados histórico
- Invitados activos actualmente
- Promedio de invitados por mes
- Último invitado registrado

### Métricas Generales
- Total de invitados en el sistema
- Invitados activos vs finalizados
- Miembros que más invitan
- Días con más invitados
- Tiempo promedio de permanencia

### Reportes Útiles
1. **Reporte de Invitados por Período**
   - Filtrar por rango de fechas
   - Exportar a Excel

2. **Reporte de Miembros Más Activos**
   - Ordenar por cantidad de invitados
   - Mostrar tendencias

3. **Reporte de Invitados Activos**
   - Lista de invitados actualmente en el gimnasio
   - Tiempo de permanencia

## 🔐 Consideraciones de Seguridad

### Permisos
- Solo personal administrativo puede acceder
- Los deportistas NO pueden ver esta sección
- Cada acción queda registrada con el usuario que la realizó

### Validaciones
- Todos los campos requeridos deben completarse
- El documento debe ser único por invitado activo
- El miembro debe existir y estar activo

### Auditoría
- Se registra quién registró cada invitado
- Se registra la fecha y hora exacta de ingreso
- Se registra la fecha y hora exacta de salida

## 💡 Mejores Prácticas

1. **Verificar Identidad**: Siempre solicitar documento del invitado
2. **Código de Pase**: Entregar el código al invitado para control de salida
3. **Registro Completo**: Llenar todos los campos posibles para mejor seguimiento
4. **Salida Oportuna**: Registrar la salida cuando el invitado se retira
5. **Revisión Periódica**: Revisar invitados activos al final del día

## 🎓 Capacitación del Personal

### Temas a Cubrir
1. Acceso al módulo de invitados
2. Búsqueda de miembros
3. Registro de invitados
4. Consulta de historial
5. Registro de salidas
6. Manejo de errores comunes

### Tiempo Estimado
- Capacitación inicial: 30 minutos
- Práctica supervisada: 1 hora
- Evaluación: 15 minutos

## 📞 Soporte y Ayuda

Para más información, consultar:
- `INVITADOS_IMPLEMENTATION.md` - Documentación técnica completa
- `INSTALACION_INVITADOS.md` - Guía de instalación paso a paso
