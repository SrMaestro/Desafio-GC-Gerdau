document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('busca');

    if (input) {
        document.addEventListener('keydown', (e) => {
            const emCampo = /^(INPUT|TEXTAREA|SELECT)$/.test(document.activeElement.tagName);
            if (e.key === '/' && !emCampo) {
                e.preventDefault();
                input.focus();
            }
        });
    }

    document.querySelectorAll('.copiar').forEach((botao) => {
        botao.addEventListener('click', async () => {
            try {
                await navigator.clipboard.writeText(botao.dataset.codigo);
                botao.textContent = 'Copiado';
                botao.classList.add('copiado');
                setTimeout(() => {
                    botao.textContent = 'Copiar';
                    botao.classList.remove('copiado');
                }, 1500);
            } catch (e) {
                botao.textContent = 'Falhou';
            }
        });
    });
});
