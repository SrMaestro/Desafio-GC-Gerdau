document.addEventListener('DOMContentLoaded', () => {
    const previa = document.getElementById('previa-codigo');
    if (!previa) return;

    const max = Number(previa.dataset.max);
    const valor = document.getElementById('previa-valor');
    const contador = document.getElementById('previa-contador');
    const aviso = document.getElementById('previa-aviso');
    const botao = document.querySelector('form button[type="submit"]');
    const campo = (nome) => document.querySelector(`[name="${nome}"]`);

    // Mesma regra do ServicoService.gerarCodigo
    function atualizar() {
        const condicao = campo('condicao');
        const sufixo = condicao.selectedOptions[0]?.dataset.sufixo || '';
        const codigo = (campo('abreviatura').value + campo('complemento').value
                + '_' + sufixo + campo('cnae').value).toUpperCase();
        const excedeu = codigo.length > max;

        valor.textContent = codigo;
        contador.textContent = `${codigo.length}/${max}`;
        contador.style.color = excedeu ? '#c00' : '';
        aviso.hidden = !excedeu;
        if (botao) botao.disabled = excedeu;
    }

    ['abreviatura', 'complemento', 'condicao', 'cnae'].forEach((nome) => {
        const el = campo(nome);
        el.addEventListener('input', atualizar);
        el.addEventListener('change', atualizar);
    });
    atualizar();
});
