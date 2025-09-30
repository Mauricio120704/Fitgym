// Aplicar estado del sidebar ANTES de que se cargue el DOM (sin animación)
(function() {
    const sidebarExpanded = localStorage.getItem('sidebarExpanded');
    if (sidebarExpanded === 'true') {
        // Agregar clase al HTML para aplicar estilos inmediatamente
        document.documentElement.classList.add('sidebar-expanded');
    }
})();

document.addEventListener('DOMContentLoaded', function () {
    const menuButton = document.getElementById('menu-button');
    const sidebar = document.getElementById('sidebar');
    const closeSidebarBtn = document.getElementById('close-sidebar');
    const sidebarTexts = document.querySelectorAll('.sidebar-text');
    const sidebarTitle = document.querySelector('.sidebar-title');
    const sidebarIcon = document.querySelector('.sidebar-icon');
    const menuLinks = document.querySelectorAll('#sidebar nav a');

    // Función para expandir el sidebar
    function expandSidebar() {
        sidebar.classList.remove('w-20');
        sidebar.classList.add('w-64');
        document.documentElement.style.setProperty('--sidebar-width', '16rem');
        
        sidebarTexts.forEach(text => {
            text.classList.remove('hidden');
        });
        
        if (sidebarTitle) {
            sidebarTitle.classList.remove('hidden');
        }
        
        if (sidebarIcon) {
            sidebarIcon.classList.add('hidden');
        }
        
        menuLinks.forEach(link => {
            link.classList.remove('justify-center');
            link.classList.add('justify-start', 'px-4');
        });
        
        // Guardar estado
        localStorage.setItem('sidebarExpanded', 'true');
        document.documentElement.classList.add('sidebar-expanded');
    }

    // Función para colapsar el sidebar
    function collapseSidebar() {
        sidebar.classList.remove('w-64');
        sidebar.classList.add('w-20');
        document.documentElement.style.setProperty('--sidebar-width', '5rem');
        
        sidebarTexts.forEach(text => {
            text.classList.add('hidden');
        });
        
        if (sidebarTitle) {
            sidebarTitle.classList.add('hidden');
        }
        
        if (sidebarIcon) {
            sidebarIcon.classList.remove('hidden');
        }
        
        menuLinks.forEach(link => {
            link.classList.add('justify-center');
            link.classList.remove('justify-start', 'px-4');
        });
        
        // Guardar estado
        localStorage.setItem('sidebarExpanded', 'false');
        document.documentElement.classList.remove('sidebar-expanded');
    }

    // Restaurar estado del sidebar al cargar la página (sin animación visible)
    const sidebarExpanded = localStorage.getItem('sidebarExpanded');
    
    // Desactivar temporalmente las transiciones
    sidebar.style.transition = 'none';
    
    if (sidebarExpanded === 'true') {
        expandSidebar();
    } else {
        collapseSidebar();
    }
    
    // Reactivar las transiciones después de aplicar el estado
    setTimeout(() => {
        sidebar.style.transition = '';
    }, 50);

    // Toggle del sidebar en desktop y móviles
    if (menuButton && sidebar) {
        menuButton.addEventListener('click', () => {
            if (window.innerWidth < 768) {
                // En móviles: abrir/cerrar completamente
                sidebar.classList.toggle('-translate-x-full');
            } else {
                // En desktop: expandir/colapsar
                const isCollapsed = sidebar.classList.contains('w-20');
                
                if (isCollapsed) {
                    expandSidebar();
                } else {
                    collapseSidebar();
                }
            }
        });
    }

    if (closeSidebarBtn && sidebar) {
        closeSidebarBtn.addEventListener('click', () => {
            sidebar.classList.add('-translate-x-full');
        });
    }

    // Cerrar sidebar al hacer clic fuera en móviles
    document.addEventListener('click', function(event) {
        if (window.innerWidth < 768) {
            const isClickInsideSidebar = sidebar && sidebar.contains(event.target);
            const isClickOnMenuButton = menuButton && menuButton.contains(event.target);
            
            if (!isClickInsideSidebar && !isClickOnMenuButton && sidebar && !sidebar.classList.contains('-translate-x-full')) {
                sidebar.classList.add('-translate-x-full');
            }
        }
    });

    // =================== Filtros Universales (estilo fitmanager) ===================
    const searchInput = document.querySelector('input[name="buscar"]');
    const estadoSelect = document.querySelector('select[name="estado"]');
    const prioridadSelect = document.querySelector('select[name="prioridad"]');
    
    // Filtros para Incidencias
    function applyIncidenciasFilters() {
        const incidentCards = document.querySelectorAll('.space-y-4 > div[class*="border"]');
        if (incidentCards.length === 0) return;
        
        const searchTerm = (searchInput?.value || '').toLowerCase();
        const estadoValue = (estadoSelect?.value || 'todos').toLowerCase();
        const prioridadValue = (prioridadSelect?.value || 'todas').toLowerCase();
        
        incidentCards.forEach(card => {
            const titulo = (card.querySelector('h3')?.textContent || '').toLowerCase();
            const descripcion = (card.querySelector('p')?.textContent || '').toLowerCase();
            const reportadoPor = (card.textContent || '').toLowerCase();
            
            const estadoSpans = card.querySelectorAll('span[class*="rounded-full"]');
            let cardEstado = 'todos';
            let cardPrioridad = 'todas';
            
            estadoSpans.forEach(span => {
                const text = span.textContent.toLowerCase();
                if (text.includes('abierto')) cardEstado = 'abierto';
                else if (text.includes('resuelto')) cardEstado = 'resuelto';
                
                if (text.includes('alta')) cardPrioridad = 'alta';
                else if (text.includes('media')) cardPrioridad = 'media';
                else if (text.includes('baja')) cardPrioridad = 'baja';
            });
            
            const matchesSearch = searchTerm === '' || titulo.includes(searchTerm) || descripcion.includes(searchTerm) || reportadoPor.includes(searchTerm);
            const matchesEstado = estadoValue === 'todos' || cardEstado === estadoValue;
            const matchesPrioridad = prioridadValue === 'todas' || cardPrioridad === prioridadValue;
            
            card.style.display = (matchesSearch && matchesEstado && matchesPrioridad) ? '' : 'none';
        });
    }
    
    // Filtros para Clases (tabla)
    function applyClasesFilters() {
        const rows = document.querySelectorAll('table tbody tr');
        if (rows.length === 0) return;
        
        const searchTerm = (searchInput?.value || '').toLowerCase();
        
        rows.forEach(row => {
            const nombre = (row.cells[0]?.textContent || '').toLowerCase();
            const instructor = (row.cells[1]?.textContent || '').toLowerCase();
            
            const matchesSearch = searchTerm === '' || nombre.includes(searchTerm) || instructor.includes(searchTerm);
            
            row.style.display = matchesSearch ? '' : 'none';
        });
    }
    
    // Filtros para Miembros/Pagos (tabla)
    function applyTableFilters() {
        const rows = document.querySelectorAll('table tbody tr');
        if (rows.length === 0) return;
        
        const searchTerm = (searchInput?.value || '').toLowerCase();
        const estadoValue = (estadoSelect?.value || 'todos').toLowerCase();
        
        rows.forEach(row => {
            const rowText = row.textContent.toLowerCase();
            const matchesSearch = searchTerm === '' || rowText.includes(searchTerm);
            
            let matchesEstado = true;
            if (estadoValue !== 'todos' && estadoValue !== '') {
                matchesEstado = rowText.includes(estadoValue);
            }
            
            row.style.display = (matchesSearch && matchesEstado) ? '' : 'none';
        });
    }
    
    // Detectar qué tipo de filtro aplicar según la página
    function applyFilters() {
        if (document.querySelector('.incident-card')) {
            applyIncidenciasFilters();
        } else if (window.location.pathname.includes('/clases')) {
            applyClasesFilters();
        } else {
            applyTableFilters();
        }
    }
    
    // Aplicar filtros sin recargar la página
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            e.preventDefault();
            applyFilters();
        });
    }
    
    if (estadoSelect) {
        estadoSelect.addEventListener('change', (e) => {
            e.preventDefault();
            applyFilters();
        });
    }
    
    if (prioridadSelect) {
        prioridadSelect.addEventListener('change', (e) => {
            e.preventDefault();
            applyFilters();
        });
    }
    
    // Prevenir envío SOLO de formularios de filtro (que tienen método GET)
    const filterForms = document.querySelectorAll('form[method="get"]');
    filterForms.forEach(form => {
        // Solo prevenir si tiene campos de filtro
        if (form.querySelector('input[name="buscar"]') || form.querySelector('select[name="estado"]')) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                applyFilters();
            });
        }
    });
});
