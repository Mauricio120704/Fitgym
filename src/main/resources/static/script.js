document.addEventListener('DOMContentLoaded', function () {
    // Selecciona el botón del menú y el sidebar por sus IDs
    const menuButton = document.getElementById('menu-button');
    const sidebar = document.getElementById('sidebar');

    // Verifica si ambos elementos existen en la página
    if (menuButton && sidebar) {
        // Añade un evento 'click' al botón del menú
        menuButton.addEventListener('click', () => {
            // Alterna la clase que oculta/muestra el sidebar
            sidebar.classList.toggle('-translate-x-full');
        });
    }
});
