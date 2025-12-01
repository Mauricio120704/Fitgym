document.addEventListener("DOMContentLoaded", function() {
    
    // Seleccionamos los inputs de radio y las tarjetas
    const radioInputs = document.querySelectorAll('.reason-card input[type="radio"]');
    const reasonCards = document.querySelectorAll(".reason-card");
    
    const startDateInput = document.getElementById("fecha-inicio");
    const endDateInput = document.getElementById("fecha-fin");
    const impactInfo = document.getElementById("impacto-suspension").querySelector("span:last-child");
    const submitButton = document.getElementById("btn-submit");
    
    // Añadimos el input de detalles
    const detailsInput = document.getElementById("detalles");
    const fileInput = document.getElementById("archivo-adjunto");
    const fileError = document.getElementById("archivo-error");

    // Botón cancelar: volver al perfil del deportista
    const cancelButton = document.querySelector(".btn-cancel");
    if (cancelButton) {
        cancelButton.addEventListener("click", function () {
            window.location.href = "/perfil";
        });
    }

    // --- Lógica de selección de Motivo ---
    radioInputs.forEach(input => {
        input.addEventListener("change", () => {
            // 1. Quita 'selected' de todas las tarjetas
            reasonCards.forEach(c => c.classList.remove("selected"));
            
            // 2. Añade 'selected' a la tarjeta padre
            if (input.checked) {
                input.closest('.reason-card').classList.add("selected");
            }
            
            // 3. Valida el formulario
            validateForm();
        });
    });

    // --- Lógica de validación de Formulario ---
    // (Renombrada de validateDatesAndImpact a validateForm)
    function validateForm() {
        const startDateValue = startDateInput.value;
        const endDateValue = endDateInput.value;
        const detailsValue = detailsInput.value.trim(); // Usamos trim()
        const reasonSelected = document.querySelector('input[name="motivo"]:checked') !== null;

        let datesAreValid = false;
        let fileIsValid = true;

        // 1. Validar lógica de fechas
        if (startDateValue && endDateValue) {
            const startDate = new Date(startDateValue);
            const endDate = new Date(endDateValue);
            const start = new Date(startDate.getUTCFullYear(), startDate.getUTCMonth(), startDate.getUTCDate());
            const end = new Date(endDate.getUTCFullYear(), endDate.getUTCMonth(), endDate.getUTCDate());

            if (end > start) {
                datesAreValid = true;
                const diffTime = Math.abs(end - start);
                const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
                impactInfo.textContent = `Tu membresía se pausará por ${diffDays} días.`;
            } else {
                impactInfo.textContent = "La fecha de fin debe ser posterior a la fecha de inicio.";
            }
        } else {
            impactInfo.textContent = "Selecciona las fechas para ver el impacto";
        }

        // 2. Validar tipo de archivo (si se seleccionó alguno)
        if (fileInput && fileInput.files && fileInput.files.length > 0) {
            const name = (fileInput.files[0].name || "").toLowerCase();
            const allowed = [".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png"];
            const hasAllowedExt = allowed.some(ext => name.endsWith(ext));

            if (!hasAllowedExt) {
                fileIsValid = false;
                if (fileError) {
                    fileError.classList.remove("hidden");
                }
            } else {
                if (fileError) {
                    fileError.classList.add("hidden");
                }
            }
        } else {
            // Sin archivo, no hay problema
            if (fileError) {
                fileError.classList.add("hidden");
            }
        }

        // 3. Validar todos los campos para habilitar el botón
        if (reasonSelected && startDateValue && endDateValue && datesAreValid && detailsValue !== "" && fileIsValid) {
            submitButton.disabled = false;
        } else {
            submitButton.disabled = true;
        }
    }

    // Añadir listeners a los inputs de fecha y detalles
    startDateInput.addEventListener("change", validateForm);
    endDateInput.addEventListener("change", validateForm);
    detailsInput.addEventListener("input", validateForm); // "input" reacciona a cada tecla
    if (fileInput) {
        fileInput.addEventListener("change", validateForm);
    }

    // --- Lógica del Formulario ---
    // Dejamos que el formulario se envíe al backend cuando el botón está habilitado.
    // Solo hacemos un chequeo final para evitar envíos accidentales si algo quedó inválido.
    document.getElementById("suspensionForm").addEventListener("submit", function(e) {
        if (submitButton.disabled) {
            e.preventDefault();
            return;
        }
        // Si el botón no está deshabilitado, permitimos el envío normal (POST /suspension)
    });

    // --- Lógica de Fecha Mínima (sin cambios) ---
    startDateInput.addEventListener("change", () => {
        if (startDateInput.value) {
            let minEndDate = new Date(startDateInput.value);
            minEndDate.setDate(minEndDate.getDate() + 2); 
            endDateInput.min = minEndDate.toISOString().split('T')[0];
        }
    });
    
    // Dispara el evento change al cargar para establecer el estado inicial
    // (esto ejecutará validateForm() y min-date)
    startDateInput.dispatchEvent(new Event('change'));
});