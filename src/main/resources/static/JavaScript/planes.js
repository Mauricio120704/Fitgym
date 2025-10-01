document.addEventListener('DOMContentLoaded', function() {
    const monthlyBtn = document.getElementById('monthly-btn');
    const annualBtn = document.getElementById('annual-btn');
    
    const priceAmounts = document.querySelectorAll('.price-amount');
    const pricePeriods = document.querySelectorAll('.price-period');
    const priceOlds = document.querySelectorAll('.price-old');

    let currentPeriod = 'monthly';

    function updatePrices(period) {
        currentPeriod = period;
        
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
    }


    // Event Listeners para los botones
    monthlyBtn.addEventListener('click', () => updatePrices('monthly'));
    annualBtn.addEventListener('click', () => updatePrices('annual'));


    // Establecer el estado inicial al cargar la página
    updatePrices('monthly');
    
    
    // Event Listeners para los botones de seleccionar plan
    document.querySelectorAll('.btn-select-plan').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const planName = this.dataset.plan;
            const priceElement = this.closest('.card').querySelector('.price-amount');
            const price = priceElement.innerText;
            const periodo = currentPeriod === 'monthly' ? 'mensual' : 'anual';
            
            // Redirigir al checkout con los parámetros
            window.location.href = `/checkout?plan=${encodeURIComponent(planName)}&periodo=${periodo}&precio=${encodeURIComponent(price)}`;
        });
    });
});
