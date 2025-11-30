// Datos simulados (mock data)
let requests = [];
let selectedRequestId = null;

// Referencias al DOM
const tableBody = document.getElementById('requests-table-body');

async function cargarSolicitudes() {
	try {
		const response = await fetch('/api/admin/suspensiones/pendientes');
		if (!response.ok) {
			console.error('Error al cargar solicitudes de suspensión', response.statusText);
			return;
		}
		requests = await response.json();
		renderTable();
		if (requests.length > 0) {
			selectRequest(requests[0].id);
		}
	} catch (error) {
		console.error('Error de red al cargar solicitudes de suspensión', error);
	}
}

// Función para renderizar la tabla
function renderTable() {
    tableBody.innerHTML = '';
    requests.forEach((req, index) => {
        const row = document.createElement('tr');
        row.className = 'request-row';
        row.id = `row-${req.id}`;
        row.innerHTML = `
            <td><strong>${req.name}</strong></td>
            <td>${req.reasonType}</td>
            <td>${req.dates}</td>
            <td>${req.days}</td>
            <td><span class="badge">${req.status}</span></td>
            <td><button class="btn-sm" onclick="selectRequest(${req.id})"><i class="far fa-eye"></i> Ver Detalle</button></td>
        `;
        // Añadir evento click a toda la fila también para mejor UX
        row.addEventListener('click', () => selectRequest(req.id));
        tableBody.appendChild(row);
    });
}

// Función para seleccionar y mostrar detalles
function selectRequest(id) {
    const req = requests.find(r => r.id === id);
    if (!req) {
        return;
    }
    selectedRequestId = id;
    
    // Actualizar estilo visual de la fila seleccionada
    document.querySelectorAll('.request-row').forEach(row => row.classList.remove('active'));
    const activeRow = document.getElementById(`row-${id}`);
    if(activeRow) activeRow.classList.add('active');

    // Actualizar panel de detalle
    document.getElementById('detail-name').textContent = req.name;
    document.getElementById('detail-id').textContent = req.userId;
    document.getElementById('detail-plan').textContent = req.plan;
    document.getElementById('detail-expire').textContent = req.expire;
    
    document.getElementById('detail-reason').textContent = req.reasonDetail;
    document.getElementById('detail-start').textContent = req.start;
    document.getElementById('detail-end').textContent = req.end;
    document.getElementById('detail-days').textContent = req.days;
    document.getElementById('detail-status').textContent = req.status || 'Pendiente de Aprobación';
    
    // Limpiar textarea
    document.getElementById('notes').value = '';
}

async function aprobarSolicitud() {
    if (!selectedRequestId) {
        return;
    }
    try {
        const response = await fetch(`/api/admin/suspensiones/${selectedRequestId}/aprobar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            console.error('Error al aprobar la solicitud de suspensión');
            return;
        }
        requests = requests.filter(r => r.id !== selectedRequestId);
        selectedRequestId = null;
        renderTable();
        if (requests.length > 0) {
            selectRequest(requests[0].id);
        } else {
            limpiarDetalle();
        }
    } catch (error) {
        console.error('Error de red al aprobar la solicitud de suspensión', error);
    }
}

async function rechazarSolicitud() {
    if (!selectedRequestId) {
        return;
    }
    const notes = document.getElementById('notes') ? document.getElementById('notes').value : '';
    try {
        const response = await fetch(`/api/admin/suspensiones/${selectedRequestId}/rechazar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nota: notes })
        });
        if (!response.ok) {
            console.error('Error al rechazar la solicitud de suspensión');
            return;
        }
        requests = requests.filter(r => r.id !== selectedRequestId);
        selectedRequestId = null;
        renderTable();
        if (requests.length > 0) {
            selectRequest(requests[0].id);
        } else {
            limpiarDetalle();
        }
    } catch (error) {
        console.error('Error de red al rechazar la solicitud de suspensión', error);
    }
}

function limpiarDetalle() {
    document.getElementById('detail-name').textContent = 'Cargando...';
    document.getElementById('detail-id').textContent = '---';
    document.getElementById('detail-plan').textContent = '---';
    document.getElementById('detail-expire').textContent = '---';
    document.getElementById('detail-reason').textContent = '---';
    document.getElementById('detail-start').textContent = '---';
    document.getElementById('detail-end').textContent = '---';
    document.getElementById('detail-days').textContent = '0';
    document.getElementById('detail-status').textContent = 'Pendiente de Aprobación';
    if (document.getElementById('notes')) {
        document.getElementById('notes').value = '';
    }
}

// Inicializar
document.addEventListener('DOMContentLoaded', () => {
    cargarSolicitudes();
    const approveBtn = document.querySelector('.action-buttons .btn-primary');
    const rejectBtn = document.querySelector('.action-buttons .btn-outline');
    if (approveBtn) {
        approveBtn.addEventListener('click', aprobarSolicitud);
    }
    if (rejectBtn) {
        rejectBtn.addEventListener('click', rechazarSolicitud);
    }
    // Seleccionar el primero por defecto
});