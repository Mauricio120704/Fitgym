// Datos obtenidos del backend
let clasesData = [];

// Almacenar estados de expansión
const expandedStates = {};

// Renderizar clases
function renderClases(filtroEstado = 'todas', filtroInstructor = 'todos') {
    const clasesList = document.getElementById('clasesList');
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

    // Filtrar por instructor
    if (filtroInstructor !== 'todos') {
        clasesFiltradas = clasesFiltradas.filter(clase => {
            if (filtroInstructor === 'carlos') return clase.instructor === 'Carlos Ruiz';
            if (filtroInstructor === 'maria') return clase.instructor === 'María González';
            if (filtroInstructor === 'ana') return clase.instructor === 'Ana Martínez';
            return true;
        });
    }

    clasesFiltradas.forEach(clase => {
        const card = createClaseCard(clase);
        clasesList.appendChild(card);
    });
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
                    <span class="rating-value">⭐ ${clase.ratingInstructor}</span>
                </div>
                <div class="rating-item">
                    <span class="rating-label">Instalaciones:</span>
                    <span class="rating-value">⭐ ${clase.ratingInstalaciones}</span>
                </div>
                <div class="rating-item">
                    <span class="rating-label">Música:</span>
                    <span class="rating-value">⭐ ${clase.ratingMusica}</span>
                </div>
                <div class="rating-item">
                    <span class="rating-label">Dificultad:</span>
                    <span class="rating-value">⭐ ${clase.ratingDificultad}</span>
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
    renderClases(
        document.getElementById('estado').value,
        document.getElementById('instructor').value
    );
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
                    ${[1,2,3,4,5].map(i => `<span class="star" onclick="setRating('general', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Califica aspectos específicos</label>
            </div>
            <div class="form-group">
                <label>Instructor</label>
                <div class="star-rating" id="rating-instructor">
                    ${[1,2,3,4,5].map(i => `<span class="star" onclick="setRating('instructor', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Instalaciones</label>
                <div class="star-rating" id="rating-instalaciones">
                    ${[1,2,3,4,5].map(i => `<span class="star" onclick="setRating('instalaciones', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Música</label>
                <div class="star-rating" id="rating-musica">
                    ${[1,2,3,4,5].map(i => `<span class="star" onclick="setRating('musica', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Dificultad</label>
                <div class="star-rating" id="rating-dificultad">
                    ${[1,2,3,4,5].map(i => `<span class="star" onclick="setRating('dificultad', ${i})">☆</span>`).join('')}
                </div>
            </div>
            <div class="form-group">
                <label>Comentarios (opcional)</label>
                <textarea id="comentarios" maxlength="500" placeholder="Comparte tu experiencia con esta clase... ¿Qué te gustó? ¿Qué se puede mejorar?" oninput="updateCharCount()"></textarea>
                <div class="char-count" id="charCount">0/500</div>
            </div>
        </form>
        <div class="modal-actions">
            <button type="button" class="btn-cancel" onclick="cerrarModal()">Cancelar</button>
            <button type="button" class="btn-submit" onclick="submitFromModal(${claseId})">Enviar Calificación</button>
        </div>
    `;
    
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
        }).then(r => r.ok ? r.json() : Promise.reject(r)).then(() => {
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
            
            renderClases();
        }).catch(() => { clasesData=[]; renderClases(); });
}

document.addEventListener('DOMContentLoaded', () => {
    cargarDesdeApi();

    // Filtros
    document.getElementById('estado').addEventListener('change', (e) => {
        renderClases(e.target.value, document.getElementById('instructor').value);
    });

    document.getElementById('instructor').addEventListener('change', (e) => {
        renderClases(document.getElementById('estado').value, e.target.value);
    });

    // Cerrar modal
    document.querySelector('.modal-close').addEventListener('click', cerrarModal);
    
    window.addEventListener('click', (e) => {
        const modal = document.getElementById('modalCalificar');
        if (e.target === modal) {
            cerrarModal();
        }
    });
});
