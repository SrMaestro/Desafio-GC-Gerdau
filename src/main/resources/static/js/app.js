document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('busca');
    const linhas = document.querySelectorAll('#tabela-servicos tbody tr[data-linha]');
    const vazio = document.getElementById('sem-resultados');
    if (!input) return;

    const normalizar = (t) => t.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

    input.addEventListener('input', () => {
        const termo = normalizar(input.value.trim());
        let visiveis = 0;
        linhas.forEach((tr) => {
            const mostrar = normalizar(tr.textContent).includes(termo);
            tr.hidden = !mostrar;
            if (mostrar) visiveis++;
        });
        if (vazio) vazio.hidden = visiveis > 0;
    });
});
