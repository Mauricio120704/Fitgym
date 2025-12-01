// Datos obtenidos del backend
let clasesData = [];

// Almacenar estados de expansión
const expandedStates = {};

// Paginación
let currentPage = 1;
const PAGE_SIZE = 20;

// Renderizar clases con filtro y paginación
function renderClases(filtroEstado = 'todas') {
    const clasesList = document.getElementById('clasesList');
    if (!clasesList) return;
    clasesList.innerHTML = '';

    let clasesFiltradas = clasesData;

    // Filtrar por estado
    if (filtroEstado !== 'todas') {
        clasesFiltradas = clasesFiltradas.filter(clase => {
            if (filtroEstado === 'proximas') return clase.estado === 'proxima';
            if (filtroEstado === 'pendientes') return clase.estado === 'pendiente';
            if (filtroEstado === 'calificadas') return clase.estado === 'calificada';
            return true;
        });
    }

    const totalItems = clasesFiltradas.length;
    const totalPages = totalItems === 0 ? 1 : Math.ceil(totalItems / PAGE_SIZE);

    // Ajustar página actual si se sale de rango
    if (currentPage > totalPages) {
        currentPage = totalPages;
    }
    if (currentPage < 1) {
        currentPage = 1;
    }

    const startIndex = (currentPage - 1) * PAGE_SIZE;
    const endIndex = startIndex + PAGE_SIZE;
    const pageItems = clasesFiltradas.slice(startIndex, endIndex);

    pageItems.forEach(clase => {
        const card = createClaseCard(clase);
        clasesList.appendChild(card);
    });

    renderPagination(totalItems, totalPages, filtroEstado);
}

function renderPagination(totalItems, totalPages, filtroEstado) {
    const container = document.getElementById('pagination');
    if (!container) return;

    container.innerHTML = '';

    if (totalItems === 0) {
        return;
    }

    const infoWrapper = document.createElement('div');
    infoWrapper.className = 'pagination-inner';

    const startItem = (currentPage - 1) * PAGE_SIZE + 1;
    const endItem = Math.min(currentPage * PAGE_SIZE, totalItems);

    const summary = document.createElement('span');
    summary.className = 'page-summary';
    summary.textContent = `Mostrando ${startItem}-${endItem} de ${totalItems} clases`;

    const controls = document.createElement('div');
    controls.className = 'page-controls';

    const prevBtn = document.createElement('button');
    prevBtn.type = 'button';
    prevBtn.className = 'page-btn';
    prevBtn.textContent = 'Anterior';
    prevBtn.disabled = currentPage <= 1;
    prevBtn.addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            renderClases(filtroEstado);
        }
    });

    const pageInfo = document.createElement('span');
    pageInfo.className = 'page-info';
    pageInfo.textContent = `Página ${currentPage} de ${totalPages}`;

    const nextBtn = document.createElement('button');
    nextBtn.type = 'button';
    nextBtn.className = 'page-btn';
    nextBtn.textContent = 'Siguiente';
    nextBtn.disabled = currentPage >= totalPages;
    nextBtn.addEventListener('click', () => {
        if (currentPage < totalPages) {
            currentPage++;
            renderClases(filtroEstado);
        }
    });

    controls.appendChild(prevBtn);
    controls.appendChild(pageInfo);
    controls.appendChild(nextBtn);

    infoWrapper.appendChild(summary);
    infoWrapper.appendChild(controls);

    container.appendChild(infoWrapper);
}

function createClaseCard(clase) {
    const card = document.createElement('div');
    card.className = 'clase-card';
    card.id = `clase-${clase.id}`;

    const isExpanded = expandedStates[clase.id] || false;

    let statusHTML = '';
    if (clase.estado === 'proxima') {
        statusHTML = '<span class="clase-status status-proxima">Próxima</span>';
    } else if (clase.estado === 'pendiente') {
        statusHTML = '<span class="clase-status status-pendiente">Pendiente</span>';
    } else if (clase.estado === 'calificada') {
        statusHTML = `<span class="clase-status status-rating"><span class="star-icon">★</span>${clase.rating}/5</span>`;
    }

    const imgSrc = clase.imagen || 'https://images.unsplash.com/photo-1558611848-73f7eb4001a1?q=80&w=600&auto=format&fit=crop';
    let contentHTML = `
        <img src="${imgSrc}" alt="${clase.titulo}" class="clase-image">
        <div class="clase-content">
            <div class="clase-header">
                <h3 class="clase-title">${clase.titulo}</h3>
                ${statusHTML}
            </div>
            <div class="clase-instructor">
                <div class="instructor-avatar"></div>
                <span class="instructor-name">${clase.instructor}</span>
            </div>
            <div class="clase-meta">
                <span class="meta-item">📅 ${clase.fechaTexto || ''}</span>
                <span class="meta-item">⏱️ ${clase.duracion || ''}</span>
            </div>
    `;

    if (clase.estado === 'pendiente') {
        contentHTML += `
            <p class="clase-message">${clase.mensaje || 'Esta clase ya pasó. Puedes calificarla ahora.'}</p>
            <div class="clase-actions"><button class="btn btn-primary btn-lg" onclick="abrirModalCalificar(${clase.id})"><span class="icon">⭐</span><span>Calificar Clase</span></button></div>
        `;
    } else if (clase.estado === 'proxima') {
        contentHTML += `
            <p class="clase-message">${clase.mensaje || 'Esta clase aún no ha ocurrido. Podrás calificarla después de asistir.'}</p>
        `;
    } else if (clase.estado === 'calificada') {
        const valInstructor = clase.ratingInstructor != null ? clase.ratingInstructor : '–';
        const valClase = clase.ratingInstalaciones != null ? clase.ratingInstalaciones : '–';
        const valAmbiente = clase.ratingMusica != null ? clase.ratingMusica : '–';
        const valExperiencia = clase.ratingDificultad != null ? clase.ratingDificultad : '–';

        contentHTML += `
            <div class="clase-review ${isExpanded ? 'expanded' : ''}">
                ${clase.review}
            </div>
            <button class="btn-ver-mas" onclick="toggleExpand(${clase.id})">
                <span>${isExpanded ? '▲' : '▼'}</span>
                <span>${isExpanded ? 'Ver menos' : 'Ver más'}</span>
            </button>
            <div class="clase-ratings ${isExpanded ? 'expanded' : 'collapsed'}">
                <div class="rating-item">
                    <span class="rating-label">Instructor:</span>
                    <span class="rating-value">★ ${valInstructor}</span>
                </div>
                <div class="rating-item">
                    <span class="rating-label">Clase / Rutina:</span>
                    <span class="rating-value">★ ${valClase}</span>
                </div>
                <div class="rating-item">
                    <span class="rating-label">Ambiente y Entorno:</span>
                    <span class="rating-value">★ ${valAmbiente}</span>
                </div>
                <div class="rating-item">
                    <span class="rating-label">Experiencia del Usuario:</span>
                    <span class="rating-value">★ ${valExperiencia}</span>
                </div>
            </div>
            <div class="clase-actions right">
                <button class="btn btn-secondary" onclick="editarCalificacion(${clase.id})">✏️ Editar</button>
                <button class="btn btn-delete" onclick="eliminarCalificacion(${clase.id})">🗑️ Eliminar</button>
            </div>
        `;
    }

    contentHTML += '</div>';
    card.innerHTML = contentHTML;

    return card;
}

function toggleExpand(claseId) {
    expandedStates[claseId] = !expandedStates[claseId];
    const estadoSelect = document.getElementById('estado');
    const estado = estadoSelect ? estadoSelect.value : 'todas';
    renderClases(estado);
}

function abrirModalCalificar(claseId) {
    const modal = document.getElementById('modalCalificar');
    const modalBody = document.getElementById('modalBody');
    const modalSubtitle = document.getElementById('modalSubtitle');
    const modalInfo = document.getElementById('modalInfo');
    const clase = clasesData.find(c => c.id === claseId);
    if (!clase) { return; }
    
    modalSubtitle.textContent = `${clase.titulo} con ${clase.instructor}`;
    modalInfo.textContent = `${clase.fechaTexto || ''} - ${clase.duracion || ''}`;
    
    modalBody.innerHTML = `
        <form class="rating-form" onsubmit="guardarCalificacion(event, ${claseId})">
            <div class="form-group">
                <label>Calificación General</label>
                <div class="star-rating" id="rating-general">
                    ${[1,2,3,4,5].map(i => `<span class="star-number">${i}</span><span class="star" onclick="setRating('general', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Califica aspectos específicos</label>
            </div>
            <div class="form-group">
                <label>Instructor</label>
                <div class="star-rating" id="rating-instructor">
                    ${[1,2,3,4,5].map(i => `<span class="star-number">${i}</span><span class="star" onclick="setRating('instructor', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Clase / Rutina</label>
                <div class="star-rating" id="rating-instalaciones">
                    ${[1,2,3,4,5].map(i => `<span class="star-number">${i}</span><span class="star" onclick="setRating('instalaciones', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Ambiente y Entorno</label>
                <div class="star-rating" id="rating-musica">
                    ${[1,2,3,4,5].map(i => `<span class="star-number">${i}</span><span class="star" onclick="setRating('musica', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Experiencia del Usuario</label>
                <div class="star-rating" id="rating-dificultad">
                    ${[1,2,3,4,5].map(i => `<span class="star-number">${i}</span><span class="star" onclick="setRating('dificultad', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Comentarios (opcional)</label>
                <textarea id="comentarios" maxlength="500" placeholder="Comparte tu experiencia con esta clase... ¿Qué te gustó? ¿Qué se puede mejorar?" oninput="updateCharCount()">${clase.review || ''}</textarea>
                <div class="char-count" id="charCount">0/500</div>
            </div>
        </form>
        <div class="modal-actions">
            <button type="button" class="btn-cancel" onclick="cerrarModal()">Cancelar</button>
            <button type="button" class="btn-submit" onclick="submitFromModal(${claseId})">Enviar Calificación</button>
        </div>
    `;
    
    // Si ya está calificada, prellenar estrellas y contador de caracteres
    if (clase.estado === 'calificada') {
        if (clase.rating != null) setRating('general', clase.rating);
        if (clase.ratingInstructor != null) setRating('instructor', clase.ratingInstructor);
        if (clase.ratingInstalaciones != null) setRating('instalaciones', clase.ratingInstalaciones);
        if (clase.ratingMusica != null) setRating('musica', clase.ratingMusica);
        if (clase.ratingDificultad != null) setRating('dificultad', clase.ratingDificultad);
        updateCharCount();
    }

    modal.style.display = 'block';
}

const ratings = {
    general: 0,
    instructor: 0,
    instalaciones: 0,
    musica: 0,
    dificultad: 0
};

function setRating(tipo, valor) {
    ratings[tipo] = valor;
    const container = document.getElementById(`rating-${tipo}`);
    const stars = container.querySelectorAll('.star');
    stars.forEach((star, index) => {
        if (index < valor) {
            star.textContent = '★';
            star.classList.add('active');
        } else {
            star.textContent = '☆';
            star.classList.remove('active');
        }
    });
}

function guardarCalificacion(event, claseId) {
    event.preventDefault();
    
    const comentarios = document.getElementById('comentarios').value;
    const clase = clasesData.find(c => c.id === claseId);
    if (!clase) return;
    const csrf = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    fetch('/api/clases/calificaciones', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', [csrfHeader]: csrf },
        body: JSON.stringify({
            reservaId: clase.reservaId,
            claseId: clase.claseId,
            deportistaId: window.__deportistaId || 0,
            ratingGeneral: ratings.general,
            ratingInstructor: ratings.instructor,
            ratingInstalaciones: ratings.instalaciones,
            ratingMusica: ratings.musica,
            ratingDificultad: ratings.dificultad,
            comentario: comentarios
        })
    }).then(r => r.ok ? r.json() : Promise.reject(r)).then(() => {
        cerrarModal();
        cargarDesdeApi();
    }).catch(() => { alert('Error guardando la calificación'); });
}

function editarCalificacion(claseId) {
    abrirModalCalificar(claseId);
    // Aquí podrías pre-llenar el formulario con los valores existentes
}

function eliminarCalificacion(claseId) {
    if (confirm('¿Estás seguro de que deseas eliminar esta calificación?')) {
        const clase = clasesData.find(c => c.id === claseId);
        if (!clase) return;
        const csrf = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
        fetch(`/api/clases/calificaciones/by-reserva/${clase.reservaId}`, {
            method: 'DELETE',
            headers: { [csrfHeader]: csrf }
        }).then(r => {
            // Consideramos 200 OK y 404 (ya no existe) como estados válidos
            if (!r.ok && r.status !== 404) {
                throw new Error('HTTP ' + r.status);
            }
        }).then(() => {
            // Recargar datos para que la clase pase a "Pendiente" (sin calificación)
            cargarDesdeApi();
        }).catch(() => { alert('Error eliminando la calificación'); });
    }
}

function cerrarModal() {
    document.getElementById('modalCalificar').style.display = 'none';
    // Resetear ratings
    Object.keys(ratings).forEach(key => ratings[key] = 0);
    // Resetear contador
    const charCount = document.getElementById('charCount');
    if (charCount) charCount.textContent = '0/500';
}

function updateCharCount() {
    const textarea = document.getElementById('comentarios');
    const charCount = document.getElementById('charCount');
    if (textarea && charCount) {
        charCount.textContent = `${textarea.value.length}/500`;
    }
}

function submitFromModal(claseId) {
    const comentarios = document.getElementById('comentarios').value;
    const clase = clasesData.find(c => c.id === claseId);
    if (!clase) return;
    const csrf = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    fetch('/api/clases/calificaciones', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', [csrfHeader]: csrf },
        body: JSON.stringify({
            reservaId: clase.reservaId,
            claseId: clase.claseId,
            deportistaId: window.__deportistaId || 0,
            ratingGeneral: ratings.general,
            ratingInstructor: ratings.instructor,
            ratingInstalaciones: ratings.instalaciones,
            ratingMusica: ratings.musica,
            ratingDificultad: ratings.dificultad,
            comentario: comentarios
        })
    }).then(r => r.ok ? r.json() : Promise.reject(r)).then(() => {
        cerrarModal();
        cargarDesdeApi();
    }).catch(() => { alert('Error guardando la calificación'); });
}

// Event Listeners
function cargarDesdeApi(){
    const params = new URLSearchParams(location.search);
    const depIdParam = params.get('deportistaId');
    const depId = Number(depIdParam || window.__deportistaId);
    const url = isFinite(depId) && depId > 0
        ? `/api/deportista/mis-clases?deportistaId=${depId}`
        : `/api/deportista/mis-clases`;
    fetch(url)
        .then(r => r.json())
        .then(data => {
            console.log('=== DATOS RECIBIDOS DEL API ===');
            console.log('Próximas:', data.proximas);
            console.log('Pendientes:', data.pendientes);
            console.log('Calificadas:', data.calificadas);
            
            // Unificar en una sola lista con bandera estado para reusar el renderer
            clasesData = []; let id=1;
            data.proximas.forEach(c => clasesData.push({ id:id++, ...c }));
            data.pendientes.forEach(c => clasesData.push({ id:id++, ...c }));
            data.calificadas.forEach(c => clasesData.push({ id:id++, ...c }));
            
            console.log('Total clases cargadas:', clasesData.length);
            console.log('Estados:', clasesData.map(c => c.estado));
            // Resetear a la primera página al recargar datos
            currentPage = 1;
            renderClases();
        }).catch(() => { clasesData=[]; renderClases(); });
}

document.addEventListener('DOMContentLoaded', () => {
    cargarDesdeApi();

    // Filtros
    const estadoSelect = document.getElementById('estado');

    if (estadoSelect) {
        estadoSelect.addEventListener('change', (e) => {
            // Cambiar de filtro siempre vuelve a la página 1
            currentPage = 1;
            renderClases(e.target.value);
        });
    }

    // Cerrar modal solo con el botón X o Cancelar
    const closeBtn = document.querySelector('.modal-close');
    if (closeBtn) {
        closeBtn.addEventListener('click', cerrarModal);
    }

    // Cerrar modal con tecla ESC
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' || e.key === 'Esc') {
            const modal = document.getElementById('modalCalificar');
            if (modal && modal.style.display === 'block') {
                cerrarModal();
            }
        }
    });
});
