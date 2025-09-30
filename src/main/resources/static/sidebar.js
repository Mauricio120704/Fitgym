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
});
