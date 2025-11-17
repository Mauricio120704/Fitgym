document.addEventListener('DOMContentLoaded', () => {
  const grid = document.getElementById('miembrosGrid');
  const searchInput = document.getElementById('searchInput');
  const overlay = document.getElementById('detalleOverlay');
  const closeDetalle = document.getElementById('closeDetalle');
  const btnCerrar = document.getElementById('btnCerrar');
  const btnMensaje = document.getElementById('btnMensaje');
  const detalleContenido = document.getElementById('detalleContenido');
  const foroForm = document.getElementById('foroNuevoPostForm');
  const foroTextarea = document.getElementById('foroNuevoPost');
  const foroCounter = document.getElementById('foroContadorCaract');
  const foroPostsContainer = document.getElementById('foroPostsContainer');
  const foroEmptyState = document.getElementById('foroEmptyState');
  const tabPerfilesBtn = document.getElementById('tabPerfilesBtn');
  const tabForoBtn = document.getElementById('tabForoBtn');
  const tabPerfiles = document.getElementById('tabPerfiles');
  const tabForo = document.getElementById('tabForo');

  const setActiveTab = (tab) => {
    if (!tabPerfiles || !tabForo || !tabPerfilesBtn || !tabForoBtn) return;
    const isPerfiles = tab === 'perfiles';

    tabPerfiles.classList.toggle('hidden', !isPerfiles);
    tabForo.classList.toggle('hidden', isPerfiles);

    if (isPerfiles) {
      tabPerfilesBtn.classList.add('border-orange-500', 'text-orange-600');
      tabPerfilesBtn.classList.remove('border-transparent', 'text-gray-500');
      tabForoBtn.classList.add('border-transparent', 'text-gray-500');
      tabForoBtn.classList.remove('border-orange-500', 'text-orange-600');
    } else {
      tabForoBtn.classList.add('border-orange-500', 'text-orange-600');
      tabForoBtn.classList.remove('border-transparent', 'text-gray-500');
      tabPerfilesBtn.classList.add('border-transparent', 'text-gray-500');
      tabPerfilesBtn.classList.remove('border-orange-500', 'text-orange-600');
    }
  };

  if (tabPerfilesBtn && tabForoBtn) {
    tabPerfilesBtn.addEventListener('click', () => setActiveTab('perfiles'));
    tabForoBtn.addEventListener('click', () => setActiveTab('foro'));
    setActiveTab('perfiles');
  }

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

  const updateForoCharCount = () => {
    if (!foroTextarea || !foroCounter) return;
    const length = foroTextarea.value.length;
    foroCounter.textContent = `${length}/1000`;
  };

  const renderRespuesta = (r) => {
    const wrapper = document.createElement('div');
    wrapper.className = 'flex gap-2';
    const iniciales = r.autorIniciales || '??';
    const nombre = r.autorNombre || 'Miembro';
    const fecha = r.creadoEnTexto || '';
    wrapper.innerHTML = `
      <div class="h-7 w-7 rounded-full bg-gray-100 text-gray-600 flex items-center justify-center text-[11px] font-semibold">${iniciales}</div>
      <div class="flex-1">
        <div class="flex items-center gap-2">
          <span class="text-xs font-semibold text-gray-800">${nombre}</span>
          ${fecha ? `<span class="text-[10px] text-gray-400">${fecha}</span>` : ''}
        </div>
        <p class="text-xs text-gray-700 whitespace-pre-line">${r.contenido || ''}</p>
      </div>
    `;
    return wrapper;
  };

  const renderPost = (post) => {
    const card = document.createElement('article');
    card.className = 'bg-white rounded-2xl border border-gray-200 p-5 shadow-sm';
    const iniciales = post.autorIniciales || '??';
    const nombre = post.autorNombre || 'Miembro';
    const fecha = post.creadoEnTexto || '';
    const contenido = post.contenido || '';
    const respuestas = Array.isArray(post.respuestas) ? post.respuestas : [];

    card.innerHTML = `
      <div class="flex items-start gap-3">
        <div class="h-10 w-10 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center font-semibold text-sm">${iniciales}</div>
        <div class="flex-1">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-sm font-semibold text-gray-900">${nombre}</h3>
              ${fecha ? `<p class="text-xs text-gray-400">${fecha}</p>` : ''}
            </div>
          </div>
          <p class="mt-3 text-sm text-gray-800 whitespace-pre-line">${contenido}</p>
          <div class="mt-4 border-t border-gray-100 pt-3 space-y-3" data-respuestas></div>
          <form class="mt-3 flex gap-2 items-start" data-responder-form data-post-id="${post.id}">
            <textarea rows="2" class="flex-1 border border-gray-300 rounded-xl px-3 py-2 text-xs resize-none focus:outline-none focus:ring-1 focus:ring-orange-400" placeholder="Responder..."></textarea>
            <button type="submit" class="px-3 py-2 rounded-lg bg-gray-900 text-white text-xs font-semibold hover:bg-gray-800 disabled:opacity-60 disabled:cursor-not-allowed">Enviar</button>
          </form>
        </div>
      </div>
    `;

    const contRespuestas = card.querySelector('[data-respuestas]');
    respuestas.forEach(r => contRespuestas.appendChild(renderRespuesta(r)));

    const form = card.querySelector('[data-responder-form]');
    const textarea = form.querySelector('textarea');
    const button = form.querySelector('button[type="submit"]');

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!textarea.value.trim()) return;
      button.disabled = true;
      button.textContent = 'Enviando...';
      try {
        const body = new URLSearchParams();
        body.append('contenido', textarea.value.trim());
        const res = await fetch(`/api/comunidad/foro/posts/${post.id}/respuestas`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: body.toString()
        });
        const data = await res.json().catch(() => ({}));
        if (!res.ok || !data.success) {
          alert(data.message || 'No se pudo enviar la respuesta');
        } else if (data.respuesta) {
          contRespuestas.appendChild(renderRespuesta(data.respuesta));
          textarea.value = '';
        }
      } catch (err) {
        console.error(err);
        alert('No se pudo enviar la respuesta');
      } finally {
        button.disabled = false;
        button.textContent = 'Enviar';
      }
    });

    return card;
  };

  const renderPosts = (list) => {
    if (!foroPostsContainer) return;
    foroPostsContainer.innerHTML = '';
    if (!list || list.length === 0) {
      if (foroEmptyState) foroEmptyState.classList.remove('hidden');
      return;
    }
    if (foroEmptyState) foroEmptyState.classList.add('hidden');
    list.forEach(p => foroPostsContainer.appendChild(renderPost(p)));
  };

  const loadForo = async () => {
    if (!foroPostsContainer) return;
    try {
      const res = await fetch('/api/comunidad/foro/posts', { headers: { 'Accept': 'application/json' } });
      if (!res.ok) throw new Error('No se pudo cargar el foro');
      const data = await res.json();
      renderPosts(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      foroPostsContainer.innerHTML = '<p class="text-red-600 text-sm">No se pudo cargar el foro de la comunidad.</p>';
      if (foroEmptyState) foroEmptyState.classList.add('hidden');
    }
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

  if (searchInput) {
    searchInput.addEventListener('input', async (e) => {
      try {
        const list = await fetchMiembros(e.target.value.trim());
        renderMiembros(list);
      } catch (err) {
        console.error(err);
      }
    });
  }

  [closeDetalle, btnCerrar].forEach(el => el.addEventListener('click', closeModal));
  overlay.addEventListener('click', (e) => { if (e.target === overlay) closeModal(); });
  btnMensaje.addEventListener('click', () => {
    // Placeholder para futura funcionalidad
    alert('Funcionalidad de mensajes prximamente.');
  });

  if (foroTextarea && foroCounter) {
    updateForoCharCount();
    foroTextarea.addEventListener('input', updateForoCharCount);
  }

  if (foroForm && foroTextarea && foroPostsContainer) {
    foroForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const contenido = foroTextarea.value.trim();
      if (!contenido) return;
      const submitBtn = foroForm.querySelector('button[type="submit"]');
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Publicando...';
      }
      try {
        const body = new URLSearchParams();
        body.append('contenido', contenido);
        const res = await fetch('/api/comunidad/foro/posts', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: body.toString()
        });
        const data = await res.json().catch(() => ({}));
        if (!res.ok || !data.success) {
          alert(data.message || 'No se pudo publicar el mensaje');
        } else if (data.post) {
          foroTextarea.value = '';
          updateForoCharCount();
          if (foroEmptyState) foroEmptyState.classList.add('hidden');
          const card = renderPost(data.post);
          foroPostsContainer.prepend(card);
        }
      } catch (err) {
        console.error(err);
        alert('No se pudo publicar el mensaje');
      } finally {
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.textContent = 'Publicar';
        }
      }
    });
  }

  // Inicial
  fetchMiembros()
    .then(renderMiembros)
    .catch(err => {
      console.error(err);
      grid.innerHTML = '<p class="text-red-600">Error cargando la comunidad.</p>';
    });

  loadForo();
});
