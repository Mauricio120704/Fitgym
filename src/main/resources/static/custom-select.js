/**
 * Custom Select - Reemplaza los selects nativos con dropdowns personalizados
 * Proporciona control total sobre la apariencia y comportamiento
 */

class CustomSelect {
    constructor(selectElement) {
        this.select = selectElement;
        this.isOpen = false;
        this.init();
    }

    init() {
        // Crear contenedor personalizado
        this.container = document.createElement('div');
        this.container.className = 'custom-select-container';
        this.select.parentNode.insertBefore(this.container, this.select);
        this.container.appendChild(this.select);

        // Crear botón que muestra el valor seleccionado
        this.button = document.createElement('button');
        this.button.type = 'button';
        this.button.className = 'custom-select-button';
        this.button.setAttribute('aria-haspopup', 'listbox');
        this.container.insertBefore(this.button, this.select);

        // Crear dropdown list
        this.dropdown = document.createElement('div');
        this.dropdown.className = 'custom-select-dropdown';
        this.container.appendChild(this.dropdown);

        // Ocultar el select nativo
        this.select.style.display = 'none';

        // Poblar opciones
        this.populateOptions();

        // Event listeners
        this.button.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.toggle();
        });

        this.select.addEventListener('change', () => {
            this.updateButton();
        });

        document.addEventListener('click', (e) => {
            if (!this.container.contains(e.target)) {
                this.close();
            }
        });

        // Actualizar botón inicial
        this.updateButton();
    }

    populateOptions() {
        this.dropdown.innerHTML = '';
        
        Array.from(this.select.options).forEach((option, index) => {
            const item = document.createElement('div');
            item.className = 'custom-select-option';
            item.textContent = option.textContent;
            item.dataset.value = option.value;
            
            if (option.selected) {
                item.classList.add('selected');
            }
            
            if (index === 0) {
                item.classList.add('placeholder');
            }

            item.addEventListener('click', (e) => {
                e.stopPropagation();
                this.select.value = option.value;
                this.select.dispatchEvent(new Event('change', { bubbles: true }));
                this.updateButton();
                this.close();
            });

            this.dropdown.appendChild(item);
        });
    }

    updateButton() {
        const selected = this.select.options[this.select.selectedIndex];
        this.button.textContent = selected ? selected.textContent : 'Seleccionar...';
        this.button.value = this.select.value;
    }

    toggle() {
        if (this.isOpen) {
            this.close();
        } else {
            this.open();
        }
    }

    open() {
        this.isOpen = true;
        this.container.classList.add('open');
        this.dropdown.style.display = 'block';
        this.button.setAttribute('aria-expanded', 'true');
    }

    close() {
        this.isOpen = false;
        this.container.classList.remove('open');
        this.dropdown.style.display = 'none';
        this.button.setAttribute('aria-expanded', 'false');
    }
}

// Inicializar todos los selects cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    // Seleccionar todos los selects dentro de modales y formularios
    const selects = document.querySelectorAll('select:not([style*="display: none"])');
    selects.forEach(select => {
        // No aplicar a selects que ya tengan la clase custom-select-processed
        if (!select.classList.contains('custom-select-processed')) {
            new CustomSelect(select);
            select.classList.add('custom-select-processed');
        }
    });
});

// Reinicializar cuando se abren modales (para selects dinámicos)
const originalShowModal = HTMLDialogElement.prototype.showModal;
if (originalShowModal) {
    HTMLDialogElement.prototype.showModal = function() {
        originalShowModal.call(this);
        const selects = this.querySelectorAll('select:not(.custom-select-processed)');
        selects.forEach(select => {
            new CustomSelect(select);
            select.classList.add('custom-select-processed');
        });
    };
}

// También reinicializar cuando se muestran elementos con display: flex/block
const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
        if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
            const target = mutation.target;
            if (target.classList && target.classList.contains('flex') && !target.classList.contains('hidden')) {
                const selects = target.querySelectorAll('select:not(.custom-select-processed)');
                selects.forEach(select => {
                    new CustomSelect(select);
                    select.classList.add('custom-select-processed');
                });
            }
        }
    });
});

// Observar cambios en el documento
observer.observe(document.body, {
    subtree: true,
    attributes: true,
    attributeFilter: ['class', 'style']
});
