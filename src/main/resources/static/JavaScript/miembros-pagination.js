// Configuración de paginación
const ITEMS_PER_PAGE = 10;
let currentPage = 1;

function initPagination() {
    const table = document.querySelector('table');
    if (!table) return;

    const tbody = table.querySelector('tbody');
    if (!tbody) return;

    const rows = Array.from(tbody.querySelectorAll('tr'));
    if (rows.length <= ITEMS_PER_PAGE) return;

    // Crear controles de paginación
    const paginationContainer = document.createElement('div');
    paginationContainer.className = 'mt-4 flex justify-center space-x-2';
    table.parentNode.insertBefore(paginationContainer, table.nextSibling);

    function updatePage(page) {
        currentPage = page;
        const start = (page - 1) * ITEMS_PER_PAGE;
        const end = start + ITEMS_PER_PAGE;

        rows.forEach((row, index) => {
            row.style.display = (index >= start && index < end) ? '' : 'none';
        });

        updatePaginationControls();
    }

    function updatePaginationControls() {
        const totalPages = Math.ceil(rows.length / ITEMS_PER_PAGE);
        paginationContainer.innerHTML = '';

        // Botón anterior
        if (currentPage > 1) {
            addPageButton('Anterior', currentPage - 1);
        }

        // Números de página
        for (let i = 1; i <= totalPages; i++) {
            if (i === 1 || i === totalPages || (i >= currentPage - 1 && i <= currentPage + 1)) {
                addPageButton(i.toString(), i, i === currentPage);
            } else if (i === currentPage - 2 || i === currentPage + 2) {
                addEllipsis();
            }
        }

        // Botón siguiente
        if (currentPage < totalPages) {
            addPageButton('Siguiente', currentPage + 1);
        }
    }

    function addPageButton(text, page, isActive = false) {
        const button = document.createElement('button');
        button.textContent = text;
        button.className = `px-3 py-1 rounded ${isActive ? 'bg-orange-500 text-white' : 'bg-white text-gray-700 hover:bg-gray-100'}`;
        button.addEventListener('click', () => updatePage(page));
        paginationContainer.appendChild(button);
    }

    function addEllipsis() {
        const span = document.createElement('span');
        span.textContent = '...';
        span.className = 'px-3 py-1 text-gray-500';
        paginationContainer.appendChild(span);
    }

    // Inicializar la primera página
    updatePage(1);
}

// Inicializar cuando el DOM esté cargado
document.addEventListener('DOMContentLoaded', initPagination);