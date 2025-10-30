// JavaScript para gestión de asistencias

function buscarSocio() {
    const dni = document.getElementById('dniInput').value.trim();
    if (!dni) {
        mostrarError('Por favor ingrese un DNI');
        return;
    }

    // Limpiar resultados anteriores
    const mensajeError = document.getElementById('mensajeError');
    if (mensajeError) {
        mensajeError.classList.add('hidden');
    }

    // Mostrar carga
    const resultadoDiv = document.getElementById('resultado');
    if (resultadoDiv) {
        resultadoDiv.classList.add('hidden');
    }

    // Realizar la búsqueda
    fetch(`/asistencia/buscar?dni=${encodeURIComponent(dni)}`)
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.json();
        })
        .then(data => {
            if (resultadoDiv) {
                resultadoDiv.classList.remove('hidden');
            }

            const mensajeError = document.getElementById('mensajeError');
            if (mensajeError) {
                mensajeError.classList.add('hidden');
            }

            // Actualizar datos en la interfaz
            document.getElementById('nombreCompleto').textContent = data.nombreCompleto || 'No disponible';
            document.getElementById('tipoMembresia').textContent = data.tipoMembresia || 'Deportista';

            // Configurar la imagen
            const fotoUrl = data.fotoUrl || 'https://ui-avatars.com/api/?name=Usuario&background=random';
            document.getElementById('fotoSocio').src = fotoUrl;

            // Configurar el estado de la membresía
            const estadoMembresia = document.getElementById('estadoMembresia');
            estadoMembresia.textContent = data.estadoMembresia || 'Desconocido';

            if (data.estadoMembresia === 'Activa') {
                estadoMembresia.className = 'status-badge status-active';
            } else {
                estadoMembresia.className = 'status-badge status-inactive';
            }

            // Configurar el botón de registro
            const btnRegistrar = document.getElementById('btnRegistrar');
            if (data.estadoMembresia !== 'Activa') {
                btnRegistrar.disabled = true;
                btnRegistrar.className = 'w-full py-2 px-4 rounded-lg text-white bg-gray-400 cursor-not-allowed';
                btnRegistrar.innerHTML = 'Membresía inactiva';
            } else if (data.tieneCheckinActivo) {
                // Mostrar botón de salida
                btnRegistrar.disabled = false;
                btnRegistrar.className = 'w-full py-2 px-4 rounded-lg text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500';
                btnRegistrar.onclick = () => registrarAsistencia(dni);
                btnRegistrar.innerHTML = 'Registrar Salida';

                // Mostrar información del ingreso
                const fechaIngreso = new Date(data.fechaIngreso);
                const opciones = {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit'
                };
                document.getElementById('infoIngreso').innerHTML = `
                    <div class="mt-4 p-3 bg-blue-50 rounded-lg">
                        <p class="text-sm text-blue-700">
                            <span class="font-medium">Ingreso registrado:</span>
                            ${fechaIngreso.toLocaleString('es-PE', opciones)}
                        </p>
                    </div>
                `;
            } else {
                // Mostrar botón de ingreso
                btnRegistrar.disabled = false;
                btnRegistrar.className = 'w-full py-2 px-4 rounded-lg text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500';
                btnRegistrar.onclick = () => registrarAsistencia(dni);
                btnRegistrar.innerHTML = 'Registrar Ingreso';
                document.getElementById('infoIngreso').innerHTML = '';
            }

            // Actualizar el historial
            actualizarHistorial();
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarError('No se encontró un deportista con el DNI proporcionado');
            if (resultadoDiv) {
                resultadoDiv.classList.add('hidden');
            }
        });
}

function registrarAsistencia(dni) {
    const btnRegistrar = document.getElementById('btnRegistrar');
    const originalText = btnRegistrar.innerHTML;
    const esSalida = btnRegistrar.textContent.trim() === 'Registrar Salida';

    // Deshabilitar el botón y mostrar carga
    btnRegistrar.disabled = true;
    btnRegistrar.innerHTML = '<svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white inline-block" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg> ' + (esSalida ? 'Registrando salida...' : 'Registrando ingreso...');

    fetch('/asistencia/registrar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `dni=${encodeURIComponent(dni)}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            throw new Error(data.error);
        }

        // Mostrar mensaje de éxito
        const tipoMensaje = data.tipo === 'salida' ? 'info' : 'success';
        let mensaje = data.mensaje;

        if (data.tiempoEstadia) {
            mensaje += `<br>Tiempo de estadía: ${data.tiempoEstadia}`;
        }

        mostrarMensaje(mensaje, tipoMensaje);

        // Limpiar el formulario y el resultado actual
        const dniInput = document.getElementById('dniInput');
        dniInput.value = '';
        document.getElementById('resultado').classList.add('hidden');
        document.getElementById('infoIngreso').innerHTML = '';

        // Actualizar el historial
        actualizarHistorial();

        // Resetear el formulario
        document.getElementById('mensajeError').classList.add('hidden');
        dniInput.focus();
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarMensaje('Error: ' + error.message, 'error');

        // Restaurar el botón
        btnRegistrar.disabled = false;
        btnRegistrar.innerHTML = esSalida ? 'Registrar Salida' : 'Registrar Ingreso';
        btnRegistrar.onclick = () => registrarAsistencia(dni);
    });
}

function mostrarError(mensaje) {
    const mensajeError = document.getElementById('mensajeError');
    if (mensajeError) {
        mensajeError.textContent = mensaje;
        mensajeError.classList.remove('hidden');
    }
}

function mostrarMensaje(mensaje, tipo = 'info') {
    const mensajeDiv = document.createElement('div');
    let clases = 'p-4 mb-4 rounded-lg fixed top-4 right-4 z-50 min-w-80 shadow-lg ';

    if (tipo === 'error') {
        clases += 'bg-red-100 text-red-700 border border-red-300';
    } else if (tipo === 'success') {
        clases += 'bg-green-100 text-green-700 border border-green-300';
    } else {
        clases += 'bg-blue-100 text-blue-700 border border-blue-300';
    }

    mensajeDiv.className = clases;
    mensajeDiv.innerHTML = `<div class="flex items-center justify-between"><div>${mensaje}</div><button onclick="this.parentElement.parentElement.remove()" class="ml-4 text-lg">&times;</button></div>`;
    document.body.appendChild(mensajeDiv);

    // Auto-eliminar después de 5 segundos
    setTimeout(() => mensajeDiv.remove(), 5000);
}

function actualizarHistorial() {
    const historialDiv = document.getElementById('historialAsistencias');

    // Mostrar mensaje de carga
    historialDiv.innerHTML = `
        <tr>
            <td colspan="3" class="py-4 text-center text-gray-500">
                <div class="flex justify-center items-center">
                    <svg class="animate-spin h-5 w-5 text-blue-600 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <span>Cargando historial...</span>
                </div>
            </td>
        </tr>
    `;

    // Llamada a la API para obtener el historial
    fetch('/asistencia/recientes')
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cargar el historial');
            }
            return response.json();
        })
        .then(data => {
            if (data && data.length > 0) {
                const rows = data.map(asistencia => {
                    const fecha = new Date(asistencia.fechaHoraIngreso);
                    const fechaFormateada = fecha.toLocaleDateString('es-PE', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                    });

                    const opciones = {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                    };

                    return `
                        <tr class="hover:bg-gray-50">
                            <td class="py-2 px-4 border-b border-gray-200">${asistencia.nombreCompleto || 'N/A'}</td>
                            <td class="py-2 px-4 border-b border-gray-200">${asistencia.dni || 'N/A'}</td>
                            <td class="py-2 px-4 border-b border-gray-200 whitespace-nowrap">
                                <div>${fechaFormateada}</div>
                                ${asistencia.fechaHoraSalida ?
                                    `<div class="text-xs text-gray-500">Salida: ${new Date(asistencia.fechaHoraSalida).toLocaleString('es-PE', opciones)}</div>
                                    <div class="text-xs text-green-600 font-medium">Se retiró</div>` :
                                    '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">Dentro</span>'
                                }
                            </td>
                        </tr>`;
                }).join('');

                historialDiv.innerHTML = rows;
            } else {
                historialDiv.innerHTML = `
                    <tr>
                        <td colspan="3" class="py-4 text-center text-gray-500">No hay registros de asistencia recientes</td>
                    </tr>`;
            }
        })
        .catch(error => {
            console.error('Error al cargar el historial:', error);
            historialDiv.innerHTML = `
                <tr>
                    <td colspan="3" class="py-4 text-center text-red-500">Error al cargar el historial: ${error.message}</td>
                </tr>
            `;
        });
}

// Inicializar la página
function inicializarPagina() {
    const dniInput = document.getElementById('dniInput');
    if (dniInput) {
        dniInput.focus();
    }
    // Cargar el historial de asistencias al iniciar
    actualizarHistorial();
}

// Inicializar cuando el DOM esté listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', inicializarPagina);
} else {
    inicializarPagina();
}
