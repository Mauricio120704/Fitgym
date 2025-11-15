document.addEventListener('DOMContentLoaded', () => {
  const grid = document.getElementById('miembrosGrid');
  const searchInput = document.getElementById('searchInput');
  const overlay = document.getElementById('detalleOverlay');
  const closeDetalle = document.getElementById('closeDetalle');
  const btnCerrar = document.getElementById('btnCerrar');
  const btnMensaje = document.getElementById('btnMensaje');
  const detalleContenido = document.getElementById('detalleContenido');

  const fetchMiembros = async (q = '') => {
    const url = q ? `/api/comunidad/miembros?q=${encodeURIComponent(q)}` : '/api/comunidad/miembros';
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error('No se pudo obtener la lista de miembros');
    return res.json();
  };

  const renderCard = (m) => {
    const card = document.createElement('article');
    card.className = 'bg-white rounded-2xl border border-gray-200 p-6 shadow-sm flex flex-col items-center text-center';
    const initials = (m.iniciales || '??');
    const nombre = (m.nombreCompleto || 'Miembro');
    const descripcion = (m.descripcion && m.descripcion.trim().length > 0) ? m.descripcion.trim() : '';
    card.innerHTML = `
      <div class="h-16 w-16 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center font-bold text-lg mb-3">${initials}</div>
      <h3 class="text-[18px] font-semibold text-gray-900 mb-2">${nombre}</h3>
      ${descripcion ? `<p class=\"text-sm text-gray-600 leading-relaxed mb-3 line-clamp-3\">${descripcion}</p>` : ''}
      ${m.peso || m.altura ? `
        <div class=\"w-full text-sm text-gray-700 mb-4\">
          ${m.peso ? `<div><span class=\"text-gray-500\">Peso:</span> ${m.peso} kg</div>` : ''}
          ${m.altura ? `<div><span class=\"text-gray-500\">Altura:</span> ${m.altura} m</div>` : ''}
        </div>
      ` : ''}
      <button data-id="${m.id}" class="btn-conectar mt-auto bg-orange-500 hover:bg-orange-600 text-white font-semibold w-full py-2.5 rounded-lg flex items-center justify-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
        Conectar
      </button>
    `;
    return card;
  };

  const renderMiembros = (list) => {
    grid.innerHTML = '';
    if (!list || list.length === 0) {
      grid.innerHTML = '<p class="text-gray-600">No se encontraron miembros.</p>';
      return;
    }
    list.forEach(m => grid.appendChild(renderCard(m)));
  };

  const openModal = () => {
    overlay.classList.remove('hidden');
    overlay.classList.add('flex');
  };
  const closeModal = () => {
    overlay.classList.add('hidden');
    overlay.classList.remove('flex');
  };

  const loadDetalle = async (id) => {
    const res = await fetch(`/api/comunidad/miembros/${id}`, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error('No se pudo obtener el detalle');
    const d = await res.json();

    const initials = (d.iniciales || '??');
    const nombre = (d.nombreCompleto || 'Miembro');
    const badge = (d.especialidad || d.genero || 'Deportista');
    const bio = (d.bio || 'Entusiasta del entrenamiento. Me encanta compartir rutas y consejos.');
    const objetivos = d.objetivos || ['Resistencia', 'Maratón'];

    detalleContenido.innerHTML = `
      <div class="flex items-center gap-4 mb-3">
        <div class="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600 font-bold">${initials}</div>
        <div>
          <div class="flex items-center gap-2">
            <h3 class="text-xl font-bold text-gray-900">${nombre}</h3>
            <span class="inline-flex items-center gap-1 text-xs font-medium bg-gray-100 text-gray-700 px-2 py-1 rounded-full">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6l4 2"/></svg>
              ${badge}
            </span>
          </div>
          <p class="text-gray-600 mt-1">${bio}</p>
        </div>
      </div>
      <div class="grid grid-cols-3 gap-4 mt-4">
        <div class="border rounded-xl p-4 text-center"><div class="text-2xl font-bold text-gray-900">${d.entrenamientos ?? '208'}</div><div class="text-sm text-gray-500">Entrenamientos</div></div>
        <div class="border rounded-xl p-4 text-center"><div class="text-2xl font-bold text-gray-900">${d.logros ?? '23'}</div><div class="text-sm text-gray-500">Logros</div></div>
        <div class="border rounded-xl p-4 text-center"><div class="text-2xl font-bold text-gray-900">${d.conexiones ?? '12'}</div><div class="text-sm text-gray-500">Conexiones</div></div>
      </div>
      <div class="mt-5">
        <h4 class="text-sm font-semibold text-gray-800 mb-2">Objetivos</h4>
        <div class="flex flex-wrap gap-2">
          ${objetivos.map(o => `<span class=\"px-2.5 py-1 rounded-full bg-gray-100 text-gray-700 text-xs font-medium\">${o}</span>`).join('')}
        </div>
      </div>
    `;
    openModal();
  };

  grid.addEventListener('click', (e) => {
    const btn = e.target.closest('.btn-conectar');
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    loadDetalle(id).catch(err => alert(err.message));
  });

  searchInput.addEventListener('input', async (e) => {
    try {
      const list = await fetchMiembros(e.target.value.trim());
      renderMiembros(list);
    } catch (err) {
      console.error(err);
    }
  });

  [closeDetalle, btnCerrar].forEach(el => el.addEventListener('click', closeModal));
  overlay.addEventListener('click', (e) => { if (e.target === overlay) closeModal(); });
  btnMensaje.addEventListener('click', () => {
    // Placeholder para futura funcionalidad
    alert('Funcionalidad de mensajes próximamente.');
  });

  // Inicial
  fetchMiembros()
    .then(renderMiembros)
    .catch(err => {
      console.error(err);
      grid.innerHTML = '<p class="text-red-600">Error cargando la comunidad.</p>';
    });
});
