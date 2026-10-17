document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('busca');
    const linhas = document.querySelectorAll('#tabela-servicos tbody tr[data-linha]');
    const vazio = document.getElementById('sem-resultados');
    const contador = document.getElementById('contador-n');

    const normalizar = (t) => t.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

    if (input) {
        input.addEventListener('input', () => {
            const termo = normalizar(input.value.trim());
            let visiveis = 0;
            linhas.forEach((tr) => {
                const mostrar = normalizar(tr.textContent).includes(termo);
                tr.hidden = !mostrar;
                if (mostrar) visiveis++;
            });
            if (vazio) vazio.hidden = visiveis > 0;
            if (contador) contador.textContent = visiveis;
        });

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
