// ----------- RECUPERACIÓN DE CONTRASEÑA -----------

const correoInput = document.getElementById("correo");
const enviarBtn = document.getElementById("enviarBtn");
const codigoField = document.getElementById("codigoField");
const codigoInput = document.getElementById("codigo");
const form = document.getElementById("recuperarForm");

let emailEnviado = null;

// Evento del botón "Enviar Instrucciones"
enviarBtn.addEventListener("click", async () => {
    const email = correoInput.value.trim();
    
    // Validación básica
    if (!email) {
        mostrarMensaje('Por favor ingresa tu correo electrónico', 'error');
        return;
    }
    
    if (!validarEmail(email)) {
        mostrarMensaje('Por favor ingresa un correo electrónico válido', 'error');
        return;
    }
    
    // Deshabilitar el botón mientras se procesa
    enviarBtn.disabled = true;
    enviarBtn.textContent = 'Enviando...';
    
    try {
        const response = await fetch('/password-recovery/solicitar-codigo', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({ email: email })
        });
        
        const data = await response.json();
        
        if (data.success) {
            emailEnviado = email;
            mostrarMensaje(data.message, 'success');
            
            // Deshabilitar el campo de correo
            correoInput.disabled = true;
            
            // Ocultar el botón de enviar
            enviarBtn.style.display = 'none';
            
            // Mostrar el campo de código
            codigoField.style.display = 'block';
            codigoInput.focus();
        } else {
            mostrarMensaje(data.message, 'error');
            enviarBtn.disabled = false;
            enviarBtn.textContent = 'Enviar Instrucciones';
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error al enviar el código. Por favor intenta de nuevo', 'error');
        enviarBtn.disabled = false;
        enviarBtn.textContent = 'Enviar Instrucciones';
    }
});

// Evento del formulario para verificar el código
form.addEventListener("submit", async (e) => {
    e.preventDefault();
    
    const codigo = codigoInput.value.trim();
    
    if (!codigo) {
        mostrarMensaje('Por favor ingresa el código de verificación', 'error');
        return;
    }
    
    if (codigo.length !== 6 || !/^\d+$/.test(codigo)) {
        mostrarMensaje('El código debe ser de 6 dígitos', 'error');
        return;
    }
    
    const submitBtn = codigoField.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Verificando...';
    
    try {
        const response = await fetch('/password-recovery/verificar-codigo', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                email: emailEnviado,
                codigo: codigo
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            mostrarMensaje('Código verificado correctamente. Redirigiendo...', 'success');
            
            // Redirigir a la página de nueva contraseña
            setTimeout(() => {
                window.location.href = `/password-recovery/nueva-contrasena?email=${encodeURIComponent(emailEnviado)}&codigo=${encodeURIComponent(codigo)}`;
            }, 1000);
        } else {
            mostrarMensaje(data.message, 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Verificar Código';
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error al verificar el código. Por favor intenta de nuevo', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Verificar Código';
    }
});

// Función para validar email
function validarEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

// Función para mostrar mensajes
function mostrarMensaje(mensaje, tipo) {
    // Eliminar mensajes anteriores
    const mensajesAnteriores = document.querySelectorAll('.mensaje-flotante');
    mensajesAnteriores.forEach(m => m.remove());
    
    const mensajeDiv = document.createElement('div');
    mensajeDiv.className = 'mensaje-flotante';
    mensajeDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        animation: slideIn 0.3s ease-out;
        max-width: 400px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    
    if (tipo === 'success') {
        mensajeDiv.style.backgroundColor = '#4CAF50';
    } else {
        mensajeDiv.style.backgroundColor = '#f44336';
    }
    
    mensajeDiv.textContent = mensaje;
    document.body.appendChild(mensajeDiv);
    
    setTimeout(() => {
        mensajeDiv.style.animation = 'slideOut 0.3s ease-in';
        setTimeout(() => mensajeDiv.remove(), 300);
    }, 5000);
}

// Agregar animaciones CSS
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);
