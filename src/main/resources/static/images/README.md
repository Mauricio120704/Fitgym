# Carpeta de Imágenes

Esta carpeta contiene las imágenes utilizadas en la aplicación.

## 📸 Instrucciones para agregar la imagen de fondo pr2.jpeg

### Paso 1: Localizar la imagen
Busca el archivo `pr2.jpeg` en tu computadora. Posibles ubicaciones:
- Carpeta "avance integrador"
- Descargas
- Documentos
- Escritorio

### Paso 2: Copiar la imagen
1. Copia el archivo `pr2.jpeg`
2. Pégalo en esta carpeta: `src/main/resources/static/images/`
3. Asegúrate de que el nombre sea exactamente: `pr2.jpeg`

### Paso 3: Activar la imagen en el CSS
1. Abre el archivo: `src/main/resources/static/Estilos/index.css`
2. Busca la línea 20 que dice:
   ```css
   /* background-image: url('/images/pr2.jpeg'); */
   ```
3. Elimina los `/*` y `*/` para descomentarla:
   ```css
   background-image: url('/images/pr2.jpeg');
   ```
4. Comenta o elimina la línea 21 (imagen de Unsplash):
   ```css
   /* background-image: url('https://images.unsplash.com/...'); */
   ```

### Paso 4: Reiniciar la aplicación
1. Detén el servidor Spring Boot (si está corriendo)
2. Vuelve a iniciar la aplicación
3. Accede a `http://localhost:8080/` o `http://localhost:8080/inicio`

¡Listo! La imagen pr2.jpeg se mostrará como fondo de la pantalla de inicio.
