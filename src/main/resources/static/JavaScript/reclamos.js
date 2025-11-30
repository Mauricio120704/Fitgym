// ---------------- INTERACCIÓN DEL FORMULARIO ----------------
const formReclamo = document.getElementById("formReclamo");
const btnEnviar = document.getElementById("btnEnviar");
const mensajeRespuesta = document.getElementById("mensajeRespuesta");

formReclamo.addEventListener("submit", async (e) => {
  e.preventDefault();

  const categoria = document.getElementById("categoria").value;
  const asunto = document.getElementById("asunto").value;
  const descripcion = document.getElementById("descripcion").value;
  const prioridad = document.getElementById("prioridad").value;

  // Validación básica (incluye prioridad)
  if (!categoria || !asunto || !descripcion || !prioridad) {
    mostrarMensaje("Por favor, completa todos los campos obligatorios.", "danger");
    return;
  }

  // Deshabilitar botón durante el envío
  btnEnviar.disabled = true;
  btnEnviar.textContent = "Enviando...";

  try {
    // Crear FormData para enviar al backend
    const formData = new URLSearchParams();
    formData.append("categoria", categoria);
    formData.append("asunto", asunto);
    formData.append("descripcion", descripcion);
    formData.append("prioridad", prioridad);

    // Enviar petición POST al backend
    const response = await fetch("/reclamos/crear", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData.toString()
    });

    const data = await response.json();

    if (data.success) {
      mostrarMensaje("✅ " + data.message + ". La página se recargará en breve.", "success");
      
      // Limpiar formulario
      formReclamo.reset();
      
      // Recargar página después de 2 segundos para mostrar el nuevo reclamo
      setTimeout(() => {
        window.location.reload();
      }, 2000);
    } else {
      mostrarMensaje("❌ " + data.message, "danger");
      btnEnviar.disabled = false;
      btnEnviar.textContent = "Enviar Reclamo";
    }

  } catch (error) {
    console.error("Error al enviar el reclamo:", error);
    mostrarMensaje("❌ Error al conectar con el servidor. Por favor, intenta nuevamente.", "danger");
    btnEnviar.disabled = false;
    btnEnviar.textContent = "Enviar Reclamo";
  }
});

// Función para mostrar mensajes de respuesta
function mostrarMensaje(mensaje, tipo) {
  mensajeRespuesta.textContent = mensaje;
  mensajeRespuesta.className = `alert alert-${tipo}`;
  mensajeRespuesta.style.display = "block";
  
  // Ocultar mensaje después de 5 segundos (solo si no es success)
  if (tipo !== "success") {
    setTimeout(() => {
      mensajeRespuesta.style.display = "none";
    }, 5000);
  }
}
