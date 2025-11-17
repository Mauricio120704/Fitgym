let clases = [];

const lista = document.getElementById("listaClases");
const filtroTipo = document.getElementById("filtroTipo");
const modal = document.getElementById("modalReserva");
const detalle = document.getElementById("detalleClase");
const btnCancelar = document.getElementById("btnCancelar");
const btnConfirmar = document.getElementById("btnConfirmar");
const listaProximas = document.getElementById("listaProximas");
// AÑADIR: Nuevos elementos
const subtituloClases = document.getElementById("subtituloClases");
const inputFecha = document.getElementById("fechaSeleccionada"); 
let claseSeleccionada = null;

function mostrarClases(filtro = "todas") {
  lista.innerHTML = "";
  const filtradas =
    filtro === "todas" ? clases : clases.filter((c) => (c.tipo || "").toLowerCase() === filtro.toLowerCase());

  filtradas.forEach((clase) => {
    const card = document.createElement("div");
    card.classList.add("tarjeta");

    card.innerHTML = `
      <div class="info">
        <h3>${clase.nombre}
          <span class="nivel">${(clase.nivel || "").toString()}</span>
        </h3>
        <p class="datos">🕒 ${clase.horaInicio} - ${clase.horaFin} • 👤 ${
      clase.instructor
    } • 👥 ${clase.cupos} • ★ ${(clase.rating || "4.8")}</p>
      </div>
      <button class="boton-reservar">Reservar</button>
    `;
    card
      .querySelector(".boton-reservar")
      .addEventListener("click", () => abrirModal(clase));
    lista.appendChild(card);
  });
}

function abrirModal(clase) {
  claseSeleccionada = clase;

  // MODIFICADO: Lee el valor del input oculto que actualiza Flatpickr
  const fechaFormateada = inputFecha.value || "Fecha no seleccionada";
  
  detalle.innerHTML = `
    <strong>${clase.nombre}</strong><br>
    Fecha: ${fechaFormateada}<br> 
    Hora: ${clase.horaInicio}<br>
    Instructor: ${clase.instructor}<br>
    Dificultad: ${clase.nivel}
  `;
  modal.style.display = "flex";

}

btnCancelar.addEventListener("click", () => (modal.style.display = "none"));

btnConfirmar.addEventListener("click", async () => {
  if (!claseSeleccionada) return;
  await reservarClase(claseSeleccionada.id);
});

filtroTipo.addEventListener("change", (e) => mostrarClases(e.target.value));

// --- AÑADIR: LÓGICA DEL CALENDARIO ---

// Función para formatear la fecha como en el mockup (ej: "sábado, 25 de octubre")
function formatearFecha(dateObj) {
  return dateObj.toLocaleDateString("es-ES", {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  });
}

// Función para actualizar la UI cuando cambia la fecha
function actualizarFecha(fechaSeleccionada) {
  const fechaFormateada = formatearFecha(fechaSeleccionada);
  
  // 1. Actualiza el subtítulo
  subtituloClases.textContent = `Clases para ${fechaFormateada}`;
  
  // 2. Actualiza el input oculto (para que el modal lo use)
  // (Formato "25 de octubre")
  inputFecha.value = fechaSeleccionada.toLocaleDateString("es-ES", {
    day: 'numeric',
    month: 'long',
  });
}

async function cargarClases(fechaISO, tipo = "todas") {
  try {
    const params = new URLSearchParams({ fecha: fechaISO });
    if (tipo && tipo !== "todas") params.append("tipo", tipo);
    const res = await fetch(`/api/reservas/clases?${params.toString()}`);
    if (!res.ok) throw new Error("Error al cargar clases");
    clases = await res.json();
    poblarFiltroTipos(clases);
    mostrarClases(filtroTipo.value || "todas");
  } catch (e) {
    console.error(e);
    lista.innerHTML = '<div class="text-red-600">No se pudieron cargar las clases.</div>';
  }
}

async function cargarProximas() {
  try {
    const res = await fetch(`/api/reservas/proximas`);
    if (!res.ok) throw new Error("Error al cargar próximas clases");
    const data = await res.json();
    listaProximas.innerHTML = "";
    if (!data || data.length === 0) {
      listaProximas.innerHTML = '<li class="text-gray-500">No hay próximas clases</li>';
      return;
    }
    data.forEach(item => {
      const li = document.createElement("li");
      li.textContent = `${item.nombre} - ${item.fecha} ${item.hora}`;
      listaProximas.appendChild(li);
    });
  } catch (e) {
    console.error(e);
    listaProximas.innerHTML = '<li class="text-red-600">No se pudieron cargar las próximas clases</li>';
  }
}

function poblarFiltroTipos(clasesData) {
  const tipos = Array.from(new Set((clasesData || []).map(c => (c.tipo || '').toLowerCase()).filter(Boolean)));
  const current = filtroTipo.value;
  filtroTipo.innerHTML = '<option value="todas">Todas las clases</option>' + tipos.map(t => `<option value="${t}">${capitalize(t)}</option>`).join("");
  if (current && (current === 'todas' || tipos.includes(current))) {
    filtroTipo.value = current;
  }
}

function capitalize(s){ return s ? s.charAt(0).toUpperCase() + s.slice(1) : s; }

// Inicializar Flatpickr y cargar datos
document.addEventListener("DOMContentLoaded", function() {
  const params = new URLSearchParams(window.location.search);
  const fechaParam = params.get("fecha");

  let defaultDateOption = "today";
  if (fechaParam && /^\d{4}-\d{2}-\d{2}$/.test(fechaParam)) {
    defaultDateOption = fechaParam;
  }

  const fp = flatpickr("#calendario-inline", {
    inline: true,
    locale: "es",
    defaultDate: defaultDateOption,
    dateFormat: "Y-m-d",
    onChange: function(selectedDates, dateStr) {
      if (selectedDates.length > 0) {
        actualizarFecha(selectedDates[0]);
        cargarClases(dateStr, filtroTipo.value || "todas");
      }
    }
  });

  // Carga inicial
  const initialDate = (fp.selectedDates[0] || new Date());
  const yyyy = initialDate.getFullYear();
  const mm = String(initialDate.getMonth() + 1).padStart(2,'0');
  const dd = String(initialDate.getDate()).padStart(2,'0');
  const iso = `${yyyy}-${mm}-${dd}`;
  actualizarFecha(initialDate);
  cargarClases(iso, "todas");
  cargarProximas();
});

// Cambiar filtro
filtroTipo.addEventListener("change", () => {
  // Recalcular desde la fecha actual seleccionada en flatpickr
  const selected = document.querySelector('.flatpickr-day.selected');
  // Fallback: input hidden no tiene ISO, así que usamos flatpickr API indirectamente
  // Tomamos la fecha mostrada en subtitulo solo para UI; recargamos desde server según flatpickr
  const calendar = document.querySelector('#calendario-inline')._flatpickr;
  const date = calendar && calendar.selectedDates && calendar.selectedDates[0] ? calendar.selectedDates[0] : new Date();
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2,'0');
  const dd = String(date.getDate()).padStart(2,'0');
  const iso = `${yyyy}-${mm}-${dd}`;
  cargarClases(iso, filtroTipo.value || "todas");
});

async function reservarClase(claseId){
  try {
    btnConfirmar.disabled = true;
    btnConfirmar.textContent = "Reservando...";
    const body = new URLSearchParams();
    body.append("claseId", claseId);
    const resp = await fetch('/api/reservas', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString()
    });
    const data = await resp.json().catch(() => ({ success:false, message:'Error inesperado'}));
    if (!resp.ok){
      alert(data.message || 'No se pudo crear la reserva');
    } else if (data.requiresPayment){
      if (data.checkoutUrl){
        window.location.href = data.checkoutUrl;
      } else {
        alert(data.message || 'Esta clase requiere pago.');
      }
    } else if (!data.success){
      alert(data.message || 'No se pudo crear la reserva');
    } else {
      alert('✅ ' + data.message);
      modal.style.display = "none";
      // refrescar próximas y lista del día (por si cambia cupo)
      await cargarProximas();
      const calendar = document.querySelector('#calendario-inline')._flatpickr;
      const date = calendar && calendar.selectedDates && calendar.selectedDates[0] ? calendar.selectedDates[0] : new Date();
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2,'0');
      const dd = String(date.getDate()).padStart(2,'0');
      const iso = `${yyyy}-${mm}-${dd}`;
      await cargarClases(iso, filtroTipo.value || 'todas');
    }
  } catch (e){
    console.error(e);
    alert('❌ Error al crear la reserva');
  } finally {
    btnConfirmar.disabled = false;
    btnConfirmar.textContent = "Confirmar Reserva";
  }
}
