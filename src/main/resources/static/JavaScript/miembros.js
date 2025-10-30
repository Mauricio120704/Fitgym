// Función para alternar la visibilidad de la contraseña
function togglePwd(inputId, toggleBtnId) {
    const input = document.getElementById(inputId);
    const button = document.getElementById(toggleBtnId);
    const iconEye = button.querySelector('.icon-eye');
    const iconEyeOff = button.querySelector('.icon-eye-off');

    if (input.type === 'password') {
        input.type = 'text';
        iconEye.classList.add('hidden');
        iconEyeOff.classList.remove('hidden');
    } else {
        input.type = 'password';
        iconEye.classList.remove('hidden');
        iconEyeOff.classList.add('hidden');
    }
}

// Eliminación por formulario POST, no se requiere JS