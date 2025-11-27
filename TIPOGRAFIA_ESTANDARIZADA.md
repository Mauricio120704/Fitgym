# Estandarización de Tipografía - FitGym

## Resumen de Cambios

Se ha implementado un sistema de tipografía consistente en toda la aplicación:

### **Tipografía Definida**

- **Títulos (h1-h6)**: `Poppins` (peso: 600-700)
- **Cuerpo y Datos**: `Inter` (peso: 400-500)
- **Código**: `Courier New` (monoespaciado)

### **Tamaños de Fuente**

#### Títulos (Poppins)
- `h1`: 2.5rem (40px)
- `h2`: 2rem (32px)
- `h3`: 1.5rem (24px)
- `h4`: 1.25rem (20px)
- `h5`: 1.1rem (17.6px)
- `h6`: 1rem (16px)

#### Cuerpo (Inter)
- Párrafos: 1rem (16px)
- Párrafos pequeños: 0.875rem (14px)
- Párrafos grandes: 1.125rem (18px)
- Etiquetas: 0.95rem (15.2px)
- Badges: 0.85rem (13.6px)

### **Archivos Modificados**

#### Archivo Base (Nuevo)
- ✅ `static/Estilos/global-styles.css` - Estilos globales de tipografía

#### Archivos CSS Actualizados
- ✅ `static/Estilos/index.css`
- ✅ `static/Estilos/login-styles.css`
- ✅ `static/Estilos/registro-styles.css`
- ✅ `static/Estilos/recuperacion.css`
- ✅ `static/Estilos/reclamos.css`
- ✅ `static/Estilos/configuracion-styles.css`
- ✅ `static/Estilos/checkout.css`
- ✅ `static/Estilos/perfil-usuario-styles.css`
- ✅ `static/Estilos/reservas.css`
- ✅ `static/deportista/mis-clases.css`

#### Archivos HTML Actualizados
- ✅ `templates/index.html`

### **Cómo Funciona**

Cada archivo CSS ahora importa `global-styles.css` al inicio:

```css
@import url('global-styles.css');
```

O para archivos en subdirectorios:

```css
@import url('../Estilos/global-styles.css');
```

Esto asegura que:
1. Todas las fuentes se cargan desde Google Fonts
2. Los estilos base se aplican globalmente
3. Los estilos específicos de cada página se superponen sin conflictos

### **Características Adicionales**

- ✅ Responsive: Tamaños ajustados para móviles (max-width: 768px y 480px)
- ✅ Accesibilidad: Line-height optimizado (1.6 para cuerpo)
- ✅ Consistencia: Colores y pesos de fuente estandarizados
- ✅ Rendimiento: Fuentes cargadas una sola vez

### **Próximos Pasos (Opcional)**

Si deseas mejorar aún más:

1. Agregar `global-styles.css` a los demás archivos HTML que no lo tengan
2. Revisar y ajustar colores para mayor consistencia
3. Implementar un sistema de espaciado consistente (padding/margin)
4. Crear variables CSS para colores y espacios

---

**Fecha de Implementación**: 27 de Noviembre de 2025
**Estado**: ✅ Completado
