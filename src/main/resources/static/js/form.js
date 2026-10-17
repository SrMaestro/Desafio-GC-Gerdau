document.addEventListener('DOMContentLoaded', () => {
    const previa = document.getElementById('previa-codigo');
    if (!previa) return;

    const max = Number(previa.dataset.max);
    const valor = document.getElementById('previa-valor');
    const regua = document.getElementById('previa-regua');
    const contador = document.getElementById('previa-contador');
    const aviso = document.getElementById('previa-aviso');
    const legenda = document.getElementById('previa-legenda');
    const botao = document.querySelector('form button[type="submit"]');
    const campo = (nome) => document.querySelector(`[name="${nome}"]`);

    for (let i = 0; i < max; i++) regua.appendChild(document.createElement('i'));
    const marcas = regua.children;

    function parte(classe, texto) {
        const span = document.createElement('span');
        span.className = classe;
        span.textContent = texto;
        return span;
    }

    // Mesma regra do ServicoService.gerarCodigo, mantendo as partes para colorir
    function partes() {
        const condicao = campo('condicao');
        const sufixo = condicao.selectedOptions[0]?.dataset.sufixo || '';
        return [
            ['parte-abrev', 'Abreviatura', campo('abreviatura').value.toUpperCase()],
            ['parte-compl', 'Complemento', campo('complemento').value.toUpperCase()],
            ['parte-sep', 'Separador', '_'],
            ['parte-cond', 'Condição', sufixo],
            ['parte-cnae', 'CNAE', campo('cnae').value],
        ];
    }

    function atualizar() {
        const lista = partes();
        const total = lista.reduce((n, p) => n + p[2].length, 0);
        const excedeu = total > max;

        valor.replaceChildren(...lista.filter((p) => p[2]).map((p) => parte(p[0], p[2])));
        if (!valor.textContent) valor.textContent = '—';

        for (let i = 0; i < marcas.length; i++) marcas[i].classList.toggle('on', i < total);
        regua.classList.toggle('estourou', excedeu);

        contador.textContent = `${total} de ${max}`;
        previa.classList.toggle('estourou', excedeu);
        aviso.hidden = !excedeu;
        if (botao) botao.disabled = excedeu;

        legenda.replaceChildren(...lista.filter((p) => p[0] !== 'parte-sep').map((p) => {
            const li = document.createElement('li');
            li.className = p[0];
            li.append(p[1] + ': ', p[2] ? p[2] : 'vazio');
            return li;
        }));
    }

    ['abreviatura', 'complemento', 'condicao', 'cnae'].forEach((nome) => {
        const el = campo(nome);
        el.addEventListener('input', atualizar);
        el.addEventListener('change', atualizar);
    });
    atualizar();
});
