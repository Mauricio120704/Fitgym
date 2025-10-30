document.addEventListener('DOMContentLoaded', function() {
    const monthlyBtn = document.getElementById('monthly-btn');
    const annualBtn = document.getElementById('annual-btn');
    
    const priceAmounts = document.querySelectorAll('.price-amount');
    const pricePeriods = document.querySelectorAll('.price-period');
    const priceOlds = document.querySelectorAll('.price-old');

    // Obtener el usuario de la URL si existe
    const urlParams = new URLSearchParams(window.location.search);
    const usuario = urlParams.get('usuario') || '';

    function updatePlanLinks(period) {
        // Actualizar los enlaces de los botones de selección de plan
        document.querySelectorAll('.plan-card').forEach(card => {
            const planName = card.dataset.plan;
            const price = card.querySelector(`.price-amount`).dataset[period];
            const btn = card.querySelector('.btn-select-plan');
            const periodo = period === 'monthly' ? 'mensual' : 'anual';
            
            // Actualizar el enlace con los parámetros correctos
            btn.href = `/checkout?plan=${encodeURIComponent(planName)}&periodo=${periodo}&precio=${encodeURIComponent(price)}${usuario ? '&usuario=' + encodeURIComponent(usuario) : ''}`;
        });
    }

    function updatePrices(period) {
        // Actualizar el estado del botón
        if (period === 'monthly') {
            monthlyBtn.classList.add('active');
            annualBtn.classList.remove('active');
        } else {
            annualBtn.classList.add('active');
            monthlyBtn.classList.remove('active');
        }

        // Actualizar los textos de precios
        priceAmounts.forEach(el => {
            el.innerText = el.dataset[period];
        });

        pricePeriods.forEach(el => {
            el.innerText = el.dataset[period];
        });
        
        // Mostrar u ocultar el precio anterior (anual)
        priceOlds.forEach(el => {
            if (period === 'annual') {
                el.innerText = el.dataset.annual;
                el.style.display = 'block';
            } else {
                el.style.display = 'none';
            }
        });

        // Actualizar los enlaces de los botones
        updatePlanLinks(period);
    }

    // Event Listeners para los botones de cambio de período
    monthlyBtn.addEventListener('click', () => updatePrices('monthly'));
    annualBtn.addEventListener('click', () => updatePrices('annual'));

    // Establecer el estado inicial al cargar la página
    updatePrices('monthly');
});
