// Pre-aplicar estado del sidebar antes de pintar (evita parpadeos)
(function () {
  if (localStorage.getItem('sidebarExpanded') === 'true') {
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

    const allowedByRole = {
        'ADMINISTRADOR': ['/', '/inicio', '/registro', '/miembros', '/incidencias', '/pagos', '/clases', '/evaluaciones', '/entrenamientos', '/perfil', '/configuracion'],
        'RECEPCIONISTA': ['/inicio', '/registro', '/miembros', '/incidencias', '/pagos', '/clases', '/perfil'],
        'ENTRENADOR': ['/inicio', '/miembros', '/entrenamientos', '/evaluaciones', '/clases', '/perfil']
    };

    function normalizeHref(href) {
        try {
            const url = new URL(href, window.location.origin);
            return url.pathname;
        } catch {
            return href;
        }
    }

    function expandSidebar() {
        if (!sidebar) return;
        sidebar.classList.remove('w-20');
        sidebar.classList.add('w-64');
        document.documentElement.style.setProperty('--sidebar-width', '16rem');
        sidebarTexts.forEach(t => t.classList.remove('hidden'));
        if (sidebarTitle) sidebarTitle.classList.remove('hidden');
        if (sidebarIcon) sidebarIcon.classList.add('hidden');
        menuLinks.forEach(l => { l.classList.remove('justify-center'); l.classList.add('justify-start','px-4'); });
        localStorage.setItem('sidebarExpanded', 'true');
        document.documentElement.classList.add('sidebar-expanded');
    }

    function collapseSidebar() {
        if (!sidebar) return;
        sidebar.classList.remove('w-64');
        sidebar.classList.add('w-20');
        document.documentElement.style.setProperty('--sidebar-width', '5rem');
        sidebarTexts.forEach(t => t.classList.add('hidden'));
        if (sidebarTitle) sidebarTitle.classList.add('hidden');
        if (sidebarIcon) sidebarIcon.classList.remove('hidden');
        menuLinks.forEach(l => { l.classList.add('justify-center'); l.classList.remove('justify-start','px-4'); });
        localStorage.setItem('sidebarExpanded', 'false');
        document.documentElement.classList.remove('sidebar-expanded');
    }

    const savedExpanded = localStorage.getItem('sidebarExpanded');
    if (savedExpanded === 'true') expandSidebar(); else collapseSidebar();

    if (menuButton && sidebar) {
        menuButton.addEventListener('click', () => {
            if (window.innerWidth < 768) {
                sidebar.classList.toggle('-translate-x-full');
            } else {
                if (sidebar.classList.contains('w-20')) {
                    expandSidebar();
                } else {
                    collapseSidebar();
                }
            }
        });
    }

    if (closeSidebarBtn && sidebar) {
        closeSidebarBtn.addEventListener('click', () => sidebar.classList.add('-translate-x-full'));
    }

    const ENFORCE_ROLE_PERMISSIONS = false; // cambiar a true cuando se desee forzar permisos

    function applyRolePermissions() {
        const toggle = localStorage.getItem('enforceRolePermissions') === 'true';
        if (!ENFORCE_ROLE_PERMISSIONS && !toggle) {
            document.querySelectorAll('#sidebar nav a').forEach(a => {
                a.classList.remove('pointer-events-none', 'opacity-40');
                if (a.parentElement) a.parentElement.classList.remove('hidden');
            });
            return;
        }
        const currentRole = localStorage.getItem('currentRole');
        const items = document.querySelectorAll('#sidebar nav a');
        items.forEach(a => {
            a.classList.remove('pointer-events-none', 'opacity-40');
            if (a.parentElement) a.parentElement.classList.remove('hidden');
        });
        if (!currentRole || !allowedByRole[currentRole]) return; // sin restricción o rol desconocido
        const allowed = allowedByRole[currentRole];
        items.forEach(a => {
            const path = normalizeHref(a.getAttribute('href'));
            const ok = allowed.some(p => path === p);
            if (!ok) {
                if (a.parentElement) a.parentElement.classList.add('hidden');
                a.classList.add('pointer-events-none', 'opacity-40');
            }
        });
    }

    applyRolePermissions();

    window.addEventListener('storage', (e) => {
        if (e.key === 'currentRole' || e.key === 'enforceRolePermissions') {
            applyRolePermissions();
        }
    });
});
