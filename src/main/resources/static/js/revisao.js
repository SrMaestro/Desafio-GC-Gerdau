document.addEventListener('DOMContentLoaded', () => {
    const campo = document.getElementById('revisor');
    if (!campo) return;

    try {
        campo.value = localStorage.getItem('revisor') || '';
    } catch (e) { /* armazenamento indisponível: o campo continua em branco */ }

    campo.addEventListener('input', () => {
        try { localStorage.setItem('revisor', campo.value.trim()); } catch (e) { /* ignora */ }
    });

    document.querySelectorAll('form[data-revisor]').forEach((form) => {
        form.addEventListener('submit', (e) => {
            const nome = campo.value.trim();
            if (!nome) {
                e.preventDefault();
                campo.focus();
                campo.setCustomValidity('Informe seu nome para registrar a decisão');
                campo.reportValidity();
                return;
            }
            campo.setCustomValidity('');
            form.querySelector('input[name="revisor"]').value = nome;
        });
    });
});
